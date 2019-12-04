/**
 * This class represents a lock for a variable
 * @version 12/02/2019
 * @author Xinyi Liu, Ming Xu
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

    /**
     * Gets transactionId.
     * @return transactionId
     */
    public int getTransactionId() {
        return transactionId;
    }

    /**
     * Gets variableId.
     * @return variableId
     */
    public int getVariableId() {
        return variableId;
    }

    /**
     * Gets lock type.
     * @return type
     */
    public LockType getType() {
        return type;
    }

    /**
     * Promotes lock to write lock.
     */
    public void promoteLockType() {
        type = LockType.WRITE_LOCK;
    }
}
