package frontEnd.syntactic.symbolTable;

import frontEnd.syntactic.Node;
import frontEnd.syntactic.symbolTable.entries.FunctionEntry;
import frontEnd.syntactic.symbolTable.entries.SymbolTableEntry;
import frontEnd.syntactic.symbolTable.entries.VariableEntry;

import java.util.*;

/**
 * The Scope.
 */
public class Scope {
    /**
     * The Symbol table.
     */
    private Map<String, SymbolTableEntry> symbolTable;
    /**
     * The Parent scope.
     */
    private Scope parentScope; // Àmbit pare
    /**
     * The Child scopes.
     */
    private List<Scope> childScopes;
    /**
     * The Root node.
     */
    private Node rootNode;
    /**
     * Indicates if this scope is the main one.
     */
    private Boolean isMainScope;

    /**
     * Instantiates a new Scope.
     */
    public Scope() {
        this.symbolTable = new LinkedHashMap<>();
        this.parentScope = null;
        this.childScopes = new ArrayList<>();
        this.isMainScope = false;
    }

    /**
     * Instantiates a new Scope.
     *
     * @param parentScope the parent scope
     */
    public Scope(Scope parentScope) {
        this.symbolTable = new HashMap<>();
        this.parentScope = parentScope;
        this.childScopes = new ArrayList<>();
    }

    /**
     * Adds an entry.
     *
     * @param entry the entry
     */
    public void addEntry(SymbolTableEntry entry) {
        if (entry == null || entry.getName() == null) {
            throw new IllegalArgumentException("Symbol entry or name cannot be null.");
        }
        symbolTable.put(entry.getName(), entry);
    }

    /**
     * Gets entry.
     *
     * @param name the name
     * @return the entry
     */
    public SymbolTableEntry getEntry(String name) {
        if (symbolTable.containsKey(name)) {
            return symbolTable.get(name);
        } else if (parentScope != null) {
            return parentScope.getEntry(name);
        } else {
            return null;
        }
    }

    /**
     * Adds a child scope.
     *
     * @param new_scope the new scope
     */
    public void addChildScope(Scope new_scope) {
        childScopes.add(new_scope);
    }

    /**
     * Gets child scopes.
     *
     * @return the child scopes
     */
    public List<Scope> getChildScopes() {
        return this.childScopes;
    }

    /**
     * Gets parent scope.
     *
     * @return the scope
     */
    public Scope getParentScope(){
        return this.parentScope;
    }

    /**
     * Gets function entry of scope.
     *
     * @return the function entry
     */
    public FunctionEntry getFunctionEntry(){
        for(Map.Entry<String, SymbolTableEntry> entry : this.symbolTable.entrySet()){
            if(entry.getValue() instanceof FunctionEntry){
                return (FunctionEntry) entry.getValue();
            }
        }
        return null;
    }

    /**
     * Gets root node.
     *
     * @return the root node
     */
    public Node getRootNode() {
        return this.rootNode;
    }

    /**
     * Sets root node.
     *
     * @param newNode the new node
     */
    public void setRootNode(Node newNode) {
        this.rootNode = newNode;
    }

    /**
     * Gets symbol table.
     *
     * @return the symbol table
     */
    public Map<String, SymbolTableEntry> getSymbolTable() {
        return symbolTable;
    }

    /**
     * Sets symbol table.
     */
    public void setSymbolTable() {
         this.symbolTable = new HashMap<>();
    }

    /**
     * Lookup symbol table entry.
     *
     * @param name the name
     * @return the symbol table entry
     */
    public SymbolTableEntry lookup(String name) {
        if (symbolTable.containsKey(name)) {
            return symbolTable.get(name);
        } else if (parentScope != null) {
            // Make a copy of the parent's return to not affect the reference
            SymbolTableEntry parentEntry = parentScope.lookup(name);
            if (parentEntry instanceof VariableEntry parentVariableEntry) {
                return new VariableEntry(parentVariableEntry.getId(), parentVariableEntry.getName(), parentVariableEntry.getLine(), parentVariableEntry.getType(), parentVariableEntry.getIsArgument());
            }
            return parentEntry; //Create a copy of the parent's entry
        } else {
            return null;
        }
    }

    /**
     * Sets is main scope.
     *
     * @param isMainScope the is main scope
     */
    public void setIsMainScope(Boolean isMainScope) {
        this.isMainScope = isMainScope;
    }

    /**
     * Entry exists boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public Boolean entryExists(String name) {
        return symbolTable.containsKey(name);
    }


    /**
     * To string.
     *
     * @param depth the depth
     * @return the string
     */
    public String toString(int depth){
        StringBuilder output = new StringBuilder();
        for(Map.Entry<String, SymbolTableEntry> entry : this.symbolTable.entrySet()){
            output.append("  ".repeat(depth)).append(entry.getValue().toString(depth)).append("\n");
        }
        output.append("\n");
        return output.toString();
    }

}
