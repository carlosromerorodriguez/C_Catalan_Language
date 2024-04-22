package frontEnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Lexer {
    private final ErrorHandler errorHandler;
    private final List<CodeLine> codeLines;
    private int currentIndex = 0;
    private final List<Token> tokens;
    private final TokenConverter tokenConverter;

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
        this.tokenize();
        this.reprocessTokens();
    }

    /**
     * Tokenizes the source code.
     */
    private void tokenize() {
        String tokenPatterns =
                "\\b(si|sino|mentre|per|fer|fi|enter|decimal|lletra|lletres|siono|res|Calçot|proces|retorn|crida|de|fins)\\b\n|" + // Palabras reservadas
                        "([A-Za-zÀ-ú][A-Za-zÀ-ú0-9_]*)|" + // Identificadores
                        "(\\d+(\\.\\d+)?)|" + // Números (decimales y enteros)
                        "(!|==|!=|<=|>=|\\+|-|\\*|/|=|<|>|\\(|\\)|;|,|:)"; // Operadores y símbolos

        for (int i = 0; i < codeLines.size(); i++) {
            Pattern pattern = Pattern.compile(tokenPatterns);
            Matcher matcher = pattern.matcher(codeLines.get(i).getContentLine());
            String line = codeLines.get(i).getContentLine();

            int lastMatchEnd = 0; // Iniciar índice para el final del último match

            while (matcher.find(lastMatchEnd)) {
                String lexeme = matcher.group();
                System.out.println("Lexeme: " + lexeme);

                // Verificar si es un número primero
                //if (lexeme.matches("\\d+|\\d-(\\.\\d+)?([eE][-+]?\\d+)?")) {
                if (lexeme.matches("-?\\d+(\\.\\d+)?")) {
                    //System.out.println("Number: " + lexeme);
                    if (lexeme.contains(".")) {
                        tokens.add(new Token<Float>("literal", codeLines.get(i).getLine(), Float.parseFloat(lexeme), lexeme));
                    } else {
                        tokens.add(new Token<Integer>("literal", codeLines.get(i).getLine(), Integer.parseInt(lexeme), lexeme));
                    }
                } else if (lexeme.matches("[-+*/=<>!]|==|!=|<=|>=|\\(|\\)|;|,|:")) {
                    //System.out.println("Operator: " + lexeme);
                    String tokenName = tokenConverter.convertLexemeToToken(lexeme);
                    tokens.add(new Token<String>(tokenName, codeLines.get(i).getLine(), lexeme, lexeme));
                } else {
                    String token = tokenConverter.convertLexemeToToken(lexeme);
                    if (!token.equals(lexeme)) {
                        tokens.add(new Token<>(token, codeLines.get(i).getLine(), null, lexeme));
                    } else {
                        // Si el lexeme no reconegut conte ñ o ç mostrem error
                        if (lexeme.contains("ç") || lexeme.contains("ñ")) {
                            if(lexeme.equals("Calçot")) {
                                String tokenName = tokenConverter.convertLexemeToToken(lexeme);
                                tokens.add(new Token<String>(tokenName, codeLines.get(i).getLine(), lexeme, lexeme));
                            } else {
                                errorHandler.recordError(lexeme + " contains invalid character.", codeLines.get(i).getLine());
                            }
                        } else {
                            // Es un nom acceptat mirem si es un function_name o var_name
                            String tokenType = "name";
                            tokens.add(new Token<String>(tokenType, codeLines.get(i).getLine(), lexeme, lexeme));
                        }
                    }
                }

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
                String aux_token = tokenConverter.getToken((String) tokens.get(i).getValue());
                if(!Objects.equals(aux_token, "")){
                    tokens.get(i).setStringToken(aux_token);
                    continue;
                }
                if(tokens.get(i+1).getStringToken().trim().equals("(")) {
                    tokens.get(i).setStringToken("function_name");
                } else if (i > 0 && tokens.get(i-1).getStringToken().equals("=")) {
                    tokens.get(i).setStringToken("literal");
                } else {
                    tokens.get(i).setStringToken("var_name");
                }
            } else if (tokens.get(i).getStringToken().equals("function")) {
                if(tokens.get(i+1).getStringToken().equals("Calçot")) {
                    tokens.get(i).setStringToken("function_main");
                }
            }
        }
    }

    void reprocessTokens() {
        ArrayList<Integer> deletedPositions = new ArrayList<>();
        for (int i = 0; i < tokens.size() - 1; i++) {
            if (tokens.get(i).getStringToken().equals("+") || tokens.get(i).getStringToken().equals("-")) { // Si el token + o -, mirem si forma part d'un literal
                if (tokens.get(i - 1).getStringToken().equals("(") || tokens.get(i - 1).getStringToken().equals("=") ||
                        tokens.get(i - 1).getStringToken().equals("+") || tokens.get(i - 1).getStringToken().equals("-") ||
                        tokens.get(i - 1).getStringToken().equals("*") || tokens.get(i - 1).getStringToken().equals("/") ||
                        tokens.get(i - 1).getStringToken().equals("==") || tokens.get(i - 1).getStringToken().equals("!=") ||
                        tokens.get(i - 1).getStringToken().equalsIgnoreCase("LOWER") || tokens.get(i - 1).getStringToken().equalsIgnoreCase("GREATER") ||
                        tokens.get(i - 1).getStringToken().equalsIgnoreCase("LOWER_EQUAL") || tokens.get(i - 1).getStringToken().equalsIgnoreCase("GREATER_EQUAL") ||
                        tokens.get(i - 1).getStringToken().equals(",")) {
                    //Si abans hi ha un parentesis obert o un operador el + o - forma part d'un literal
                    //Ajuntem el + o - amb el seguent token
                    if (tokens.get(i + 1).getStringToken().equals("literal")) {
                        if (tokens.get(i).getStringToken().equals("+")) {
                            tokens.get(i + 1).setValue(tokens.get(i + 1).getValue());
                        } else {
                            if (tokens.get(i + 1).getValue() instanceof Float) {
                                tokens.get(i + 1).setValue(-1 * (float) tokens.get(i + 1).getValue());
                            } else {
                                tokens.get(i + 1).setValue(-1 * (int) tokens.get(i + 1).getValue());
                            }
                        }
                        deletedPositions.add(i);
                    } else if (tokens.get(i + 1).getStringToken().equalsIgnoreCase("var_name")) {
                        String aux = tokens.get(i).getStringToken() + tokens.get(i + 1).getStringToken();
                        tokens.get(i + 1).setValue(aux);
                        deletedPositions.add(i);
                    }
                }
            }
        }

        for (int i = 0; i < deletedPositions.size(); i++) {
            tokens.remove(deletedPositions.get(i) - i);
        }
    }

    public void showTokens() {
        for (Token token : tokens) {
            System.out.println("Token: "+token.getStringToken()+" in line: "+token.getLine()+" value: " + (token.getValue() == null ? "" : token.getValue()));
        }
    }

    public List<Token> getTokens() {
        return tokens;
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