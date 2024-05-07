package backEnd;

import frontEnd.syntactic.Node;
import frontEnd.syntactic.symbolTable.Scope;
import frontEnd.syntactic.symbolTable.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class TAC {
    private List<TacExpression> tacs;
    private int tempTac = 0;
    private Node topNode;
    private SymbolTable symbolTable;
    public TAC(SymbolTable symbolTable) {
        this.tacs = new ArrayList<>();
        this.symbolTable = symbolTable;
        //this.topNode = node;
    }
    /*public generateTAC()) {
        List<String> tacs = new ArrayList<>();
        buildTAC(topNode, tacs);
        //printTAC(tacs);
    }

    public void buildTAC(Node node, List<String> tacs) {
        System.out.println("-------------TAC------------------" + "\n\n");
        //addTacs(this.symbolTable.getRootScope(), 0);
        switch(node.getType()){
            case "EXPRESSION":
                handleExpression(node, tacs);
                break;
            case "DECLARATION":
                handleDeclaration(node, tacs);
                break;
            case "IF":
                handleIf(node, tacs);
                break;
            case "WHILE":
                handleWhile(node, tacs);
                break;
            case "FOR":
                handleFor(node, tacs);
                break;
        }

        for (Node child : node.getChildren()) {
            buildTAC(child, tacs);
        }
    }
    private void handleExpression(Node node, List<String> tacs) {
        if (node.getChildren().size() == 2){
            Node left = node.getChildren().get(0);
            Node right = node.getChildren().get(2);
            Node operator = node.getChildren().get(1);

            String leftOperand = resolveExpression(left, tacInstructions);
            String rightOperand = resolveExpression(right, tacInstructions);
            String result = "t" + tempTac++;

            tacs.add(result + " = " + leftOperand + " " + operator.getValue() + " " + rightOperand);
            node.setValue(result);
        } else if (node.getChildren().isEmpty()) {  // Es un nodo hoja
            return node.getValue().toString();
        }
    }
    private String resolveExpression(Node node, List<String> tacs) {
    if (node.getChildren().isEmpty()) {
        return node.getValue().toString();
    } else {
        // Si no es hoja
        handleExpression(node, tacInstructions);
        return node.getValue().toString();
    }
*/

    public void buildTAC(){
        System.out.println("-------------TAC------------------" + "\n\n");
        addTacs(this.symbolTable.getRootScope(), 0);

    }

    private void addTacs(Scope scope, int depth){
        for (Scope TacScope: scope.getChildScopes()){
            System.out.println("--".repeat((depth*5)+1) + TacScope.toString((depth*5)+1));
            addTacs(TacScope, depth + 1);
        }
    }
}
