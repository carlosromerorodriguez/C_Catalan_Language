package frontEnd.intermediateCode;

/**
 * TACEntry class that contains the operation, the operands and the destination of the TACEntry.
 */
public class TACEntry {
    /**
     * String variable that contains the operation of the TACEntry.
     */
    private final String operation;
    /**
     * String variable that contains the first operand of the TACEntry.
     */
    private final String operand1;
    /**
     * String variable that contains the second operand of the TACEntry.
     */
    private final String operand2;
    /**
     * String variable that contains the destination of the TACEntry.
     */
    private String destination;
    /**
     * Type variable that contains the type of the TACEntry.
     */
    private final Type type; // Necessari per "condition" i per tant fer append al seguent bloc creat Ex: if i < 5 goto L2

    /**
     * Constructor of the TACEntry class.
     * @param op It is the operation of the TACEntry.
     * @param op1 It is the first operand of the TACEntry.
     * @param op2 It is the second operand of the TACEntry.
     * @param destination It is the destination of the TACEntry.
     * @param type It is the type of the TACEntry.
     */
    public TACEntry(String op, String op1, String op2, String destination, Type type) {
        operation = op;
        operand1 = op1;
        operand2 = op2;
        this.destination = destination;
        this.type = type;
    }

    /**
     * Method to get the type of the TACEntry.
     * @return The type of the TACEntry.
     */
    public Type getType() {
        return type;
    }

    /**
     * Method to set the destination of the TACEntry.
     * @param destination It is the destination that is going to be set.
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Method to print the TACEntry into a String format.
     * @return The TACEntry in a String format.
     */
    @Override
    public String toString() {
        if(!type.equals(Type.PARAM) && !type.equals(Type.CALL) && !type.equals(Type.RET) && !type.equals(Type.GOTO) && !type.equals(Type.PRINT)) {
            if(type.equals(Type.CONDITION)) {
                return "IF " + operand1 + " goto " + destination;
            }
            return destination + " = " + operand1 + " " + operation + " " + operand2;
        } else {
            return operation + " " + operand2;
        }
    }

    /**
     * Method to get the operation of the TACEntry.
     * @return The operation of the TACEntry.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Method to get the first operand of the TACEntry.
     * @return The first operand of the TACEntry.
     */
    public String getOperand1() {
        return operand1;
    }

    /**
     * Method to get the second operand of the TACEntry.
     * @return The second operand of the TACEntry.
     */
    public String getOperand2() {
        return operand2;
    }

    /**
     * Method to get the destination of the TACEntry.
     * @return The destination of the TACEntry.
     */
    public String getDestination() {
        return destination;
    }
}
