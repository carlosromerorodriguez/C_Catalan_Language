package frontEnd.symbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scope {
    private Map<String, SymbolTableEntry> symbolTable;
    private Scope parentScope; // Àmbit pare (opcional)
    private List<Scope> childScopes;

    public Scope() {
        this.symbolTable = new HashMap<>();
        this.parentScope = null;
        this.childScopes = new ArrayList<>();
    }

    public Scope(Scope parentScope) {
        this.symbolTable = new HashMap<>();
        this.parentScope = parentScope;
        this.childScopes = new ArrayList<>();
    }

    public void addEntry(SymbolTableEntry entry) {
        if (entry == null || entry.getName() == null) {
            throw new IllegalArgumentException("Symbol entry or name cannot be null.");
        }
        symbolTable.put(entry.getName(), entry);
    }

    public SymbolTableEntry getEntry(String name) {
        if (symbolTable.containsKey(name)) {
            return symbolTable.get(name);
        } else if (parentScope != null) {
            return parentScope.getEntry(name);
        } else {
            return null;
        }
    }

    public void addChildScope(Scope parentScope) {
        childScopes.add(new Scope(parentScope));
    }

    public List<Scope> getChildScopes() {
        return this.childScopes;
    }

    public void removeEntry(String name) {
        symbolTable.remove(name);
    }

    public Scope getParentScope(){
        return this.parentScope;
    }

    public FunctionEntry getFunctionEntry(){
        for (Map.Entry<String, SymbolTableEntry> entry : symbolTable.entrySet()){
            SymbolTableEntry value = entry.getValue();
            if(value instanceof FunctionEntry){
                return (FunctionEntry) value;
            }
        }
        return null;
    }
}
