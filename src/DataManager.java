import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
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

    public int getId() {
        return id;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean containsVariable(int vid) {
        return variables.containsKey(vid);
    }

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

    public int read(Transaction.TransactionType t, int ts, Operation o) {
        if (Operation.OperationType.READ.equals(o.getType()) && canRead(t, o)) {
            lockManagers.get(o.getVariableId()).lock(o.getType(), o.getTransactionId(), o.getVariableId());
            if (Transaction.TransactionType.READ_ONLY.equals(t)) {
                return readByReadOnlyTransaction(ts, o, variables.get(o.getVariableId()));
            } else {
                return readByReadWriteTransaction(o, variables.get(o.getVariableId()));
            }
        }
        return 0;
    }

    private int readByReadOnlyTransaction(int ts, Operation o, Variable v) {
        o.setValue(v.getLastCommittedValueBefore(ts));
        return v.getLastCommittedValueBefore(ts);
    }

    private int readByReadWriteTransaction(Operation o, Variable v) {
        if (v.getTransactionIdToCommit() == o.getTransactionId()) {
            o.setValue(v.getValueToCommit());
            return v.getValueToCommit();
        } else {
            o.setValue(v.getLastCommittedValue());
            return v.getLastCommittedValue();
        }
    }

    public boolean canWrite(Transaction.TransactionType t, Operation o) {
        if (!isActive || Transaction.TransactionType.READ_ONLY.equals(t) ||
                !variables.containsKey(o.getVariableId())) {
            return false;
        }
        return lockManagers.get(o.getVariableId()).canAcquireLock(o.getType(), o.getTransactionId());
    }

    public void write(Transaction.TransactionType t, Operation o) {
        if (Operation.OperationType.WRITE.equals(o.getType()) && canWrite(t, o)) {
            lockManagers.get(o.getVariableId()).lock(o.getType(), o.getTransactionId(), o.getVariableId());
            Variable variable = variables.get(o.getVariableId());
            variable.setValueToCommit(o.getValue());
            variable.setTransactionIdToCommit(o.getTransactionId());
        }
    }

    public void abort(int tid) {
        for (LockManager lockManager : lockManagers.values()) {
            lockManager.unlock(tid);
        }
    }

    public void commit(int tid, int ts) {
        for (LockManager lockManager : lockManagers.values()) {
            if (lockManager.isWriteLockedBy(tid)) {
                variables.get(lockManager.getVariableId()).commit(ts);
            }
            lockManager.unlock(tid);
        }
    }

    public void dump() {
        List<String> variableStrings = new ArrayList<>();
        for (int i = 1; i <= VARIABLE_COUNT; i++) {
            if (variables.containsKey(i)) {
                variableStrings.add(variables.get(i).toString());
            }
        }
        System.out.println(String.format("site %d - %s", id, String.join(", ", variableStrings)));
    }

    public void fail() {
        isActive = false;
        for (Variable variable : variables.values()) {
            variable.fail();
        }
        for (LockManager lockManager : lockManagers.values()) {
            lockManager.unlockAll();
        }
    }

    public void recover() {
        isActive = true;
        for (Variable variable : variables.values()) {
            variable.recover();
        }
    }
}
