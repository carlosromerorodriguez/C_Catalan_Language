package frontEnd.syntactic.symbolTable.entries;

import java.util.UUID;

/**
 * The Symbol table entry.
 */
public abstract class SymbolTableEntry {
    /**
     * The UUID for entry.
     */
    private UUID id;
    /**
     * The symbol name.
     */
    private String name;
    /**
     * The Line where symbol is located.
     */
    private int line;

    /**
     * Instantiates a new Symbol table entry.
     *
     * @param id   the id
     * @param name the name
     * @param line the line
     */
    public SymbolTableEntry(UUID id, String name, int line) {
        this.id = id;
        this.name = name;
        this.line = line;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets line.
     *
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * To string method.
     *
     * @param depth the depth
     * @return the string
     */
    public String toString(int depth) {
        return null;
    }

    /**
     * Sets line.
     *
     * @param line the line
     */
    public void setLine(int line) {
        this.line = line;
    }
}

