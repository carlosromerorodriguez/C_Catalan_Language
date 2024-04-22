package frontEnd;

public class CodeLine {
    private final int line;
    private final String contentLine;

    public CodeLine(int line, String contentLine) {
        this.line = line;
        this.contentLine = contentLine;
    }

    public int getLine() {
        return line;
    }

    public String getContentLine() {
        return contentLine;
    }
}
