package frontEnd.semantic;

import global.errors.ErrorHandler;
import global.symbolTable.*;
import global.symbolTable.entries.*;

import java.util.*;

/**
 * The SemanticAnalizer class is responsible for checking the semantic correctness of the code based on the symbol table.
 */
public class SemanticAnalizer {
    /**
     * The enum Vartype.
     */
    public enum Vartype {
        /**
         * Enter vartype.
         */
        ENTER,
        /**
         * Decimal vartype.
         */
        DECIMAL,
        /**
         * Siono vartype.
         */
        SIONO,
        /**
         * Lletres vartype.
         */
        LLETRES,
        /**
         * Unassigned vartype.
         */
        UNASSIGNED,
        /**
         * Res vartype.
         */
        RES
    }

    /**
     * The Symbol table.
     */
    private SymbolTable symbolTable;

    /**
     * The Error handler to record the semantic errors.
     */
    private ErrorHandler errorHandler;

    /**
     * Operators list.
     */
    private List<String> operators;

    /**
     * Instantiates a new Semantic analizer.
     *
     * @param symbolTable  the symbol table
     * @param errorHandler the error handler
     */
    public SemanticAnalizer(SymbolTable symbolTable, ErrorHandler errorHandler) {
        this.symbolTable = symbolTable;
        this.errorHandler = errorHandler;
        this.operators = Arrays.asList(
                "+", "-", "*", "/", "(", ")", "==", ">", ">=", "<", "<=", "GREATER", "LOWER"
        );
    }

    /**
     * String type to type Vartype.
     */
    private Vartype stringToType(String type) {
        switch (type) {
            case "enter" -> {
                return Vartype.ENTER;
            }
            case "decimal" -> {
                return Vartype.DECIMAL;
            }
            case "siono" -> {
                return Vartype.SIONO;
            }
            case "res" ->{
                return Vartype.RES;
            }
            default -> {
                return Vartype.UNASSIGNED;
            }
        }
    }

    /**
     * Analizes symbol table.
     */
    public void analizeSymbolTable() {
        analizeScopes(symbolTable.getRootScope());
    }

    /**
     * Analize each symbol table contained in each scope.
     *
     * @param currentScope the current scope
     */
    private void analizeScopes(Scope currentScope) {
        // Check each entry in the symbol table
        for (Map.Entry<String, SymbolTableEntry> entry : currentScope.getSymbolTable().entrySet()) {
            if (entry.getValue() instanceof VariableEntry tableEntry) {
                checkVariable(currentScope, tableEntry);
            } else if (entry.getValue() instanceof FunctionEntry tableEntry) {
                checkFunction(currentScope, tableEntry);
            } else if (entry.getValue() instanceof ConditionalEntry tableEntry) {
                checkConditional(currentScope, tableEntry);
            } else if (entry.getValue() instanceof CallEntry tableEntry) {
                checkCall(currentScope, tableEntry);
            }
        }
        // Recursive call to analize the child scopes
        for (Scope scope : currentScope.getChildScopes()) {
            analizeScopes(scope);
        }
    }
    /**
     * Check call entry of symbol table.
     *
     * @param scope     the scope
     * @param callEntry the call entry
     */
    private void checkCall(Scope scope, CallEntry callEntry) {
        FunctionEntry functionEntry = (FunctionEntry) this.symbolTable.getRootScope().getEntry(callEntry.getName());
        if(callEntry.getParameters().size() == 0){
            callEntry.reArrangeParameters();
        }
        checkParametersType(callEntry, functionEntry, scope);

        if (functionEntry.getParameters().size() != callEntry.getParameters().size()) {
            this.errorHandler.recordParameterMismatchError(callEntry.getName(), functionEntry.getParameters().size(), callEntry.getParameters().size(), callEntry.getLine());
        }
    }

    /**
     * Check conditional entry of symbol table.
     *
     * @param scope     the scope
     * @param condEntry the conditional entry
     */
    private void checkConditional(Scope scope, ConditionalEntry condEntry) {
        if(!checkBooleanExpression(condEntry.getCondition(), scope)){
           this.errorHandler.recordConditionError(condEntry.getLine());
        }
    }

    /**
     * Check function entry of symbol table.
     *
     * @param scope    the scope
     * @param funcEntry the func entry
     */
    private void checkFunction(Scope scope, FunctionEntry funcEntry) {
        Vartype functionType = stringToType(funcEntry.getReturnType());
        Vartype newType;
        if (Objects.equals(funcEntry.getReturnType(), "res")) {
            if (!funcEntry.getReturnValue().isEmpty()) {
                this.errorHandler.recordInvalidReturnError(funcEntry.getName(), funcEntry.getLine());

            }
        } else {
            if (funcEntry.getReturnValue().isEmpty()) {
                this.errorHandler.recordMissingReturnError(funcEntry.getName(), funcEntry.getLine());
            }
        }

        if (funcEntry.getReturnValue().isEmpty() && !Objects.equals(funcEntry.getReturnType(), "res")) {
        }
        for (Object term : funcEntry.getReturnValue()) {
            newType = getTermType(term, scope);
            if (functionType != Vartype.RES && newType != Vartype.UNASSIGNED && newType != functionType) {
                this.errorHandler.recordTypeMismatchError("return of function " + funcEntry.getName(), funcEntry.getReturnType().toUpperCase(), funcEntry.getLine());
            }
        }
    }

