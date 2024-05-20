package frontEnd.global;

import frontEnd.syntactic.Node;

import java.util.*;

/**
 * Class that manages the errors found in the code file
 */
public class ErrorHandler {
    private final LinkedList<Error> errors;

    /**
     * Instantiates a new Error handler.
     */
    public ErrorHandler() {
        this.errors = new LinkedList<>();
    }

    /**
     * Records an error.
     *
     * @param errorMsg the error msg
     * @param line     the line
     */
    public void recordError(String errorMsg, int line){
        errors.add(new Error(errorMsg, line));
    }

    /**
     * Records type mismatch error.
     *
     * @param location the location
     * @param type     the type
     * @param line     the line
     */
    public void recordTypeMismatchError(String location, String type, int line){
        errors.add(new Error("Coincidència de tipus errònia: s'esperava '" + type + "'", line));
    }

    /**
     * Records parameter mismatch error.
     *
     * @param func           the func
     * @param expectedParams the expected params
     * @param gotParams      the got params
     * @param line           the line
     */
    public void recordParameterMismatchError(String func, int expectedParams, int gotParams, int line){
        errors.add(new Error("Nombre de paràmetres no coincident a la funció '" + func + "': s'esperava '" + expectedParams + "' i s'ha obtingut '" + gotParams + "'", line));
    }

    /**
     * Records missing return error.
     *
     * @param func the func
     * @param line the line
     */
    public void recordMissingReturnError(String func, int line){
        errors.add(new Error("Falta retornar un valor a la funció '" + func +"'", line));
    }

    /**
     * Records invalid return error.
     *
     * @param func the func
     * @param line the line
     */
    public void recordInvalidReturnError(String func, int line) {
        errors.add(new Error("Valor de retorn invàlid a la funció '" + func + "'", line));
    }

    /**
     * Records condition error error.
     *
     * @param line the line
     */
    public void recordConditionError(int line){
        errors.add(new Error("Els tipus de condició no coincideixen", line));
    }

    /**
     * Records function is not declared error
     *
     * @param node the node
     */
    public void recordFunctionIsNotDeclared(Node node){
        errors.add(new Error("Funció '" + node.getValue() + "' no declarada prèviament", node.getLine()));
    }

    /**
     * Records variable doesnt exist error
     *
     * @param node the node
     */
    public void recordVariableDoesntExist(Node node){
        errors.add(new Error("Variable '" + node.getValue() + "' no declarada prèviament", node.getLine()));
    }

    /**
     * Records variable already declared error
     *
     * @param node the node
     */
    public void recordVariableAlreadyDeclared(Node node) {
        errors.add(new Error("Variable '" + node.getValue() + "' ja s'ha declarat prèviament", node.getLine()));
    }

    /**
     * Prints the errors.
     */
    public void printErrors() {
        orderErrorsByLine();

        String ANSI_RED = "\u001B[31m";
        String ANSI_RESET = "\u001B[0m";
        String line = "_______________________________________________________________________________________";

        System.out.println(ANSI_RED + line + ANSI_RESET);
        for (Error e : errors) {
            System.out.printf(ANSI_RED + "ERROR: %s a la línia %d.\n" + ANSI_RESET, e.getErrorMsg(), e.getLine());
        }
        System.out.println(ANSI_RED + line + ANSI_RESET);
    }

    /**
     * Orders errors by line number.
     */
    private void orderErrorsByLine() {
        removeDuplicateErrors();
        errors.sort(Comparator.comparingInt(Error::getLine));
    }

    /**
     * Removes duplicate errors that can be found in the list
     */
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

    /**
     * Returns if there are errors.
     *
     * @return the boolean
     */
    public Boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Records function already exists error
     *
     * @param node the node
     */
    public void recordFunctionAlreadyExists(Node node) {
        errors.add(new Error("La funció '" + node.getValue() + "' ja existeix", node.getLine()));
    }
}
