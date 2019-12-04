import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a transaction.
 * @version 12/03/2019
 * @author Xinyi Liu, Ming Xu
 */
public class Transaction {
    public enum TransactionType {
        READ_ONLY, READ_WRITE
    }

    private int id;
    private int timestamp;
    private TransactionType type;
    private boolean isBlocked;
    private boolean isAborted;
    private Set<Integer> accessedSites;

    public Transaction(int tid, int ts, TransactionType t) {
        id = tid;
        timestamp = ts;
        type = t;
        isBlocked = false;
        isAborted = false;
        accessedSites = new HashSet<>();
    }

    /**
     * Gets transactionId.
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Get timestamp of start.
     * @return timestamp
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * Get transaction type.
     * @return type
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Returns whether this transaction is blocked or not.
     * @return boolean
     */
    public boolean isBlocked() {
        return isBlocked;
    }

    /**
     * Sets the transaction to be blocked.
     */
    public void block() {
        isBlocked = true;
    }

    /**
     * Sets the transaction to be unblocked.
     */
    public void unblock() {
        isBlocked = false;
    }

    /**
     * Returns whether this transaction is aborted by accessing a down site or not.
     * @return boolean
     */
    public boolean isAborted() {
        return isAborted;
    }

    /**
     * Sets this transaction to be aborted.
     */
    public void setAborted() {
        isAborted = true;
    }

    /**
     * Gets the sites that this transaction accessed.
     * @return accessedSites.
     */
    public Set<Integer> getAccessedSites() {
        return accessedSites;
    }

    /**
     * Adds a accessed site.
     * @param sid siteId
     */
    public void addAccessedSite(int sid) {
        accessedSites.add(sid);
    }

    /**
     * Returns whether this transaction has accessed a certain site.
     * @param sid siteId
     * @return boolean
     */
    public boolean hasAccessedSite(int sid) {
        return accessedSites.contains(sid);
    }
}
