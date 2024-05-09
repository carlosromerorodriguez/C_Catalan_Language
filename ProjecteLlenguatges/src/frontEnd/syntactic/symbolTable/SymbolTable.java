package frontEnd.syntactic.symbolTable;


import frontEnd.syntactic.Node;

import java.util.HashSet;
import java.util.List;

public class SymbolTable {
    private Scope rootScope;
    private Scope currentScope;
    private Node allTree;

    public SymbolTable() {
        this.rootScope = new Scope();
        this.currentScope = rootScope;
    }


   public Scope getRootScope(){
        return this.rootScope;
   }

   public Scope getCurrentScope() {
        return this.currentScope;
   }

   public List<Scope> getChildScopes() {
        return this.currentScope.getChildScopes();
   }

   public void addScope(){
        Scope newScope = new Scope(this.currentScope);
        this.currentScope.addChildScope(newScope);
        this.currentScope = newScope;
        this.currentScope.setSymbolTable();
   }

   public void enterScope(int i){
        this.currentScope = this.currentScope.getChildScopes().get(i);
   }

   public void leaveScope(){
        if (this.currentScope != this.rootScope) {
            this.currentScope = this.currentScope.getParentScope();
        }
   }

    public void setAllTree(Node allTree) {
        this.allTree = allTree;
    }

    public Node getAllTree() {
        return allTree;
    }

    public void addSymbolEntry(SymbolTableEntry newEntry){
        this.currentScope.addEntry(newEntry);
   }

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

