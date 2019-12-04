import java.util.*;

/**
 * This class handles all transactions including read or write on variables and events of different
 * sites. It is able to detect deadlocks.
 * @version 12/04/2019
 * @author Xinyi Liu, Ming Xu
 */
public class TransactionManager {
    public static final int SITE_COUNT = 10;

    private Map<Integer, DataManager> sites;            // <siteId, dataManager>
    private Map<Integer, Transaction> transactions;     // <transactionId, transaction>
    private List<Operation> waitingOperations;
    private Map<Integer, Set<Integer>> waitsForGraph;   // <transactionId, Set<transactionId>>

    public TransactionManager() {
        sites = new HashMap<>();
        for (int i = 1; i <= SITE_COUNT; i++) {
            sites.put(i, new DataManager(i));
        }
        transactions = new HashMap<>();
        waitingOperations = new ArrayList<>();
        waitsForGraph = new HashMap<>();
    }

    /**
     * Begins a new transaction if not exists.
     * @param tid transactionId
     * @param ts timestamp
     */
    public void begin(int tid, int ts) {
        if (!transactions.containsKey(tid)) {
            transactions.put(tid, new Transaction(tid, ts, Transaction.TransactionType.READ_WRITE));
            waitsForGraph.put(tid, new HashSet<>());
            System.out.println(String.format("T%d begins", tid));
        }
    }

    /**
     * Begins a new read-only transaction if not exists.
     * @param tid transactionId
     * @param ts timestamp
     */
    public void beginRO(int tid, int ts) {
        if (!transactions.containsKey(tid)) {
            transactions.put(tid, new Transaction(tid, ts, Transaction.TransactionType.READ_ONLY));
            System.out.println(String.format("T%d begins and is read-only", tid));
        }
    }

    /**
     * Ends a transaction if exists.
     * @param tid transactionId
     * @param ts timestamp
     */
    public void end(int tid, int ts) {
        if (transactions.containsKey(tid)) {
            if (transactions.get(tid).isAborted()) {
                System.out.println(String.format("T%d aborts due to previous access of a down site", tid));
                abort(tid);
            } else {
                if (Transaction.TransactionType.READ_WRITE.equals(transactions.get(tid).getType())) {
                    for (Integer siteId : transactions.get(tid).getAccessedSites()) {
                        sites.get(siteId).commit(tid, ts);
                    }
                }
                System.out.println(String.format("T%d commits", tid));
                transactions.remove(tid);
                removeFromWaitsForGraph(tid);
                retry();
            }
        }
    }

    /**
     * Reads value from a variable.
     * @param tid transactionId
     * @param vid variableId
     * @param ts timestamp
     */
    public void read(int tid, int vid, int ts) {
        if (transactions.containsKey(tid)) {
            Operation operation = new Operation(ts, tid, vid, Operation.OperationType.READ, 0);
            Transaction transaction = transactions.get(tid);
            Integer conflictTransactionId = getConflictWriteOperationWaitingBefore(tid, vid, ts);
            if (conflictTransactionId != null) {
                addToWaitsForGraphFromWaitingOperations(tid, conflictTransactionId);
            } else {
                for (int i = 1; i <= SITE_COUNT; i++) {
                    if (sites.get(i).canRead(transaction.getType(), operation)) {
                        int value = sites.get(i).read(transaction.getType(), transaction.getTimestamp(), operation);
                        transaction.addAccessedSite(i);
                        transaction.unblock();
                        System.out.println(String.format("T%d reads x%d: %d", tid, vid, value));
                        return;
                    }
                }
                addToWaitsForGraphFromExecutedOperations(tid, vid);
            }
            if (!transaction.isBlocked()) {
                waitingOperations.add(operation);
                transaction.block();
                System.out.println(String.format("T%d blocked", tid));
            }
            detectDeadlock(tid);
        }
    }

