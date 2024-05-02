import frontEnd.*;

import java.util.List;
import java.util.Map;


public class Main {
    private static final String FILE_PATH = "src/files/example1.ç";
    private static final String GRAMMAR_PATH = "src/files/grammar.json";

    public static void  main(String[] args) {
        TokenConverter tokenConverter = new TokenConverter();
        ErrorHandler errorHandler = new ErrorHandler();
        PreProcessing preProcessing = new PreProcessing(errorHandler, FILE_PATH);
        Lexer lexer = new Lexer(errorHandler, preProcessing.readFile(), tokenConverter);

        lexer.showTokens();

        System.out.println("\n\nERRORS:");
        errorHandler.printErrors();

        Map<String, List<List<String>>> grammar = preProcessing.loadGrammar(GRAMMAR_PATH);

        Parser parser = new Parser(new FirstFollow(grammar), tokenConverter, errorHandler);
        parser.printParseTable();
        parser.buildParsingTree(lexer.getTokens());
        errorHandler.printErrors();
        //parser.printTree();
    }
}
