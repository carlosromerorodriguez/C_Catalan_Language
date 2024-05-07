package backEnd;

import frontEnd.syntactic.symbolTable.Scope;
import frontEnd.syntactic.symbolTable.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class TAC {
    private List<TacExpression> tacs;
    private SymbolTable symbolTable;

    public TAC(SymbolTable symbolTable) {
        this.tacs = new ArrayList<>();
        this.symbolTable = symbolTable;
    }

    public void buildTAC(){
        System.out.println("-------------TAC------------------" + "\n\n");
        addTacs(this.symbolTable.getRootScope(), 0);
    }

    private void addTacs(Scope scope, int depth){
        for (Scope TacScope: scope.getChildScopes()){
            System.out.println("\t".repeat(depth*5) + "-" + TacScope);
            addTacs(TacScope, depth + 1);
        }
    }
}
