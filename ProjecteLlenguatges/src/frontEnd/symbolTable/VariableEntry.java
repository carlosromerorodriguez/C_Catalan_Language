package frontEnd.symbolTable;

public class VariableEntry extends SymbolTableEntry {
    private String type;
    private Object value;
    private Boolean isArgument = false;

    public VariableEntry(String id, String name, int line, String type, Object value, Boolean isArgument) {
        super(id, name, line);
        this.type = type;
        this.value = value;
        this.isArgument = isArgument;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "VariableEntry{" +
                "name='" + getName() + '\'' +
                ", type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}

