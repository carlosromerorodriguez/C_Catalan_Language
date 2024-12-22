package frontEnd.syntactic;

import global.symbolTable.entries.CallEntry;

import java.util.Stack;

/**
 * Parser control variable to handle the parser context while parsing and generating the AST
 */
public class ParserControlVariables {
    /**
     * The current context of the parser
     */
    public String context = "";
    /**
     * Boolean to know if it is the first token
     */
    public Boolean isFirstToken = false;
    /**
     * The Type declaration the parser is in
     */
    public String typeDeclaration = "";
    /**
     * The Last top symbol the parser processed
     */
    public String lastTopSymbol = "";
    /**
     * The Current top symbol the parser is processing
     */
    public String currentTopSymbol = "";
    /**
     * The Current varname the parser is processing
     */
    public String currentVarname = ""; // Per saber a quina variable s'ha de guardar l'expressió
    /**
     * Boolean to know if the parser has seen an equal statement
     */
    public Boolean equalSeen = false;
    /**
     * Boolean to know if the parser has seen a return statement
     */
    public Boolean retornSeen = false;
    /**
     * The Function type the parser is currently processing
     */
    public String functionType = "";
    /**
     * Boolean to know if the parser can change context
     */
    public Boolean canChangeContext = true;
    /**
     * Boolean to know if the parser is inside a condition
     */
    public Boolean insideCondition = false;
    /**
     * The Current conditional the parser is processing
     */
    public String currentConditional = "";
    /**
     * The Last var type seen in arguments.
     */
    public String lastVarTypeSeenInArguments = "";
    /**
     * Boolean to know if the parser is inside arguments
     */
    public Boolean isInArguments = false;
    /**
     * Boolean to know if the parser is inside arguments in a function sentence
     */
    public Boolean argumentsInFunctionSentence = false;
    /**
     * The Last top node the parser processed
     */
    public Node lastTopNode;
    /**
     * The Current top node the parser is processing
     */
    public Node currentTopNode;
    /**
     * The Current call entry the parser is processing
     */
    public CallEntry currentCallEntry;
    /**
     * Boolean to know if the parser is inside a call
     */
    public Boolean isCall = false;
    /**
     * Stack to store the call entries
     */
    public Stack<CallEntry> callEntryStack = new Stack<>();

    /**
     * Instantiates a new Parser control variables.
     */
    public ParserControlVariables() {
    }
}
