package frontEnd.global;

import java.util.*;

public class ErrorHandler {
    private final TreeSet<Error> errors;

    public ErrorHandler() {
        this.errors = new TreeSet<>(Comparator.comparingInt(Error::getLine));
    }

    public void recordError(String errorMsg, int line){
        errors.add(new Error(errorMsg, line));
    }

    public void printErrors() {
        String ANSI_RED = "\u001B[31m";
        String ANSI_RESET = "\u001B[0m";
        String line = "_______________________________________________________________________________________";

        for (Error e : errors) {
            System.out.println(ANSI_RED + line + ANSI_RESET);
            System.out.printf(ANSI_RED + "Error: %s in line %d\n" + ANSI_RESET, e.getErrorMsg(), e.getLine());
            System.out.println(ANSI_RED + line + ANSI_RESET);
        }
    }

    public Boolean hasErrors() {
        return !errors.isEmpty();
    }
}
