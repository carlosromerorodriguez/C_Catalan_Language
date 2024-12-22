package frontEnd.lexic;

/**
 * Class that represents a line of code
 */
public class CodeLine {
    /**
     * Line of the code
     */
    private final int line;
    /**
     * content of the line of the code
     */
    private final String contentLine;

    /**
     * Instantiates a new Code line.
     *
     * @param line        the line
     * @param contentLine the content line
     */
    public CodeLine(int line, String contentLine) {
        this.line = line;
        this.contentLine = contentLine;
    }

    /**
     * Gets the line.
     *
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the content of line.
     *
     * @return the content of the line
     */
    public String getContentLine() {
        return contentLine;
    }
}
