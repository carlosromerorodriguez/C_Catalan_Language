package frontEnd;

public class Token<T> {
    private String stringToken;
    private int line;
    private final T value;

    public Token(String stringToken, int line) {
        this.stringToken = stringToken;
        this.line = line;
        value = null;
    }

    public Token(String stringToken, int line, T value) {
        this.stringToken = stringToken;
        this.line = line;
        this.value = value;
    }

    public String getStringToken() {
        return stringToken;
    }

    public void setStringToken(String stringToken) {
        this.stringToken = stringToken;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public T getValue() {
        return value;
    }

}
