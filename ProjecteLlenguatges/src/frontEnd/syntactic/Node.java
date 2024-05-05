package frontEnd.syntactic;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String type;
    private List<Node> children;
    private Node parent;
    private int line;
    private Object value;

    public Node(String type, int line) {
        this.type = type;
        this.children = new ArrayList<>();
        this.parent = null;
        this.line = line;
    }

    public void addChild(Node child) {
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
}
