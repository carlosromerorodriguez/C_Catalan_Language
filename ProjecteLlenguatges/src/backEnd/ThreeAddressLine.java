package backEnd;

import frontEnd.lexic.Token;

public class ThreeAddressLine {
    private final Token operator;
    private final Object argument1;
    private final Object argument2;

    public ThreeAddressLine(Token operator, Object argument1, Object argument2) {
        this.operator = operator;
        this.argument1 = argument1;
        this.argument2 = argument2;
    }

    public Token getOperator() {
        return this.operator;
    }

    public Object getArgument1() {
        return argument1;
    }

    public Object getArgument2() {
        return argument2;
    }
}
