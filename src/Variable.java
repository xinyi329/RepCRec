import java.util.LinkedHashMap;
import java.util.Map;

/**
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

    public int getId() {
        return id;
    }

    public int getValueToCommit() {
        return valueToCommit;
    }

    public int getTransactionIdToCommit() {
        return transactionIdToCommit;
    }

    public int getLastCommittedValue() {
        return lastCommittedValue;
    }

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

    public boolean isReadable() {
        return isReadable;
    }

    public void setValueToCommit(int v) {
        valueToCommit = v;
    }

    public void setTransactionIdToCommit(int tid) {
        transactionIdToCommit = tid;
    }

    public void fail() {
        isReadable = false;
    }

    public void recover() {
        if (id % 2 != 0) {
            isReadable = true;
        }
    }

    public void commit(int timestamp) {
        lastCommittedValue = valueToCommit;
        committedValues.put(timestamp, lastCommittedValue);
        isReadable = true;
    }

    @Override
    public String toString() {
        return String.format("x%d: %d", id, lastCommittedValue);
    }
}
