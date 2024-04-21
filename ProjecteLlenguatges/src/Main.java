import frontEnd.*;

import java.util.List;
import java.util.Map;


public class Main {
/*TODO Arreglar:
    enter: nombre = (((0 +10)  * 11)  +7/1) -10  ç
    negat en condicions
*/
    public static void  main(String[] args) {
        TokenConverter tokenConverter = new TokenConverter();
        ErrorHandler errorHandler = new ErrorHandler();
        PreProcessing preProcessing = new PreProcessing(errorHandler, "src/files/example3.ç");
        Lexer lexer = new Lexer(errorHandler, preProcessing.readFile(), tokenConverter);

        lexer.showTokens();

        System.out.println("\n\nERRORS:");
        errorHandler.printErrors();

        Map<String, List<List<String>>> grammar = preProcessing.loadGrammar("src/files/grammar.json");

        Parser parser = new Parser(new FirstFollow(grammar));
        parser.printParseTable();
        parser.buildParsingTree(lexer.getTokens());
    }
}
