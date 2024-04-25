package frontEnd;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String type;
    private List<Node> children;
    private Node parent;
    private Object value;

    public Node(String type) {
        this.type = type;
        this.children = new ArrayList<>();
        this.parent = null;
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
}
