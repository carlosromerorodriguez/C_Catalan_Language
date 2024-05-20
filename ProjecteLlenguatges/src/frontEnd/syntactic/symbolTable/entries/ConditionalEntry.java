package frontEnd.syntactic.symbolTable.entries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Conditional entry.
 */
public class ConditionalEntry extends SymbolTableEntry {
    /**
     * The conditional type.
     */
    private String type;
    /**
     * The Condition expression.
     */
    private List<Object> condition;

    /**
     * Instantiates a new Conditional entry.
     *
     * @param id   the id
     * @param name the name
     * @param line the line
     * @param type the type
     */
    public ConditionalEntry(UUID id, String name, int line, String type) {
        super(id, name, line);
        this.type = type;
        condition = new ArrayList<>();
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Add condition.
     *
     * @param condition the condition
     */
    public void addCondition(Object condition) {
        this.condition.add(condition);
    }

    /**
     * Gets condition.
     *
     * @return the condition
     */
    public List<Object> getCondition() {
        return condition;
    }

    /**
     * To string method.
     *
     * @param depth the depth
     * @return the string
     */
    @Override
    public String toString(int depth) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ConditionalEntry{\n");
        stringBuilder.append("  ".repeat(depth)).append("name=").append(super.getName()).append("\n");
        stringBuilder.append("  ".repeat(depth)).append("type=").append(type).append("\n");
        stringBuilder.append("  ".repeat(depth)).append("line=").append(super.getLine()).append("\n");
        return stringBuilder.toString();
    }

}
