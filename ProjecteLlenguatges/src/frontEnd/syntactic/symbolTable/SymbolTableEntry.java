package frontEnd.syntactic.symbolTable;

import java.util.UUID;

public abstract class SymbolTableEntry {
    private UUID id;      // Identificador únic per a cada entrada
    private String name;    // Nom del símbol
    private int line;       // Línia on es troba el símbol

    public SymbolTableEntry(UUID id, String name, int line) {
        this.id = id;
        this.name = name;
        this.line = line;
    }

    public UUID getId() {
        return this.id;
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

