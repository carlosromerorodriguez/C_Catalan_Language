package frontEnd.lexic;

import frontEnd.global.ErrorHandler;
import frontEnd.lexic.CodeLine;
import frontEnd.lexic.Token;
import frontEnd.lexic.TokenConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Lexer {
    private final String tokenPattern =
            "\\b(si|cert|fals|sino|mentre|per|fer|fi|enter|decimal|lletra|lletres|siono|res|Calçot|proces|retorn|crida|de|fins)\\b\n|" + // Palabras reservadas
            "([A-Za-zÀ-ú][A-Za-zÀ-ú0-9_]*)|" + // Identificadores
            "(\\d+(\\.\\d+)?)|" + // Números (decimales y enteros)
            "(!|==|!=|<=|>=|\\+|-|\\*|/|=|<|>|\\(|\\)|;|,|:)"; // Operadores y símbolos
    private final ErrorHandler errorHandler;
    private final List<CodeLine> codeLines;
    private final List<Token> tokens;
    private final TokenConverter tokenConverter;
    private int currentIndex;

    /**
     * Constructor for the Lexer class.
     * @param codeLines The source code to be tokenized.
     * @param tokenConverter converter of tokens
     */
    public Lexer(TokenConverter tokenConverter, ErrorHandler errorHandler, List<CodeLine> codeLines) {
        this.errorHandler = errorHandler;
        this.codeLines = codeLines;
        this.tokens = new ArrayList<>();
        this.tokenConverter = tokenConverter;
        this.currentIndex = 0;

        this.tokenize();         // Convierte el código fuente en tokens
        this.reClassifyTokens(); // Reclasifica los tokens para asignarles su tipo específico
        this.reProcessTokens();  // Procesa los tokens para unir los operadores + y - con los literales
    }

    /**
     * Tokenizes the source code.
     */


    private void tokenize() {
        Pattern pattern = Pattern.compile(this.tokenPattern);

        for (CodeLine codeLine : this.codeLines) {
            Matcher matcher = pattern.matcher(codeLine.getContentLine());

            String line = codeLine.getContentLine();
            int lastMatchEnd = 0; // Índice que indica el final del último match

            while (matcher.find(lastMatchEnd)) {
                String lexeme = matcher.group();
                String token = this.tokenConverter.convertLexemeToToken(lexeme);

                // Verificar si es un número primero
                if (lexeme.matches("-?\\d+(\\.\\d+)?")) {
                    checkIsFloatOrInteger(lexeme, codeLine);
                }
                // Se verifica si es un operador o un símbolo
                else if (lexeme.matches("[-+*/=<>!]|==|!=|<=|>=|\\(|\\)|;|,|:")) {
                    this.tokens.add(new Token<String>(token, codeLine.getLine(), lexeme, lexeme));
                }
                // Se verifica si es un booleano
                else if (lexeme.equals("cert") || lexeme.equals("fals")) {
                    Boolean value = lexeme.equals("cert");
                    this.tokens.add(new Token<Boolean>("literal", codeLine.getLine(), value, lexeme));
                }
                // Si no es un número, operador, símbolo o booleano, se verifica si es una palabra reservada
                else {
                    // Si el token no es igual al lexema, se agrega un token con el lexema
                    if (!token.equals(lexeme)) {
                        this.tokens.add(new Token<>(token, codeLine.getLine(), null, lexeme));
                    } else {
                        // Si el lexeme contiene carácteres prohibidos en nuestro lenguaje
                        if (lexeme.contains("ç") || lexeme.contains("ñ")) {
                            checkIsPermittedLexeme(lexeme, token, codeLine);
                        }
                        // Es un lexema aceptado en nuestro lenguaje
                        else {
                            // Es un FUNCTION_NAME o VAR_NAME
                            this.tokens.add(new Token<String>("name", codeLine.getLine(), lexeme, lexeme));
                        }
                    }
                }

                lastMatchEnd = matcher.end();
                line = line.replaceFirst(Pattern.quote(lexeme), "");
            }

            //Si encara queden coses a la linia ens guardem l'error
            if (!line.trim().isEmpty()) {
                errorHandler.recordError(line + " is not a statement.", codeLine.getLine());
            }
        }
    }

    private void reClassifyTokens() {
        for (int i = 0; i < tokens.size() - 1; i++) {
            Token token = tokens.get(i);
            String tokenType = token.getStringToken();

            if ("name".equals(tokenType)) {
                classifyNameToken(token, i);
            } else if ("function".equals(tokenType)) {
                classifyFunctionToken(token, i);
            }
        }
    }

    private void classifyNameToken(Token token, int index) {
        String auxToken = tokenConverter.getToken((String) token.getValue());
        if (!auxToken.isEmpty()) {
            token.setStringToken(auxToken);
            return;
        }

        // No hay cambio en el valor, entonces no es necesario asignarlo de nuevo
        if ((index < tokens.size() - 1) && "(".equals(tokens.get(index + 1).getStringToken().trim())) {
            token.setStringToken("function_name");
        } else if ((index > 0) && "=".equals(tokens.get(index - 1).getStringToken())) {
            token.setStringToken("literal");
        } else {
            token.setStringToken("var_name");
        }
    }

    private void classifyFunctionToken(Token token, int index) {
        if ((index < tokens.size() - 1) && "Calçot".equals(tokens.get(index + 1).getStringToken())) {
            token.setStringToken("function_main");
        }
    }

    private void checkIsPermittedLexeme(String lexeme, String token, CodeLine codeLine) {
        if (lexeme.equals("Calçot")) {
            tokens.add(new Token<String>(token, codeLine.getLine(), lexeme, lexeme));
        } else {
            errorHandler.recordError(lexeme + " contains invalid character.", codeLine.getLine());
        }
    }

    private void checkIsFloatOrInteger(String lexeme, CodeLine codeLine) {
        if (lexeme.contains(".")) {
            this.tokens.add(new Token<Float>("literal", codeLine.getLine(), Float.parseFloat(lexeme), lexeme));
        } else {
            this.tokens.add(new Token<Integer>("literal", codeLine.getLine(), Integer.parseInt(lexeme), lexeme));
        }
    }

    void reProcessTokens() {
        ArrayList<Integer> deletedPositions = new ArrayList<>();
        for (int i = 0; i < tokens.size() - 1; i++) {
            if (isPlusOrMinus(tokens.get(i))) {
                if (isUnaryOperatorContext(tokens.get(i - 1))) {
                    handleUnaryOperator(i, deletedPositions);
                }
            }
        }

        removeDeletedTokens(deletedPositions);
    }

    private boolean isPlusOrMinus(Token token) {
        return token.getStringToken().equals("+") || token.getStringToken().equals("-");
    }

    private boolean isUnaryOperatorContext(Token prevToken) {
        String prevTokenStr = prevToken.getStringToken();
        return prevTokenStr.matches("\\(|=|\\+|-|\\*|/|==|!=|,|!") ||
                prevTokenStr.equalsIgnoreCase("LOWER") ||
                prevTokenStr.equalsIgnoreCase("GREATER") ||
                prevTokenStr.equalsIgnoreCase("LOWER_EQUAL") ||
                prevTokenStr.equalsIgnoreCase("GREATER_EQUAL") ||
                prevTokenStr.equalsIgnoreCase("RETORN");
    }

    private void handleUnaryOperator(int index, ArrayList<Integer> deletedPositions) {
        Token nextToken = tokens.get(index + 1);
        if (nextToken.getStringToken().equals("literal")) {
            processLiteralToken(index, nextToken);
            deletedPositions.add(index);
        } else if (nextToken.getStringToken().equalsIgnoreCase("var_name")) {
            String combinedValue = tokens.get(index).getStringToken() + nextToken.getValue();
            nextToken.setValue(combinedValue);
            deletedPositions.add(index);
        }
    }

    private void processLiteralToken(int index, Token nextToken) {
        if (tokens.get(index).getStringToken().equals("+")) {
            nextToken.setValue(nextToken.getValue()); // Es redundante, pero se deja para claridad
        } else {
            // Apply negative sign to the numeric value
            if (nextToken.getValue() instanceof Float) {
                nextToken.setValue(-1 * (Float) nextToken.getValue());
            } else {
                nextToken.setValue(-1 * (Integer) nextToken.getValue());
            }
        }
    }

    private void removeDeletedTokens(ArrayList<Integer> deletedPositions) {
        for (int i = 0; i < deletedPositions.size(); i++) {
            tokens.remove(deletedPositions.get(i) - i);
        }
    }

    public void showTokens() {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_YELLOW = "\u001B[33m";
        String ANSI_PURPLE = "\u001B[35m";

        Map<Integer, List<String>> tokensByLine = new TreeMap<>();
        for (Token token : tokens) {
            int line = token.getLine();
            String tokenDisplay = " [" + ANSI_PURPLE + token.getStringToken() + ANSI_RESET +
                    (token.getValue() == null ? "]" : ANSI_GREEN + " , " + token.getValue() + ANSI_RESET + "]");

            if (!tokensByLine.containsKey(line)) {
                tokensByLine.put(line, new ArrayList<>());
            }
            tokensByLine.get(line).add(tokenDisplay);
        }

        // Se Imprimen los tokens línea por línea
        for (Map.Entry<Integer, List<String>> entry : tokensByLine.entrySet()) {
            System.out.print(ANSI_YELLOW + "(" + entry.getKey() + ")" + ANSI_RESET);
            for (String tokenStr : entry.getValue()) {
                System.out.print(tokenStr);
            }
            System.out.println();
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }
}