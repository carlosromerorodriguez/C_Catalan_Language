package frontEnd.global;

import frontEnd.syntactic.Node;

import java.util.*;

public class ErrorHandler {
    private final LinkedList<Error> errors;

    public ErrorHandler() {
        this.errors = new LinkedList<>();
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
        errors.add(new Error("Els tipus de condició no coincideixen", line));
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
        orderErrorsByLine();

        String ANSI_RED = "\u001B[31m";
        String ANSI_RESET = "\u001B[0m";
        String line = "_______________________________________________________________________________________";

        for (Error e : errors) {
            System.out.println(ANSI_RED + line + ANSI_RESET);
            System.out.printf(ANSI_RED + "Error: %s in line %d.\n" + ANSI_RESET, e.getErrorMsg(), e.getLine());
            System.out.println(ANSI_RED + line + ANSI_RESET);
        }
    }

    private void orderErrorsByLine() {
        removeDuplicateErrors();
        errors.sort(Comparator.comparingInt(Error::getLine));
    }

    private void removeDuplicateErrors() {
        for (int i = 0; i < errors.size(); i++) {
            for (int j = 0; j < errors.size(); j++) {
                if (i != j && errors.get(i).getErrorMsg().equals(errors.get(j).getErrorMsg()) && errors.get(i).getLine() == errors.get(j).getLine()) {
                    errors.remove(j);
                    j--;
                }
            }
        }

    }

    public Boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void recordFunctionAlreadyExists(Node node) {
        errors.add(new Error("Function "+node.getValue()+" already exists", node.getLine()));
    }
}
