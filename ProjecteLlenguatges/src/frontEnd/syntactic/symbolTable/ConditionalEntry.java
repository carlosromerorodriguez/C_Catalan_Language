package frontEnd.syntactic.symbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConditionalEntry extends SymbolTableEntry{
    private String type; // Que es si es un if, else, while, for
    private List<Object> condition;

    public ConditionalEntry(UUID id, String name, int line, String type) {
        super(id, name, line);
        this.type = type;
        condition = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public void addCondition(Object condition) {
        this.condition.add(condition);
    }

    public List<Object> getCondition() {
        return condition;
    }
    @Override
    public String toString() {
        return "VariableEntry{\n\tname='" + super.getName() +"\n\ttipus= "+ type + "'\n\tline='" + super.getLine() + "'\n\tcondition='" + condition + "'}\n";
    }
}
