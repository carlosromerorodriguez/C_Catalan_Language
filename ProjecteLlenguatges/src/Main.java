import frontEnd.ErrorHandler;
import frontEnd.Lexer;
import frontEnd.PreProcessing;
import frontEnd.TokenConverter;


public class Main {

    public static void  main(String[] args) {
        TokenConverter tokenConverter = new TokenConverter();
        ErrorHandler errorHandler = new ErrorHandler();
        PreProcessing preProcessing = new PreProcessing(errorHandler, "ProjecteLlenguatges/src/files/example1.ç");
        Lexer lexer = new Lexer(errorHandler, preProcessing.readFile(), tokenConverter);

        lexer.showTokens();

        System.out.println("\n\nERRORS:");
        errorHandler.printErrors();
    }
}