package frontEnd.global;

public class Error {
    private final String errorMsg;
    private final int line;

    public Error(String errorMsg, int line) {
        this.errorMsg = errorMsg;
        this.line = line;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getLine() {
        return line;
    }

}

