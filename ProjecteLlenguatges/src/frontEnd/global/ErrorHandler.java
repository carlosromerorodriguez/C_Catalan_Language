package frontEnd.global;

import frontEnd.syntactic.Node;

import java.util.*;

public class ErrorHandler {
    private final TreeSet<Error> errors;

    public ErrorHandler() {
        this.errors = new TreeSet<>(Comparator.comparing(Error::getErrorMsg));
    }

    public void recordError(String errorMsg, int line){
        errors.add(new Error(errorMsg, line));
    }
    public void recordTypeMismatchError(String location, String type, int line){
        errors.add(new Error("Type mismatch in " + location + ": expected " + type, line));
    }

    public void recordParameterMismatchError(String func, int expectedParams, int gotParams, int line){
        errors.add(new Error("Parameters number mismatch in function " + func + ": expected " + expectedParams + " got " + gotParams, line));
    }
    public void recordMissingReturnError(String func, int line){
        errors.add(new Error("Missing return in function " + func, line));
    }
    public void recordInvalidReturnError(String func, int line) {
        errors.add(new Error("Invalid return in void function " + func, line));
    }
    public void recordConditionError(int line){
        errors.add(new Error("Condition does not evaluate into a SIONO", line));
    }

    public void recordFunctionIsNotDeclared(Node node){
        errors.add(new Error("Function " + node.getValue() + " is not previously declared", node.getLine()));
    }

    public void recordVariableDoesntExist(Node node){
        errors.add(new Error("Variable " + node.getValue() + " is not previously declared", node.getLine()));
    }

    public void recordVariableAlreadyDeclared(Node node) {
        errors.add(new Error("Variable " + node.getValue() + " is already declared", node.getLine()));
    }

    public void printErrors() {
        String ANSI_RED = "\u001B[31m";
        String ANSI_RESET = "\u001B[0m";
        String line = "_______________________________________________________________________________________";

        for (Error e : errors) {
            System.out.println(ANSI_RED + line + ANSI_RESET);
            System.out.printf(ANSI_RED + "Error: %s in line %d.\n" + ANSI_RESET, e.getErrorMsg(), e.getLine());
            System.out.println(ANSI_RED + line + ANSI_RESET);
        }
    }

    public Boolean hasErrors() {
        return !errors.isEmpty();
    }
}
