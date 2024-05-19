package backEnd;

public class TACEntry {
    private String operation;
    private String operand1;
    private String operand2;
    private String destination;
    private Type type; // Necessari per "condition" i per tant fer append al seguent bloc creat Ex: if i < 5 goto L2

    public TACEntry(String op, String op1, String op2, String destination, Type type) {
        operation = op;
        operand1 = op1;
        operand2 = op2;
        this.destination = destination;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

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

    public String getOperation() {
        return operation;
    }

    public String getOperand1() {
        return operand1;
    }

    public String getOperand2() {
        return operand2;
    }

    public String getDestination() {
        return destination;
    }
}
