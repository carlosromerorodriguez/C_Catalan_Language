package global.symbolTable;


import frontEnd.syntactic.Node;
import global.symbolTable.entries.SymbolTableEntry;

import java.util.List;

/**
 * The Symbol table.
 */
public class SymbolTable {
    /**
     * The Root scope.
     */
    private Scope rootScope;
    /**
     * The Current scope.
     */
    private Scope currentScope;
    /**
     * All tree.
     */
    private Node allTree;

    /**
     * Instantiates a new Symbol table.
     */
    public SymbolTable() {
        this.rootScope = new Scope();
        this.currentScope = rootScope;
    }

    /**
     * Get root scope scope.
     *
     * @return the scope
     */
    public Scope getRootScope(){
        return this.rootScope;
   }

    /**
     * Gets current scope.
     *
     * @return the current scope
     */
    public Scope getCurrentScope() {
        return this.currentScope;
   }

    /**
     * Gets child scopes.
     *
     * @return the child scopes
     */
    public List<Scope> getChildScopes() {
        return this.currentScope.getChildScopes();
   }

    /**
     * Adds a new scope.
     */
    public void addScope(){
        Scope newScope = new Scope(this.currentScope);
        this.currentScope.addChildScope(newScope);
        this.currentScope = newScope;
        this.currentScope.setSymbolTable();
   }


    /**
     * Leaves current scope.
     */
    public void leaveScope(){
        if (this.currentScope != this.rootScope) {
            this.currentScope = this.currentScope.getParentScope();
        }
   }

    /**
     * Sets all tree.
     *
     * @param allTree the all tree
     */
    public void setAllTree(Node allTree) {
        this.allTree = allTree;
    }

    /**
     * Gets all tree.
     *
     * @return the all tree
     */
    public Node getAllTree() {
        return allTree;
    }

    /**
     * Adds a symbol entry to current scope.
     *
     * @param newEntry the new entry
     */
    public void addSymbolEntry(SymbolTableEntry newEntry){
        this.currentScope.addEntry(newEntry);
   }

    /**
     * To string method.
     *
     * @return the string
     */
   @Override
   public String toString(){
       System.out.println("-------------SYMBOL TABLE-------------");
       return printTable(rootScope, 0);
   }
    private String printTable(Scope scope, int depth){
        StringBuilder stringBuilder = new StringBuilder();
        for (Scope TacScope: scope.getChildScopes()){
            stringBuilder.append("--".repeat((depth * 5) + 1)).append(TacScope.toString((depth * 5) + 1)).append("\n");
            stringBuilder.append(printTable(TacScope, depth + 1));
        }
        return stringBuilder.toString();
    }
}

