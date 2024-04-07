package frontEnd;

public class CodeLine {
    private int line;
    private String contentLine;

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
