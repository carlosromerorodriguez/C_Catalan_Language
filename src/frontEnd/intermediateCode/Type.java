package frontEnd.intermediateCode;

/**
 * Enum that contains the types of the TACEntries.
 */
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

    /**
     * String that contains the type of the TACEntry.
     */
    private final String type;

    /**
     * Constructor of the Type enum.
     * @param name It is the name of the type.
     */
    Type(String name) {
        this.type = name;
    }

    /**
     * Method to get the type of the TACEntry.
     * @return The type of the TACEntry.
     */
    public String getType() {
        return type;
    }
}