package frontEnd.semantic;

import frontEnd.global.ErrorHandler;
import frontEnd.syntactic.symbolTable.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
//import com.udojava.evalex.Expression;


public class SemanticAnalizer {
    public enum Vartype {
        ENTER(4),
        DECIMAL(5.65),
        SIONO(true),
        LLETRES('a'),
        UNASSIGNED(null);

        private final Object value;

        Vartype(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    private SymbolTable symbolTable;
    private ErrorHandler errorHandler;
    private List<String> operators;

    public SemanticAnalizer(SymbolTable symbolTable, ErrorHandler errorHandler) {
        this.symbolTable = symbolTable;
        this.errorHandler = errorHandler;
        this.operators = Arrays.asList(
                "+", "-", "*", "/", "(", ")", "==", ">", ">=", "<", "<="
        );
    }
    private Vartype stringToType(String type){
        switch(type){
            case "enter" -> {
                return Vartype.ENTER;
            }
            case "decimal" -> {
                return Vartype.DECIMAL;
            }
            case "siono" -> {
                return Vartype.SIONO;
            }
            default -> {
                return Vartype.UNASSIGNED;
            }
        }
    }
    public void analizeSymbolTable() {
        analizeScopes(symbolTable.getRootScope());
    }
    private void analizeScopes(Scope currentScope) {
        for (Scope scope : currentScope.getChildScopes()) {
            for (Map.Entry<String, SymbolTableEntry> entry: scope.getSymbolTable().entrySet()){
                if(entry.getValue() instanceof VariableEntry tableEntry){
                    checkVariable(scope, tableEntry);
                }
                else if(entry.getValue() instanceof FunctionEntry tableEntry){
                    checkFunction(scope, tableEntry);
                }
                else if(entry.getValue() instanceof ConditionalEntry tableEntry){
                    checkConditional(scope, tableEntry);
                }

                analizeScopes(scope);
            }
        }
    }
    private void checkConditional(Scope scope, ConditionalEntry condEntry) {
        StringBuilder expression = new StringBuilder();
        for(Object term: condEntry.getCondition()){

        }

        return;

    }

    private void checkFunction(Scope scope, FunctionEntry funcEntry) {
        Vartype functionType = stringToType(funcEntry.getReturnType());
        Vartype newType;
        for(Object term: funcEntry.getReturnValue()) {
            newType = getTermType(term, scope);
            if (newType != Vartype.UNASSIGNED && newType != functionType) {
                this.errorHandler.recordError("Function type mismatch", funcEntry.getLine());
            }
        }
    }

    private void checkVariable(Scope scope, VariableEntry varEntry){
        Vartype currentType = stringToType(varEntry.getType());
        Vartype newType;
        for(Object term: varEntry.getExpression()){
            newType = getTermType(term, scope);
            if(newType != Vartype.UNASSIGNED && newType != currentType){
                this.errorHandler.recordError("Type mismatch", varEntry.getLine());
            }
        }
    }

    private Vartype getTermType(Object term, Scope scope) {
        if(term instanceof Integer) return Vartype.ENTER;
        if(term instanceof Float) return Vartype.DECIMAL;
        if(term instanceof Boolean) return Vartype.SIONO;
        if(term instanceof String value){
            if(this.operators.contains(term)){
                return Vartype.UNASSIGNED;
            }
            return getVariableType(scope, value);
        }
        return Vartype.UNASSIGNED;
    }

    private Vartype getVariableType(Scope scope, String var){
        if(scope == null) return Vartype.UNASSIGNED;
        SymbolTableEntry entry = scope.getSymbolTable().get(var);
        if(entry != null){
            if(entry instanceof VariableEntry variableEntry){
                return stringToType(variableEntry.getType());
            }
        }
        else{
            return getVariableType(scope.getParentScope(), var);
        }
        return Vartype.UNASSIGNED;
    }
}
