import java.util.ArrayList;
import java.util.List;

/**
 * @author Xinyi Liu, Ming Xu
 */
public class LockManager {
    private int variableId;
    private List<Lock> locks;

    public LockManager(int vid) {
        variableId = vid;
        locks = new ArrayList<>();
    }

    public int getVariableId() {
        return variableId;
    }

    public boolean canAcquireLock(Operation.OperationType t, int tid) {
        if (Operation.OperationType.READ.equals(t)) {
            return canAcquireReadLock(tid);
        } else {
            return canAcquireWriteLock(tid);
        }
    }

    private boolean canAcquireReadLock(int tid) {
        Lock writeLock = getWriteLock();
        if (writeLock != null && writeLock.getTransactionId() != tid) {
            return false;
        }
        return true;
    }

    private boolean canAcquireWriteLock(int tid) {
        if (locks.size() > 1) {
            return false;
        }
        Lock writeLock = getWriteLock();
        if (writeLock != null && writeLock.getTransactionId() != tid) {
            return false;
        }
        return true;
    }

    public void lock(Operation.OperationType t, int tid, int vid) {
        if (Operation.OperationType.READ.equals(t)) {
            lockForRead(tid, vid);
        } else {
            lockForWrite(tid, vid);
        }
    }

    private void lockForRead(int tid, int vid) {
        if (canAcquireReadLock(tid)) {
            Lock transactionLock = getLock(tid);
            if (transactionLock == null) {
                locks.add(new Lock(tid, vid, Lock.LockType.READ_LOCK));
            }
        }
    }

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

    public void unlock(int tid) {
        locks.removeIf(l -> l.getTransactionId() == tid);
    }

    public void unlockAll() {
        locks.clear();
    }

    public boolean isWriteLockedBy(int tid) {
        Lock writeLock = getWriteLock();
        if (writeLock != null && writeLock.getTransactionId() == tid) {
            return true;
        }
        return false;
    }

    private Lock getWriteLock() {
        for (Lock lock : locks) {
            if (Lock.LockType.WRITE_LOCK.equals(lock.getType())) {
                return lock;
            }
        }
        return null;
    }

    private Lock getLock(int tid) {
        for (Lock lock : locks) {
            if (lock.getTransactionId() == tid) {
                return lock;
            }
        }
        return null;
    }
}
