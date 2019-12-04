/**
 * This class represents a read or write operation.
 * @version 12/03/2019
 * @author Xinyi Liu, Ming Xu
 */
public class Operation {
    public enum OperationType {
        READ, WRITE
    }

    private int timestamp;
    private int transactionId;
    private int variableId;
    private OperationType type;
    private int value;

    public Operation(int ts, int tid, int vid, OperationType t, int v) {
        timestamp = ts;
        transactionId = tid;
        variableId = vid;
        type = t;
        value = v;
    }

    /**
     * Gets timestamp.
     * @return timestamp.
     */
    public int getTimestamp() {
        return timestamp;
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
     * @return variableId.
     */
    public int getVariableId() {
        return variableId;
    }

    /**
     * Gets operation type.
     * @return type
     */
    public OperationType getType() {
        return type;
    }

    /**
     * Gets value.
     * @return value
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets value.
     * @param v value
     */
    public void setValue(int v) {
        if (OperationType.READ.equals(type)) {
            value = v;
        }
    }
}