    /**
     * Check variable entry of symbol table.
     *
     * @param scope    the scope
     * @param varEntry the variable entry
     */
    private void checkVariable(Scope scope, VariableEntry varEntry) {
        Vartype currentType = stringToType(varEntry.getType());
        Vartype newType;
        // Check the current expression types
        for (Object term : varEntry.getExpression()) {
            newType = getTermType(term, scope);
            if (newType != Vartype.UNASSIGNED && newType != currentType) {
                this.errorHandler.recordTypeMismatchError("assignation", varEntry.getType().toUpperCase(), varEntry.getLine());
            }
        }

        // Check the past expressions types
        for (List<Object> list : varEntry.getPastExpressions()) {
            for (Object term : list) {
                newType = getTermType(term, scope);
                if (newType != Vartype.UNASSIGNED && newType != currentType) {
                    this.errorHandler.recordTypeMismatchError("assignation", varEntry.getType().toUpperCase(), varEntry.getLine());
                }
            }
        }
    }

    /**
     * Get type of a term (number, boolean, variable...).
     *
     * @param term  the term
     * @param scope the scope
     * @return the term Vartype
     */
    private Vartype getTermType(Object term, Scope scope) {
        if (term instanceof Integer) return Vartype.ENTER;
        if (term instanceof Float) return Vartype.DECIMAL;
        if (term instanceof Boolean) return Vartype.SIONO;
        if (term instanceof String value) {
            if (this.operators.contains(term)) {
                return Vartype.UNASSIGNED;
            }
            return getVariableType(scope, value);
        }
        if (term instanceof CallEntry) {
            return getFunctionType((CallEntry) term, scope);
        }
        return Vartype.UNASSIGNED;
    }

    /**
     * Get function Vartype of a call entry.
     *
     * @param call  the call
     * @param scope the scope
     * @return the function type
     */
    private Vartype getFunctionType(CallEntry call, Scope scope) {
        SymbolTableEntry entry = this.symbolTable.getRootScope().getEntry(call.getName());
        if (entry instanceof FunctionEntry functionEntry) {
            if (functionEntry.getParameters().size() != call.getParameters().size()) {
                this.errorHandler.recordParameterMismatchError(call.getName(), functionEntry.getParameters().size(), call.getParameters().size(), call.getLine());
            } else {
                checkParametersType(call, functionEntry, scope);
            }
            return stringToType(functionEntry.getReturnType());
        }
        return Vartype.UNASSIGNED;

    }

    /**
     * Check parameters Vartype of a call entry.
     *
     * @param call          the call
     * @param functionEntry the function entry
     * @param scope         the scope
     */
    private void checkParametersType(CallEntry call, FunctionEntry functionEntry, Scope scope) {
        Vartype functionParameterType = Vartype.UNASSIGNED;
        Vartype callParameterType = Vartype.UNASSIGNED;
        for (int i = 0; i < functionEntry.getParameters().size(); i++) {
            functionParameterType = stringToType(functionEntry.getParameters().get(i).getType());
            for (Object term : call.getParameters().get(i)) {
                if(term.toString().equals(",")){
                    continue;
                }
                callParameterType = getTermType(term, scope);
                if (callParameterType != Vartype.UNASSIGNED && functionParameterType != callParameterType) {
                    this.errorHandler.recordTypeMismatchError("parameter number" + (i + 1), functionEntry.getReturnType(), call.getLine());
                }
            }
        }
    }

    /**
     * Get variable Vartype.
     *
     * @param scope the scope
     * @param var   the var
     * @return the variable type
     */
    private Vartype getVariableType(Scope scope, String var) {
        if (scope == null) return Vartype.UNASSIGNED;
        SymbolTableEntry entry = scope.getSymbolTable().get(var);
        if (entry != null) {
            if (entry instanceof VariableEntry variableEntry) {
                return stringToType(variableEntry.getType());
            }
        } else {
            return getVariableType(scope.getParentScope(), var);
        }
        return Vartype.UNASSIGNED;
    }


    /**
     * Check boolean expression boolean.
     *
     * @param expr  the expr
     * @param scope the scope
     * @return the boolean
     */
    public boolean checkBooleanExpression(List<Object> expr, Scope scope) {
        Vartype previousType = Vartype.UNASSIGNED;
        Vartype currentType = Vartype.UNASSIGNED;
        boolean first = true;

        // Check the current boolean expression types
        for (Object token : expr) {
            currentType = getTermType(token, scope);
            if (first){
                previousType = currentType;
                first = false;
            }
            if(currentType != Vartype.UNASSIGNED && currentType != previousType){
                return false;
            }
        }
        return true;
    }
}