    /**
     * Writes value to a variable for all copies stored in all available sites.
     * @param tid transactionId
     * @param vid variableId
     * @param v value
     * @param ts timestamp
     */
    public void write(int tid, int vid, int v, int ts) {
        if (transactions.containsKey(tid)) {
            Operation operation = new Operation(ts, tid, vid, Operation.OperationType.WRITE, v);
            Transaction transaction = transactions.get(tid);
            Integer conflictTransactionId = getConflictOperationWaitingBefore(tid, vid, ts);
            if (conflictTransactionId != null && waitsForGraph.get(conflictTransactionId).size() > 0) {
                addToWaitsForGraphFromWaitingOperations(tid, conflictTransactionId);
            } else {
                boolean canWrite = true;
                for (DataManager site : sites.values()) {
                    if (site.isActive() && site.containsVariable(vid)) {
                        canWrite = canWrite && site.canWrite(transaction.getType(), operation);
                    }
                }
                if (canWrite) {
                    for (DataManager site : sites.values()) {
                        if (site.isActive() && site.containsVariable(vid)) {
                            site.write(transaction.getType(), operation);
                            transaction.addAccessedSite(site.getId());
                        }
                    }
                    transaction.unblock();
                    System.out.println(String.format("T%d writes x%d: %d", tid, vid, v));
                    return;
                }
                addToWaitsForGraphFromExecutedOperations(tid, vid);
            }
            if (!transaction.isBlocked()) {
                waitingOperations.add(operation);
                transaction.block();
                System.out.println(String.format("T%d blocked", tid));
            }
            detectDeadlock(tid);
        }
    }

    /**
     * Gives the committed values of all copies of all variables at all sites, including sites
     * that are down.
     */
    public void dump() {
        for (int i = 1; i <= SITE_COUNT; i++) {
            sites.get(i).dump();
        }
    }

    /**
     * Executes a site failure event.
     * @param sid siteId
     */
    public void fail(int sid) {
        if (sites.containsKey(sid)) {
            sites.get(sid).fail();
            for (Transaction transaction : transactions.values()) {
                if (transaction.hasAccessedSite(sid)) {
                    transaction.setAborted();
                }
            }
            System.out.println(String.format("site %d fails", sid));
        }
    }

    /**
     * Executes a site recovery event.
     * @param sid siteId.
     */
    public void recover(int sid) {
        if (sites.containsKey(sid)) {
            sites.get(sid).recover();
            System.out.println(String.format("site %d recovers", sid));
        }
        retry();
    }

    /**
     * Aborts a transaction.
     * @param tid transactionId
     */
    private void abort(int tid) {
        for (DataManager site : sites.values()) {
            site.abort(tid);
        }
        waitingOperations.removeIf(operation -> operation.getTransactionId() == tid);
        transactions.remove(tid);
        removeFromWaitsForGraph(tid);
        retry();
    }

