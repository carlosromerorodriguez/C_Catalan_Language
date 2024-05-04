package frontEnd.symbolTable;


import java.util.List;
import java.util.Map;

public class SymbolTable {
    private Scope rootScope;
    private Scope currentScope;

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

   public void addSymbolEntry(SymbolTableEntry newEntry){
        this.currentScope.addEntry(newEntry);
   }
}

