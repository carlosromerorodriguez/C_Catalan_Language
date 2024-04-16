package frontEnd.symbolTable;

public abstract class SymbolTableEntry {
    private String id;      // Identificador únic per a cada entrada
    private String name;    // Nom del símbol
    private int line;       // Línia on es troba el símbol

    public SymbolTableEntry(String id, String name, int line) {
        this.id = id;
        this.name = name;
        this.line = line;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SymbolTableEntry{name='" + name + "', scope='" + id + "', line='" + line + "'}";
    }
}

