package frontEnd;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Lexer {
    private ErrorHandler errorHandler;

    private List<CodeLine> codeLines;

    private int currentIndex = 0;
    private List<Token> tokens;
    private TokenConverter tokenConverter;
    int a;


    /**
     * Constructor for the Lexer class.
     * @param codeLines The source code to be tokenized.
     * @param tokenConverter converter of tokens
     */
    public Lexer(ErrorHandler errorHandler, List<CodeLine> codeLines, TokenConverter tokenConverter) {
        this.errorHandler = errorHandler;
        this.codeLines = codeLines;
        this.tokenConverter = tokenConverter;
        tokenConverter.fillTokensDictionary();
        this.tokens = new ArrayList<>();
        tokenize();
    }

    /**
     * Tokenizes the source code.
     */
    private void tokenize() {
        String tokenPatterns =
                "\\b(si|sino|mentre|per|fer|fi|enter|decimal|lletra|lletres|siono|res|Calçot|proces|retorn|crida)\\b\n|" + // Palabras reservadas
                        "([A-Za-zÀ-ú][A-Za-zÀ-ú0-9_]*)|" + // Identificadores
                        "(-?\\d+(\\.\\d+)?)|" + // Números (decimales y enteros)
                        "(==|!=|<=|>=|\\+|\\-|\\*|/|=|<|>|\\(|\\)|;|,|:)"; // Operadores y símbolos

        for (int i = 0; i < codeLines.size(); i++) {
            Pattern pattern = Pattern.compile(tokenPatterns);
            Matcher matcher = pattern.matcher(codeLines.get(i).getContentLine());
            String line = codeLines.get(i).getContentLine();
            String lastToken = "";

            int lastMatchEnd = 0; // Iniciar índice para el final del último match

            while (matcher.find(lastMatchEnd)) {
                String lexeme = matcher.group();

                //System.out.println("RAW: "+lexeme);
                // Verificar si es un número primero
                if (lexeme.matches("\\d+(\\.\\d+)?([eE][-+]?\\d+)?")) {
                    //System.out.println("Number: " + lexeme);
                    if (lexeme.contains(".")) {
                        tokens.add(new Token<Float>("id", codeLines.get(i).getLine(), Float.parseFloat(lexeme)));
                    } else {
                        tokens.add(new Token<Integer>("id", codeLines.get(i).getLine(), Integer.parseInt(lexeme)));
                    }
                } else if (lexeme.matches("[+\\-*/=<>!]|==|!=|<=|>=|\\(|\\)|;|,|:")) {
                    //System.out.println("Operator: " + lexeme);
                    String tokenName = tokenConverter.convertLexemeToToken(lexeme);
                    tokens.add(new Token<String>(tokenName, codeLines.get(i).getLine(), lexeme));
                } else {
                    String token = tokenConverter.convertLexemeToToken(lexeme);
                    if (!token.equals(lexeme)) {
                        tokens.add(new Token<>(token, codeLines.get(i).getLine()));
                    } else {
                        // Si el lexeme no reconegut conte ñ o ç mostrem error
                        if (lexeme.contains("ç") || lexeme.contains("ñ")) {
                            errorHandler.recordError(lexeme + " contains invalid character.", codeLines.get(i).getLine());
                        } else {
                            // Es un nom acceptat mirem si es un function_name o var_name
                            String tokenType = "name";
                            tokens.add(new Token<String>(tokenType, codeLines.get(i).getLine(), lexeme));
                        }
                    }
                }

                lastToken = lexeme; // Actualizar el último token procesado
                lastMatchEnd = matcher.end(); // Actualizar el final del último match para continuar desde aquí

                // Reemplazar solo la primera ocurrencia del lexema procesado en la línea
                line = line.replaceFirst(Pattern.quote(lexeme), "");
            }

            if(!line.trim().isEmpty()) { //Si encara queden coses a la linia ens guardem l'error
                //Guardem l'error amb la linia
                errorHandler.recordError(line + " is not a statement.", codeLines.get(i).getLine());
            }
        }

        for (int i = 0; i < tokens.size() - 1; i++) {
            if(tokens.get(i).getStringToken().equals("name")) {
                if(tokens.get(i+1).getStringToken().equals(" ( ")) {
                    tokens.get(i).setStringToken("function_name");
                } else if (i > 0 && tokens.get(i-1).getStringToken().equals("=")) {
                    tokens.get(i).setStringToken("id");
                } else {
                    tokens.get(i).setStringToken("var_name");
                }
            }
        }
    }

    public void showTokens() {
        for (Token token : tokens) {
            System.out.println("Token: "+token.getStringToken()+" in line: "+token.getLine()+" value: " + (token.getValue() == null ? "" : token.getValue()));
        }
    }

    /**
     * Gets the next token.
     */
    public Token nextToken() {
        if (currentIndex < tokens.size()) {
            return tokens.get(currentIndex++);
        }
        return null;
    }

    /**
     * Peeks the next token.
     */
    public Token peekToken() {
        if (currentIndex < tokens.size()) {
            return tokens.get(currentIndex);
        }
        return null;
    }

    /**
     * Gets the current token.
     */
    public Token currentToken() {
        if (currentIndex > 0 && currentIndex <= tokens.size()) {
            return tokens.get(currentIndex - 1);
        }
        return null;
    }
}

/* REGEX:
OPTION1:
String tokenPatterns =
                "(\\b(si|sino|mentre|per|fer:|fi|enter:|decimal:|lletra:|lletres:|siono:|res|Calçot|proces|retorn|crida)\\b)|" + // Palabras reservadas
                        "([A-Za-zÀ-ú_][A-Za-zÀ-ú0-9]*)|" + // Identificadores
                        "(-?\\d+(\\.\\d+)?)|" + // Números (decimales y enteros)
                        "(==|!=|<=|>=|\\+|\\-|\\*|/|=|<|>|\\(|\\)|;|,)"; // Operadores y símbolos


OPTION2:

 */