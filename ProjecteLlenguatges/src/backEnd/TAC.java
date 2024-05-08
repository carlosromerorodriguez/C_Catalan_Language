package backEnd;

import frontEnd.syntactic.Node;
import frontEnd.syntactic.symbolTable.Scope;
import frontEnd.syntactic.symbolTable.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class TAC {
    private List<String> tacs;
    private int tempCounter = 0;
    private Node topNode;

    public TAC(Node topNode) {
        this.tacs = new ArrayList<>();
        this.topNode = topNode;
    }
    private void printTAC() {
        System.out.println("-------------TAC------------------" + "\n\n");
        for (String tac : tacs) {
            System.out.println(tac);
        }
    }

    private String generateTempVariable() {
        return "t" + (tempCounter++);
    }


    public void generateTAC() {
        Node rootNode = this.topNode;
        if (rootNode.getType().equals("sortida")) {
            for (Node child : rootNode.getChildren()) {
                if (child.getType().equals("llista de funcions")) {
                    for (Node func : child.getChildren()) {
                        buildTAC(func, 0);
                    }
                } else if (child.getType().equals("main")) {
                    buildTAC(child, 0);
                }
            }
        }
        printTAC();
    }


    public void buildTAC(Node node, int depth) {
        String indent = "  ".repeat(depth);
        //addTacs(this.symbolTable.getRootScope(), 0);
        switch (node.getType()) {
            case "main":
                tacs.add(indent + "enter main");
                break;
            case "funcio":
                handleFunction(node, depth);
                break;
            case "END":
                break;
            case "llista_sentencies":

                break;
            case "llista_sentencies_rest":

                break;
            case "ε":

                break;
            case "sentencia":

                break;
            case ";":

                break;
            case "assignació":
                handleAssignment(node, depth);
                break;
            case "continuació_assignació":

                break;
            case "assignació_final":

                break;
            case "següent_token":

                break;
            case "expressió":
                handleExpression(node, depth);
                break;
            case "terme":

                break;
            case "factor":

                break;
            case "literal":

                break;
            case "=":

                break;
            case "VAR_NAME":

                break;
            case ":":

                break;
            case "VAR_TYPE":

                break;
            case "START":

                break;
            case "(":

                break;
            case ")":

                break;
            case "CALÇOT":

                break;
            case "FUNCTION_MAIN":

                break;
        }

        for (Node child : node.getChildren()) {
            buildTAC(child, depth + 1);
        }
        if (node.getType().equals("main")) {
            tacs.add(indent + "exit main");
        }
    }

    private void handleFunction(Node node, int depth) {
        String funcName = "hola";
        tacs.add("  ".repeat(depth) + "enter function " + funcName);
        for (Node child : node.getChildren()) {
            buildTAC(child, depth + 1);
        }
        tacs.add("  ".repeat(depth) + "exit function " + funcName);
    }
    private void handleExpression(Node node, int depth) {
        String result = generateTempVariable();
        tacs.add("  ".repeat(depth) + result + " = eval_expression");
    }

    private void handleAssignment(Node node, int depth) {
        String varName = "a";
        String value = "4";
        String expressionResult = generateTempVariable();
        tacs.add("  ".repeat(depth) + varName + " = " + expressionResult);
        tacs.add("  ".repeat(depth + 1) + expressionResult + " = " + value);
    }



/*
    public void buildTAC(){
        System.out.println("-------------TAC------------------" + "\n\n");
        addTacs(this.symbolTable.getRootScope(), 0);

    }

    private void addTacs(Scope scope, int depth){
        for (Scope TacScope: scope.getChildScopes()){
            System.out.println("--".repeat((depth*5)+1) + TacScope.toString((depth*5)+1));
            addTacs(TacScope, depth + 1);
        }
    }*/
}
