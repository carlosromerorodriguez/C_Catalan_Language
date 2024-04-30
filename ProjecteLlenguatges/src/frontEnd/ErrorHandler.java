package frontEnd;

import java.util.*;

public class ErrorHandler {
    private final TreeSet<Error> errors;

    public ErrorHandler() {
        this.errors = new TreeSet<>(Comparator.comparingInt(Error::getLine));
    }

    public void recordError(String errorMsg, int line){
        errors.add(new Error(errorMsg, line));
    }

    public void printErrors(){
        for (Error e : errors) {
            System.out.printf("Error: %s in line %d\n", e.getErrorMsg(), e.getLine());
        }
    }
}
