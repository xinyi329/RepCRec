/**
 * @author Xinyi Liu
 */
public class Lock {
    public enum LockType {
        READ_LOCK, WRITE_LOCK
    }

    private int transactionId;
    private int variableId;
    private LockType type;

    public Lock(int tid, int vid, LockType t) {
        transactionId = tid;
        variableId = vid;
        type = t;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getVariableId() {
        return variableId;
    }

    public LockType getType() {
        return type;
    }

    public void promoteLockType() {
        type = LockType.WRITE_LOCK;
    }
}
