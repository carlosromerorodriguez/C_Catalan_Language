import backEnd.TAC;
import backEnd.TACGenerator;
import backEnd.TACToRISCConverter;
import frontEnd.global.ErrorHandler;
import frontEnd.lexic.CodeLine;
import frontEnd.lexic.Lexer;
import frontEnd.lexic.PreProcessing;
import frontEnd.lexic.TokenConverter;
import frontEnd.semantic.SemanticAnalizer;
import frontEnd.syntactic.FirstFollow;
import frontEnd.syntactic.Parser;

import java.util.List;


public class Main {
    private static final String FILE_PATH = "src/files/example4.ç";
    private static final String GRAMMAR_PATH = "src/files/grammar.json";
    private static final String MIPS_FILE_PATH = "src/mips.txt";

    public static void  main(String[] args) {
        // 1. Classe per convertir els tokens a la nostra representació interna
        TokenConverter tokenConverter = new TokenConverter();

        // 2. Classe per gestionar els errors
        ErrorHandler errorHandler = new ErrorHandler();

        // 3. Classe per llegir el fitxer d'entrada i eliminar els comentaris
        PreProcessing preProcessing = new PreProcessing(errorHandler, FILE_PATH);
        List<CodeLine> linesWithOutComments = preProcessing.readFile();

        // 4. Classe per convertir el fitxer d'entrada en tokens
        Lexer lexer = new Lexer(tokenConverter, errorHandler, linesWithOutComments);
        lexer.showTokens();

        // 5. Classe per carregar la gramàtica i omplir la taula de parsing
        FirstFollow inputFirstFollow = new FirstFollow(preProcessing.loadGrammar(GRAMMAR_PATH));
        Parser parser = new Parser(inputFirstFollow, tokenConverter, errorHandler, inputFirstFollow.getGrammar());
        parser.buildParsingTree(lexer.getTokens());
        parser.optimizeTree();
        parser.printTree();

        SemanticAnalizer semanticAnalizer = new SemanticAnalizer(parser.getSymbolTable(), errorHandler);
        semanticAnalizer.analizeSymbolTable();

        // Si trobem errors al frontend no continuem amb el backend
        /*if(errorHandler.hasErrors()) {
            errorHandler.printErrors();
            return;
        }*/

        TACGenerator tacGenerator = new TACGenerator();
        tacGenerator.generateTAC(parser.getSymbolTable().getAllTree());
        tacGenerator.printTAC();
        tacGenerator.processFunctionArguments(parser.getSymbolTable());
        //errorHandler.printErrors();

        TACToRISCConverter tacToRISCConverter = new TACToRISCConverter(MIPS_FILE_PATH);
        tacToRISCConverter.convertTAC(tacGenerator.getTAC());
        tacToRISCConverter.reprocessSubValues();

    }

}
