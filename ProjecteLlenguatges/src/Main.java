import frontEnd.*;

import java.util.List;
import java.util.Map;


public class Main {

    public static void  main(String[] args) {
        TokenConverter tokenConverter = new TokenConverter();
        ErrorHandler errorHandler = new ErrorHandler();
        PreProcessing preProcessing = new PreProcessing(errorHandler, "src/files/example5.ç");
        Lexer lexer = new Lexer(errorHandler, preProcessing.readFile(), tokenConverter);

        lexer.showTokens();

        System.out.println("\n\nERRORS:");
        errorHandler.printErrors();

        Map<String, List<List<String>>> grammar = preProcessing.loadGrammar("src/files/grammar2.json");

        FirstFollow firstFollow = new FirstFollow(grammar);
        firstFollow.FIRST();
        firstFollow.showFIRST();
        firstFollow.FOLLOW();
        System.out.println("\n\nFOLLOW:");
        firstFollow.showFOLLOW();

    }
}