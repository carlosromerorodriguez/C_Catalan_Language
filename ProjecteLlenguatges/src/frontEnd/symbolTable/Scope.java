package frontEnd.symbolTable;

import frontEnd.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scope {
    private HashMap<String, SymbolTableEntry> symbolTable;
    private Scope parentScope; // Àmbit pare
    private List<Scope> childScopes;
    private Node rootNode;
    private Boolean isMainScope;

    public Scope() {
        this.symbolTable = new HashMap<>();
        this.parentScope = null;
        this.childScopes = new ArrayList<>();
        this.isMainScope = false;
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

    public void addChildScope(Scope new_scope) {
        childScopes.add(new_scope);
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
        for(Map.Entry<String, SymbolTableEntry> entry : this.symbolTable.entrySet()){
            if(entry.getValue() instanceof FunctionEntry){
                return (FunctionEntry) entry.getValue();
            }
        }
        return null;
    }

    public Node getCurrentNode() {
        return this.rootNode;
    }

    public Node getRootNode() {
        return this.rootNode;
    }

    public void setRootNode(Node newNode) {
        this.rootNode = newNode;
    }

    public Map<String, SymbolTableEntry> getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable() {
         this.symbolTable = new HashMap<>();
    }

    public SymbolTableEntry lookup(String name) {
        if (symbolTable.containsKey(name)) {
            return symbolTable.get(name);
        } else if (parentScope != null) {
            return parentScope.lookup(name);
        } else {
            return null;
        }
    }

    public void setIsMainScope(Boolean isMainScope) {
        this.isMainScope = isMainScope;
    }
    public Boolean isConditionalScope() {
        //Si te un coditionalEntry a la taula de simbols es un scope condicional
        for(Map.Entry<String, SymbolTableEntry> entry : this.symbolTable.entrySet()){
            if(entry.getValue() instanceof ConditionalEntry){
                return true;
            }
        }
        return false;
    }
}
