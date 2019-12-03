import java.util.HashSet;
import java.util.Set;

/**
 * @author Xinyi Liu, Ming Xu
 */
public class Transaction {
    public enum TransactionType {
        READ_ONLY, READ_WRITE
    }

    private int id;
    private int timestamp;
    private TransactionType type;
    private boolean isAbortedBySiteFailure;
    private boolean isAbortedByDeadlock;
    private Set<Integer> accessedSites;

    public Transaction(int tid, int ts, TransactionType t) {
        id = tid;
        timestamp = ts;
        type = t;
        isAbortedBySiteFailure = false;
        isAbortedByDeadlock = false;
        accessedSites = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public TransactionType getType() {
        return type;
    }

    public boolean isAborted() {
        return isAbortedBySiteFailure || isAbortedByDeadlock;
    }

    public boolean isAbortedBySiteFailure() {
        return isAbortedBySiteFailure;
    }

    public boolean isAbortedByDeadlock() {
        return isAbortedByDeadlock;
    }

    public void setAbortedBySiteFailure() {
        isAbortedBySiteFailure = true;
    }

    public void setAbortedByDeadlock() {
        isAbortedByDeadlock = true;
    }

    public Set<Integer> getAccessedSites() {
        return accessedSites;
    }

    public void addAccessedSite(int sid) {
        accessedSites.add(sid);
    }

    public boolean hasAccessedSite(int sid) {
        return accessedSites.contains(sid);
    }
}
