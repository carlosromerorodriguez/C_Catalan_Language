package frontEnd.syntactic;

import frontEnd.syntactic.symbolTable.CallEntry;

public class ParserControlVariables {
    public String context = "";
    public Boolean isFirstToken = false;
    public String typeDeclaration = "";
    public String lastTopSymbol = "";
    public String currentTopSymbol = "";
    public String currentVarname = ""; // Per saber a quina variable s'ha de guardar l'expressió
    public Boolean equalSeen = false;
    public Boolean retornSeen = false;
    public String functionType = "";
    public Boolean canChangeContext = true;
    public Boolean insideCondition = false;
    public String currentConditional = "";
    public String lastVarTypeSeenInArguments = "";
    public Boolean isInArguments = false;
    public Boolean argumentsInFunctionSentence = false;
    public Node lastTopNode;
    public Node currentTopNode;
    public CallEntry currentCallEntry;
    public Boolean isCall = false;

    public ParserControlVariables() {
    }
}
