import backEnd.TAC;
import frontEnd.global.ErrorHandler;
import frontEnd.lexic.CodeLine;
import frontEnd.lexic.Lexer;
import frontEnd.lexic.PreProcessing;
import frontEnd.lexic.TokenConverter;
import frontEnd.syntactic.FirstFollow;
import frontEnd.syntactic.Parser;

import java.util.List;


public class Main {
    private static final String FILE_PATH = "src/files/example7.ç";
    private static final String GRAMMAR_PATH = "src/files/grammar.json";

    public static void  main(String[] args) {
        // 1. Clase para convertir el input del usuario a token de nuestro lenguaje
        TokenConverter tokenConverter = new TokenConverter();

        // 2. Clase para almacenar los errores del proceso de compilación
        ErrorHandler errorHandler = new ErrorHandler();

        // 3. Clase para preprocesar el archivo de entrada y eliminar comentarios
        PreProcessing preProcessing = new PreProcessing(errorHandler, FILE_PATH);
        List<CodeLine> linesWithOutComments = preProcessing.readFile();

        // 4. Clase para convertir el archivo de entrada (sin comentarios) a tokens
        Lexer lexer = new Lexer(tokenConverter, errorHandler, linesWithOutComments);
        lexer.showTokens();
        errorHandler.printErrors();

        // 5. Clase para cargar la gramática y construir la tabla de análisis sintáctico
        FirstFollow inputFirstFollow = new FirstFollow(preProcessing.loadGrammar(GRAMMAR_PATH));
        Parser parser = new Parser(inputFirstFollow, tokenConverter, errorHandler);
            //parser.printParseTable();
        parser.buildParsingTree(lexer.getTokens());
            //parser.printTree();

        TAC tac = new TAC(parser.getSymbolTable());
        tac.buildTAC();
        errorHandler.printErrors();

    }
}
