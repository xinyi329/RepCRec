import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class maintains locks for a single variable on a certain site.
 * @version 12/03/2019
 * @author Xinyi Liu, Ming Xu
 */
public class LockManager {
    private int variableId;
    private List<Lock> locks;

    public LockManager(int vid) {
        variableId = vid;
        locks = new ArrayList<>();
    }

    /**
     * Gets variableId.
     * @return variableId
     */
    public int getVariableId() {
        return variableId;
    }

    /**
     * Returns whether a transaction can acquire a lock.
     * @param t operationType
     * @param tid transactionId
     * @return boolean
     */
    public boolean canAcquireLock(Operation.OperationType t, int tid) {
        if (Operation.OperationType.READ.equals(t)) {
            return canAcquireReadLock(tid);
        } else {
            return canAcquireWriteLock(tid);
        }
    }

    /**
     * Returns whether a transaction can acquire a read lock.
     * @param tid transactionId
     * @return boolean
     */
    private boolean canAcquireReadLock(int tid) {
        Lock writeLock = getWriteLock();
        if (writeLock != null && writeLock.getTransactionId() != tid) {
            return false;
        }
        return true;
    }

    /**
     * Returns whether a transaction can acquire a write lock.
     * @param tid transactionId
     * @return boolean
     */
    private boolean canAcquireWriteLock(int tid) {
        if (locks.size() > 1) {
            return false;
        } else if (locks.size() == 1 && getLock(tid) == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Adds lock on a variable.
     * @param t operationType
     * @param tid transactionId
     * @param vid variableId
     */
    public void lock(Operation.OperationType t, int tid, int vid) {
        if (Operation.OperationType.READ.equals(t)) {
            lockForRead(tid, vid);
        } else {
            lockForWrite(tid, vid);
        }
    }

    /**
     * Adds read lock on a variable.
     * @param tid transactionId
     * @param vid variableId
     */
    private void lockForRead(int tid, int vid) {
        if (canAcquireReadLock(tid)) {
            Lock transactionLock = getLock(tid);
            if (transactionLock == null) {
                locks.add(new Lock(tid, vid, Lock.LockType.READ_LOCK));
            }
        }
    }

    /**
     * Adds write lock on a variable.
     * @param tid transactionId
     * @param vid variableId
     */
    private void lockForWrite(int tid, int vid) {
        if (canAcquireWriteLock(tid)) {
            Lock transactionLock = getLock(tid);
            if (transactionLock == null) {
                locks.add(new Lock(tid, vid, Lock.LockType.WRITE_LOCK));
            } else {
                transactionLock.promoteLockType();
            }
        }
    }

    /**
     * Releases locks of a transaction.
     * @param tid transactionId.
     */
    public void unlock(int tid) {
        locks.removeIf(l -> l.getTransactionId() == tid);
    }

    /**
     * Release all locks.
     */
    public void unlockAll() {
        locks.clear();
    }

    /**
     * Returns whether a transaction is holding a write lock.
     * @param tid transactionId
     * @return boolean
     */
    public boolean isWriteLockedBy(int tid) {
        Lock writeLock = getWriteLock();
        if (writeLock != null && writeLock.getTransactionId() == tid) {
            return true;
        }
        return false;
    }

    /**
     * Gets all transactions that hold lock on this variable.
     * @return transactionIds of lock holders
     */
    public List<Integer> getLockHolders() {
        return locks.stream().map(lock -> lock.getTransactionId()).collect(Collectors.toList());
    }

    /**
     * Gets write lock if exists.
     * @return boolean
     */
    private Lock getWriteLock() {
        for (Lock lock : locks) {
            if (Lock.LockType.WRITE_LOCK.equals(lock.getType())) {
                return lock;
            }
        }
        return null;
    }

    /**
     * Gets the lock a transaction holds if exists.
     * @param tid transactionId
     * @return lock
     */
    private Lock getLock(int tid) {
        for (Lock lock : locks) {
            if (lock.getTransactionId() == tid) {
                return lock;
            }
        }
        return null;
    }
}
