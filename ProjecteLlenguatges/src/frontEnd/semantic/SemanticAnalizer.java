package frontEnd.semantic;

import frontEnd.global.ErrorHandler;
import frontEnd.syntactic.symbolTable.*;

import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.*;
//import com.udojava.evalex.Expression;


public class SemanticAnalizer {
    public enum Vartype {
        ENTER,
        DECIMAL,
        SIONO,
        LLETRES,
        UNASSIGNED;
    }

    private SymbolTable symbolTable;
    private ErrorHandler errorHandler;
    private List<String> operators;
    private static final List<String> mathOps = List.of("+", "-", "*", "/");

    // Lista de operadores booleanos
    private static final List<String> boolOps = List.of("&&", "||", "!");

    public SemanticAnalizer(SymbolTable symbolTable, ErrorHandler errorHandler) {
        this.symbolTable = symbolTable;
        this.errorHandler = errorHandler;
        this.operators = Arrays.asList(
                "+", "-", "*", "/", "(", ")", "==", ">", ">=", "<", "<="
        );
    }

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
            for (Map.Entry<String, SymbolTableEntry> entry : scope.getSymbolTable().entrySet()) {
                if (entry.getValue() instanceof VariableEntry tableEntry) {
                    checkVariable(scope, tableEntry);
                } else if (entry.getValue() instanceof FunctionEntry tableEntry) {
                    checkFunction(scope, tableEntry);
                } else if (entry.getValue() instanceof ConditionalEntry tableEntry) {
                    checkConditional(scope, tableEntry);
                } else if (entry.getValue() instanceof CallEntry tableEntry) {
                    checkCall(scope, tableEntry);
                }

                analizeScopes(scope);
            }
        }
    }

    private void checkCall(Scope scope, CallEntry condEntry) {

    }

    private void checkConditional(Scope scope, ConditionalEntry condEntry) {
        if(!checkBooleanExpression(condEntry.getCondition(), scope)){
           this.errorHandler.recordConditionError(condEntry.getLine());
        }
        return;
    }

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
            if (newType != Vartype.UNASSIGNED && newType != functionType) {
                this.errorHandler.recordTypeMismatchError("return of function " + funcEntry.getName(), funcEntry.getReturnType().toUpperCase(), funcEntry.getLine());
            }
        }
    }

    private void checkVariable(Scope scope, VariableEntry varEntry) {
        Vartype currentType = stringToType(varEntry.getType());
        Vartype newType;
        for (Object term : varEntry.getExpression()) {
            newType = getTermType(term, scope);
            if (newType != Vartype.UNASSIGNED && newType != currentType) {
                this.errorHandler.recordTypeMismatchError("assignation", varEntry.getType().toUpperCase(), varEntry.getLine());
            }
        }
    }

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

    private void checkParametersType(CallEntry call, FunctionEntry functionEntry, Scope scope) {
        Vartype functionParameterType = Vartype.UNASSIGNED;
        Vartype callParameterType = Vartype.UNASSIGNED;
        for (int i = 0; i < functionEntry.getParameters().size(); i++) {
            functionParameterType = stringToType(functionEntry.getParameters().get(i).getType());
            for (Object term : call.getParameters().get(i)) {
                callParameterType = getTermType(term, scope);
                if (callParameterType != Vartype.UNASSIGNED && functionParameterType != callParameterType) {
                    this.errorHandler.recordTypeMismatchError("parameter number" + (i + 1), functionEntry.getReturnType(), call.getLine());
                }
            }
        }
    }

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


    public  boolean checkBooleanExpression(List<Object> expr, Scope scope) {
        Stack<Vartype> stack = new Stack<>();
        Stack<Integer> parenStack = new Stack<>();

        for (Object token : expr) {
            if (token instanceof String && token.equals("(")) {
                parenStack.push(stack.size());
            } else if (token instanceof String && token.equals(")")) {
                if (parenStack.isEmpty() || stack.size() <= parenStack.peek() || stack.peek() != Vartype.SIONO) {
                    return false;
                }
                stack.setSize(parenStack.pop());
                stack.push(Vartype.SIONO);
            } else if (token instanceof Integer) { // Números enteros
                stack.push(Vartype.ENTER);
            } else if (token instanceof Float) { // Números decimales
                stack.push(Vartype.DECIMAL);
            } else if (token instanceof Boolean) { // Booleanos
                stack.push(Vartype.SIONO);
            } else if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) { // Operadores matemáticos
                if (stack.size() < 2 || stack.pop() != stack.peek()) {
                    return false;
                }
            } else if (token.equals("and") || token.equals("or") || token.equals("not") || token.equals("GREATER") || token.equals("<") || token.equals("<=") || token.equals(">=") || token.equals("==") || token.equals("!=")) { // Operadores booleanos
                if (stack.isEmpty() || stack.pop() != Vartype.SIONO || stack.peek() != Vartype.SIONO) {
                    return false;
                }
            } else { // Variables
                stack.push(getTermType(token, scope));
            }
        }

        return stack.size() == 1 && stack.peek() == Vartype.SIONO && parenStack.isEmpty();
    }

}
