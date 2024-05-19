package backEnd;

public enum Type{
    PARAM ("PARAM"),
    CALL ("CALL"),
    CALL_EXP ("CALL"),
    GOTO ("GOTO"),
    CONDITION ("CONDITION"),
    ADD("ADD"),
    SUB("SUB"),
    MUL("MUL"),
    DIV("DIV"),
    AND("AND"),
    OR("OR"),
    EQ("="),
    EQU(""),
    NE("NE"),
    LT("LT"),
    LE("LE"),
    GT("GT"),
    GE("GE"),
    RET("RET"),
    UNDEFINED("UNDEFINED"),
    PRINT("PRINT"),
    ;

    private final String type;

    Type(String name) {
        this.type = name;
    }

    public String getType() {
        return type;
    }
}