    /**
     * Retries waiting operations by time order.
     */
    private void retry() {
        Iterator<Operation> iterator = waitingOperations.iterator();
        while (iterator.hasNext()) {
            Operation operation = iterator.next();
            if (transactions.containsKey(operation.getTransactionId())) {
                if (Operation.OperationType.READ.equals(operation.getType())) {
                    read(operation.getTransactionId(), operation.getVariableId(), operation.getTimestamp());
                } else {
                    write(operation.getTransactionId(), operation.getVariableId(), operation.getValue(), operation.getTimestamp());
                }
                if (!transactions.get(operation.getTransactionId()).isBlocked()) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Gets the transactionId of conflict write operation right before the new operation that
     * accesses the same variable.
     * @param tid transactionId
     * @param vid variableId
     * @param ts timestamp
     * @return conflictTransactionId
     */
    private Integer getConflictWriteOperationWaitingBefore(int tid, int vid, int ts) {
        Integer conflictTransactionId = null;
        for (Operation operation : waitingOperations) {
            if (operation.getTimestamp() >= ts) {
                break;
            } else if (Operation.OperationType.WRITE.equals(operation.getType()) &&
                    operation.getVariableId() == vid && operation.getTransactionId() != tid) {
                conflictTransactionId = operation.getTransactionId();
            }
        }
        return conflictTransactionId;
    }

    /**
     * Gets the transactionId of conflict operation right before the new operation that accesses
     * the same variable.
     * @param tid transactionId
     * @param vid variableId
     * @param ts timestamp
     * @return conflictTransactionId
     */
    private Integer getConflictOperationWaitingBefore(int tid, int vid, int ts) {
        Integer conflictTransactionId = null;
        for (Operation operation : waitingOperations) {
            if (operation.getTimestamp() >= ts) {
                break;
            } else if (operation.getVariableId() == vid && operation.getTransactionId() != tid) {
                conflictTransactionId = operation.getTransactionId();
            }
        }
        return conflictTransactionId;
    }

    /**
     * Adds a pair of conflict relation in waiting operations to waits-for graph.
     * @param tid transactionId
     * @param ctid conflictTransactionId
     */
    private void addToWaitsForGraphFromWaitingOperations(int tid, int ctid) {
        if (waitsForGraph.containsKey(tid)) {
            waitsForGraph.get(tid).add(ctid);
        }
    }

    /**
     * Adds all conflict relation of a transaction due to current lock holders to wait-for graph.
     * @param tid transactionId
     * @param vid variableId
     */
    private void addToWaitsForGraphFromExecutedOperations(int tid, int vid) {
        if (waitsForGraph.containsKey(tid)) {
            for (DataManager site : sites.values()) {
                if (site.isActive() && site.containsVariable(vid)) {
                    List<Integer> conflictTransactionIds = site.getLockHolders(vid);
                    conflictTransactionIds.removeIf(conflictTransactionId -> conflictTransactionId == tid);
                    waitsForGraph.get(tid).addAll(conflictTransactionIds);
                }
            }
        }
    }

    /**
     * Removes all conflict relations from waits-for graph.
     * @param tid transactionId
     */
    private void removeFromWaitsForGraph(int tid) {
        waitsForGraph.remove(tid);
        for (Set transactionIds : waitsForGraph.values()) {
            transactionIds.remove(tid);
        }
    }

    /**
     * Detects deadlocks in waits-for graph and aborts the youngest transaction if there is.
     * @param tid transactionId
     */
    private void detectDeadlock(int tid) {
        if (waitsForGraph.containsKey(tid)) {
            List<Integer> cycle = new ArrayList<>();
            List<Integer> visited = new ArrayList<>();
            if (isCyclic(tid, cycle, visited)) {
                int youngestTransactionId = getYoungestTransactionId(cycle);
                System.out.println(String.format("T%d aborts due to deadlock", youngestTransactionId));
                abort(youngestTransactionId);
            }
        }
    }

    /**
     * Determine whether there is a cycle start from the input transactionId.
     * @param tid transactionId
     * @param cycle list of transactionIds that form the cycle
     * @param visited list of transactionIds that visited
     * @return boolean
     */
    private boolean isCyclic(int tid, List<Integer> cycle, List<Integer> visited) {
        if (cycle.contains(tid)) {
            return true;
        }
        if (visited.contains(tid)) {
            return false;
        }
        visited.add(tid);
        cycle.add(tid);
        for (int conflictTransactionId : waitsForGraph.get(tid)) {
            if (isCyclic(conflictTransactionId, cycle, visited)) {
                return true;
            }
        }
        cycle.remove(Integer.valueOf(tid));
        return false;
    }

    /**
     * Gets the youngest transactionId.
     * @param cycle list of transactionIds that form a cycle
     * @return youngestTransactionId
     */
    private int getYoungestTransactionId(List<Integer> cycle) {
        int timestamp = -1;
        int transactionId = -1;
        for (int conflictTransactionId : cycle) {
            if (transactions.get(conflictTransactionId).getTimestamp() > timestamp) {
                timestamp = transactions.get(conflictTransactionId).getTimestamp();
                transactionId = conflictTransactionId;
            }
        }
        return transactionId;
    }

}
