package frontEnd;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    public enum ErrorType {

    }

    private List<Error> errors;

    public ErrorHandler() {
        this.errors = new ArrayList<>();
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
