/**
 * @author Xinyi Liu, Ming Xu
 */
public class Operation {
    public enum OperationType {
        READ, WRITE
    }

    private int transactionId;
    private int variableId;
    private OperationType type;
    private int value;

    public Operation(int tid, int vid, OperationType t, int v) {
        transactionId = tid;
        variableId = vid;
        type = t;
        value = v;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getVariableId() {
        return variableId;
    }

    public OperationType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int v) {
        if (OperationType.READ.equals(type)) {
            value = v;
        }
    }
}
