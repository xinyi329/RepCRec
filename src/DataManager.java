import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides the storage of variables and manages their locks for a certain site.
 * @version 12/03/2019
 * @author Xinyi Liu, Ming Xu
 */
public class DataManager {
    public static final int VARIABLE_COUNT = 20;

    private int id;
    private boolean isActive;
    private Map<Integer, Variable> variables;       // <variableId, variable>
    private Map<Integer, LockManager> lockManagers; // <variableId, lockManager>

    public DataManager(int sid) {
        id = sid;
        isActive = true;
        variables = new HashMap<>();
        lockManagers = new HashMap<>();
        for (int i = 1; i <= VARIABLE_COUNT; i++) {
            if (i % 2 == 0 || i % 10 + 1 == id) {
                variables.put(i, new Variable(i));
                lockManagers.put(i, new LockManager(i));
            }
        }
    }

    /**
     * Gets siteId.
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns whether the site is active.
     * @return boolean
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Returns whether the site is holding a variable.
     * @param vid variableId
     * @return boolean
     */
    public boolean containsVariable(int vid) {
        return variables.containsKey(vid);
    }

    /**
     * Returns whether a transaction can read a variable.
     * @param t transactionType
     * @param o operation
     * @return boolean
     */
    public boolean canRead(Transaction.TransactionType t, Operation o) {
        if (!isActive || !variables.containsKey(o.getVariableId())) {
            return false;
        }
        if (!variables.get(o.getVariableId()).isReadable()) {
            return false;
        }
        if (Transaction.TransactionType.READ_WRITE.equals(t)) {
            return lockManagers.get(o.getVariableId()).canAcquireLock(o.getType(), o.getTransactionId());
        }
        return true;
    }

    /**
     * Reads a value.
     * @param t transactionType
     * @param ts timestamp
     * @param o operation
     * @return value
     */
    public int read(Transaction.TransactionType t, int ts, Operation o) {
        if (Operation.OperationType.READ.equals(o.getType()) && canRead(t, o)) {
            if (Transaction.TransactionType.READ_ONLY.equals(t)) {
                return readByReadOnlyTransaction(ts, o, variables.get(o.getVariableId()));
            } else {
                lockManagers.get(o.getVariableId()).lock(o.getType(), o.getTransactionId(), o.getVariableId());
                return readByReadWriteTransaction(o, variables.get(o.getVariableId()));
            }
        }
        return 0;
    }

    /**
     * Reads a value for a read-only transaction.
     * @param ts timestamp
     * @param o operation
     * @param v variable
     * @return value
     */
    private int readByReadOnlyTransaction(int ts, Operation o, Variable v) {
        o.setValue(v.getLastCommittedValueBefore(ts));
        return v.getLastCommittedValueBefore(ts);
    }

    /**
     * Reads a value for a read-write transaction.
     * @param o operation
     * @param v variable
     * @return value
     */
    private int readByReadWriteTransaction(Operation o, Variable v) {
        if (v.getTransactionIdToCommit() == o.getTransactionId()) {
            o.setValue(v.getValueToCommit());
            return v.getValueToCommit();
        } else {
            o.setValue(v.getLastCommittedValue());
            return v.getLastCommittedValue();
        }
    }

    /**
     * Returns whether a transaction can write to a variable.
     * @param t transactionType
     * @param o operation
     * @return boolean
     */
    public boolean canWrite(Transaction.TransactionType t, Operation o) {
        if (!isActive || Transaction.TransactionType.READ_ONLY.equals(t) ||
                !variables.containsKey(o.getVariableId())) {
            return false;
        }
        return lockManagers.get(o.getVariableId()).canAcquireLock(o.getType(), o.getTransactionId());
    }

    /**
     * Writes to a variable.
     * @param t transactionType
     * @param o operation
     */
    public void write(Transaction.TransactionType t, Operation o) {
        if (Operation.OperationType.WRITE.equals(o.getType()) && canWrite(t, o)) {
            lockManagers.get(o.getVariableId()).lock(o.getType(), o.getTransactionId(), o.getVariableId());
            Variable variable = variables.get(o.getVariableId());
            variable.setValueToCommit(o.getValue());
            variable.setTransactionIdToCommit(o.getTransactionId());
        }
    }

    /**
     * Gets transactionIds of lock holders on a variable.
     * @param vid variableId
     * @return transactionIds of lock holders
     */
    public List<Integer> getLockHolders(int vid) {
        if (lockManagers.containsKey(vid)) {
            return lockManagers.get(vid).getLockHolders();
        }
        return new ArrayList<>();
    }

    /**
     * Aborts a transaction.
     * @param tid transactionId
     */
    public void abort(int tid) {
        for (LockManager lockManager : lockManagers.values()) {
            lockManager.unlock(tid);
        }
    }

    /**
     * Commits a transaction.
     * @param tid transactionId
     * @param ts timestamp
     */
    public void commit(int tid, int ts) {
        for (LockManager lockManager : lockManagers.values()) {
            if (lockManager.isWriteLockedBy(tid)) {
                variables.get(lockManager.getVariableId()).commit(ts);
            }
            lockManager.unlock(tid);
        }
    }

    /**
     * Gives the committed values of all copies of all variables at this site.
     */
    public void dump() {
        List<String> variableStrings = new ArrayList<>();
        for (int i = 1; i <= VARIABLE_COUNT; i++) {
            if (variables.containsKey(i)) {
                variableStrings.add(variables.get(i).toString());
            }
        }
        System.out.println(String.format("site %d - %s", id, String.join(", ", variableStrings)));
    }

    /**
     * Fails this site.
     */
    public void fail() {
        isActive = false;
        for (Variable variable : variables.values()) {
            variable.fail();
        }
        for (LockManager lockManager : lockManagers.values()) {
            lockManager.unlockAll();
        }
    }

    /**
     * Recovers this site.
     */
    public void recover() {
        isActive = true;
        for (Variable variable : variables.values()) {
            variable.recover();
        }
    }
}
