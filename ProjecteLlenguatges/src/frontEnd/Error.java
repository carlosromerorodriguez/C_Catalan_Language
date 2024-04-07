package frontEnd;

public class Error {
    private String errorMsg;
    private int line;

    public Error(String errorMsg, int line) {
        this.errorMsg = errorMsg;
        this.line = line;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
}

