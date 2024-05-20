package global.errors;

/**
 * Error clas that represents an error in the code
 */
public class Error {
    /**
     * The Error message.
     */
    private final String errorMsg;
    /**
     * The Line where the error is.
     */
    private final int line;

    /**
     * Constructor
     *
     * @param errorMsg Error message
     * @param line     Line where the error is
     */
    public Error(String errorMsg, int line) {
        this.errorMsg = errorMsg;
        this.line = line;
    }

    /**
     * Gets error msg.
     *
     * @return the error msg
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Gets line.
     *
     * @return the line
     */
    public int getLine() {
        return line;
    }

}

