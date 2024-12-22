package frontEnd.lexic;

/**
 * Token class
 *
 * @param <T> Type of the value of the token
 */
public class Token<T> {
    /**
     * stringToken: String representation of the token
     */
    private String stringToken;
    /**
     * line: Line where the token is
     */
    private int line;
    /**
     * value: Value of the token
     */
    private T value;
    /**
     * originalName: Original name of the token
     */
    private String originalName;

    /**
     * Constructor of the class Token
     *
     * @param stringToken String representation of the token
     * @param line        Line where the token is
     */
    public Token(String stringToken, int line) {
        this.stringToken = stringToken;
        this.line = line;
        value = null;
    }

    /**
     * Constructor of the class Token
     *
     * @param stringToken  String representation of the token
     * @param line         Line where the token is
     * @param value        Value of the token
     * @param originalName the original name
     */
    public Token(String stringToken, int line, T value, String originalName) {
        this.stringToken = stringToken;
        this.line = line;
        this.value = value;
        this.originalName = originalName;
    }


    /**
     * Gets string token.
     *
     * @return the string token
     */
    public String getStringToken() {
        return stringToken;
    }

    /**
     * Sets string token.
     *
     * @param stringToken the string token
     */
    public void setStringToken(String stringToken) {
        this.stringToken = stringToken;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Gets line.
     *
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * Sets line.
     *
     * @param line the line
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public T getValue() {
        return value;
    }

    /**
     * Gets original name.
     *
     * @return the original name
     */
    public String getOriginalName() {
        return originalName;
    }
}
