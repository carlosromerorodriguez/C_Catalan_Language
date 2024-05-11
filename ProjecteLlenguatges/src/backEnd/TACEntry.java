package backEnd;

public class TACEntry {
    private String operation;
    private String operand1;
    private String operand2;
    private String destination;
    private String type; // Necessari per "condition" i per tant fer append al seguent bloc creat Ex: if i < 5 goto L2

    public TACEntry(String op, String op1, String op2, String destination, String type) {
        operation = op;
        operand1 = op1;
        operand2 = op2;
        this.destination = destination;
        this.type = type;
    }

    @Override
    public String toString() {
        if(!type.equals("PARAM") && !type.equals("CALL")) {
            return destination + " = " + operand1 + " " + operation + " " + operand2;
        } else {
            return operation + " " + operand2;
        }
    }
}
