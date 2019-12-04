import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents variable with value to commit and committed values.
 * @version 12/02/2019
 * @author Xinyi Liu, Ming Xu
 */
public class Variable {
    private int id;
    private int valueToCommit;
    private int transactionIdToCommit;
    private int lastCommittedValue;
    private Map<Integer, Integer> committedValues;  // <timestamp, committedValue>
    private boolean isReadable;

    public Variable(int vid) {
        id = vid;
        valueToCommit = 10 * vid;
        lastCommittedValue = 10 * vid;
        committedValues = new LinkedHashMap<>();
        committedValues.put(0, lastCommittedValue);
        isReadable = true;
    }

    /**
     * Gets variableId.
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets value to commit.
     * @return valueToCommit
     */
    public int getValueToCommit() {
        return valueToCommit;
    }

    /**
     * Gets transactionId of value to commit.
     * @return transactionId
     */
    public int getTransactionIdToCommit() {
        return transactionIdToCommit;
    }

    /**
     * Gets value from last commit.
     * @return getLastCommittedValue
     */
    public int getLastCommittedValue() {
        return lastCommittedValue;
    }

    /**
     * Gets value from last commit before a timestamp.
     * @param ts timestamp
     * @return committedValue before timestamp
     */
    public int getLastCommittedValueBefore(int ts) {
        int committedValue = 0;
        for (int timestamp : committedValues.keySet()) {
            if (timestamp <= ts) {
                committedValue = committedValues.get(timestamp);
            } else {
                break;
            }
        }
        return committedValue;
    }

    /**
     * Returns whether this variable is readable.
     * @return boolean
     */
    public boolean isReadable() {
        return isReadable;
    }

    /**
     * Sets value to commit.
     * @param v valueToCommit
     */
    public void setValueToCommit(int v) {
        valueToCommit = v;
    }

    /**
     * Sets transactionId of value to commit.
     * @param tid transactionId
     */
    public void setTransactionIdToCommit(int tid) {
        transactionIdToCommit = tid;
    }

    /**
     * Performs when the site fails.
     */
    public void fail() {
        isReadable = false;
    }

    /**
     * Performs when the site recovers.
     */
    public void recover() {
        if (id % 2 != 0) {
            isReadable = true;
        }
    }

    /**
     * Commits a value.
     * @param ts timestamp
     */
    public void commit(int ts) {
        lastCommittedValue = valueToCommit;
        committedValues.put(ts, lastCommittedValue);
        isReadable = true;
    }

    @Override
    public String toString() {
        return String.format("x%d: %d", id, lastCommittedValue);
    }
}
