package frontEnd;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private String sourceCode;
    private int currentIndex = 0;
    private List<String> tokens = new ArrayList<>();
    private TokenConverter tokenConverter = new TokenConverter();

    /**
     * Constructor for the Lexer class.
     * @param sourceCode The source code to be tokenized.
     */
    public Lexer(String sourceCode) {
        this.sourceCode = sourceCode;
        tokenize();
    }

    /**
     * Tokenizes the source code.
     */
    private void tokenize() {
        //TODO: Implement the tokenization of the source code
        // word es una paraula la qual volem tokenitzar
        tokens.add(tokenConverter.convertLexemeToToken(word));
    }

    /**
     * Gets the next token.
     */
    public String nextToken() {
        if (currentIndex < tokens.size()) {
            return tokens.get(currentIndex++);
        }
        return null;
    }

    /**
     * Peeks the next token.
     */
    public String peekToken() {
        if (currentIndex < tokens.size()) {
            return tokens.get(currentIndex);
        }
        return null;
    }

    /**
     * Gets the current token.
     */
    public String currentToken() {
        if (currentIndex > 0 && currentIndex <= tokens.size()) {
            return tokens.get(currentIndex - 1);
        }
        return null;
    }
}
