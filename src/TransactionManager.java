import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Xinyi Liu, Ming Xu
 */
public class TransactionManager {
    public static final int SITE_COUNT = 10;

    private Map<Integer, DataManager> sites;        // <siteId, dataManager>
    private Map<Integer, Transaction> transactions; // <transactionId, transaction>
    private List<Operation> waitingOperations;

    public TransactionManager() {
        sites = new HashMap<>();
        for (int i = 1; i <= SITE_COUNT; i++) {
            sites.put(i, new DataManager(i));
        }
        transactions = new HashMap<>();
        waitingOperations = new ArrayList<>();
    }

    /**
     * Begins a new transaction if not exists.
     * @param tid transactionId
     * @param ts timestamp
     */
    public void begin(int tid, int ts) {
        if (!transactions.containsKey(tid)) {
            transactions.put(tid, new Transaction(tid, ts, Transaction.TransactionType.READ_WRITE));
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
                abort(tid);
                System.out.println(String.format("T%d aborts", tid));
            } else {
                if (Transaction.TransactionType.READ_WRITE.equals(transactions.get(tid).getType())) {
                    for (Integer siteId : transactions.get(tid).getAccessedSites()) {
                        sites.get(siteId).commit(tid, ts);
                    }
                }
                System.out.println(String.format("T%d commits", tid));
            }
            transactions.remove(tid);
        }
    }

    /**
     * Reads value from a variable.
     * @param tid transactionId
     * @param vid variableId
     */
    public void read(int tid, int vid) {
        if (transactions.containsKey(tid) && !transactions.get(tid).isAbortedByDeadlock()) {
            Operation operation = new Operation(tid, vid, Operation.OperationType.READ, 0);
            Transaction transaction = transactions.get(tid);
            for (int i = 1; i <= SITE_COUNT; i++) {
                if (sites.get(i).canRead(transaction.getType(), operation)) {
                    int value = sites.get(i).read(transaction.getType(), transaction.getTimestamp(), operation);
                    transaction.addAccessedSite(i);
                    System.out.println(String.format("x%d: %d", vid, value));
                    return;
                }
            }
            waitingOperations.add(operation);
        }
    }

    /**
     * Writes value to a variable for all copies stored in all available sites.
     * @param tid transactionId
     * @param vid variableId
     * @param v value
     */
    public void write(int tid, int vid, int v) {
        if (transactions.containsKey(tid) && !transactions.get(tid).isAbortedByDeadlock()) {
            Operation operation = new Operation(tid, vid, Operation.OperationType.WRITE, v);
            Transaction transaction = transactions.get(tid);
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
                System.out.println(String.format("T%d writes %d to x%d", tid, v, vid));
            } else {
                waitingOperations.add(operation);
            }
        }
    }

    /**
     * Gives the committed values of all copies of all variables at all sites, including sites that are down.
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
                    transaction.setAbortedBySiteFailure();
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
    }

    private void abort(int tid) {
        if (transactions.containsKey(tid)) {
            for (DataManager site : sites.values()) {
                site.abort(tid);
            }
        }
    }
}
