package frontEnd.syntactic;

import java.util.*;

public class Node {
    private String type;
    private List<Node> children;
    private Node parent;
    private int line;
    private Object value;
    Set<String> terminalSymbols = new HashSet<>(Arrays.asList(
            "+", "-", "*", "/", "=", ";", ",", ":", "(", ")", "{", "}", "GREATER", "LOWER", "LOWER_EQUAL", "GREATER_EQUAL", "!", "==", "!=",
            "RETORN", "FUNCTION", "START", "END", "LITERAL", "VAR_NAME", "FOR", "DE", "FINS", "VAR_TYPE", "IF",
            "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR", "CALÇOT", "VOID", "FUNCTION_MAIN", "SUMANT", "RESTANT"
    ));

    public Node(String type, int line) {
        this.type = type;
        this.children = new ArrayList<>();
        this.parent = null;
        this.line = line;
    }

    public void addChild(Node child) {
        if(terminalSymbols.contains(type)) {
            return;
        }
        children.add(child);
        child.parent = this;
    }

    public List<Node> getChildren() {
        return children;
    }

    public String getType() {
        return type;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void printTree(int level) {
        System.out.println(" ".repeat(level * 2) + type + " (" + value + ")");
        for (Node child : children) {
            child.printTree(level + 1);
        }
    }

    public Node getParent() {
        return this.parent;
    }
    public int getLine(){
        return this.line;
    }
    public void setLine(int line) {
        this.line = line;
    }

    public Object getValue() {
        return this.value;
    }

    public void setParent(Node topNode) {
        this.parent = topNode;
    }

    public Boolean contains(Node node) {
        if (this == node) {
            return true;
        }
        for (Node child : children) {
            if (child.contains(node)) {
                return true;
            }
        }
        return false;
    }

    public void optimize() {
        List<String> types = new ArrayList<>();
        for(Node child : children) {
            types.add(child.getType());
        }

       if(types.contains("ε")) {
           List<Node> toRemove = new ArrayList<>();
           for (Node child : children) {
               if(!child.getType().equals("ε")) {
                   toRemove.add(child);
               }
               /*if(doesntBelongToProduction(child.getType())) {
                   toRemove.add(child);
               }*/
           }
           children.removeAll(toRemove);
       } else {
           for(Node child : children) {
               child.optimize();
           }
       }
    }

    /*private Boolean doesntBelongToProduction(String type) {

    }*/
}
