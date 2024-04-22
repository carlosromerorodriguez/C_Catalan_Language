package frontEnd;

public class Token<T> {
    private String stringToken;
    private int line;
    private T value;
    private String originalName;

    public Token(String stringToken, int line) {
        this.stringToken = stringToken;
        this.line = line;
        value = null;
    }

    public Token(String stringToken, int line, T value, String originalName) {
        this.stringToken = stringToken;
        this.line = line;
        this.value = value;
        this.originalName = originalName;
    }

    public String getStringToken() {
        return stringToken;
    }

    public void setStringToken(String stringToken) {
        this.stringToken = stringToken;
    }

    public void setValue(T value) {
        this.value = value;
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

    public String getOriginalName() {
        return originalName;
    }
}
