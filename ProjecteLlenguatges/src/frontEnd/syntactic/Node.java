package frontEnd.syntactic;

import frontEnd.lexic.Token;

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

    public void printTree() {
        print("", true);
    }

    private void print(String prefix, boolean isTail) {
        // Definición de colores ANSI
        String cyan = "\u001B[36m";
        String yellow = "\u001B[33m";
        String reset = "\u001B[0m";

        // Imprimir el tipo de nodo en verde y el valor en cian, si existe
        System.out.println(prefix + (isTail ? "└── " : "├── ") + yellow + type + reset +
                (value == null ? "" : " (" + cyan + value.toString() + reset + ")"));

        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (!children.isEmpty()) {
            children.get(children.size() - 1).print(prefix + (isTail ? "    " : "│   "), true);
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

    public void pruneEpsilonPaths() {
        Iterator<Node> iterator = children.iterator();
        while (iterator.hasNext()) {
            Node child = iterator.next();
            child.pruneEpsilonPaths();

            // Si su hijo es un nodo "ε" y ese nodo no tiene hijos, se elimina directamente
            if (child.type.equals("ε") && child.children.isEmpty()) {
                iterator.remove();
                // Se verifica si después de eliminar el nodo ε, el padre (este nodo) no tiene otros hijos significativos
                if (children.isEmpty()) {
                    // Si este nodo ahora no tiene hijos y no es un nodo terminal útil, también se marca para la eliminación
                    if (shouldBeRemovedWhenEmpty()) {
                        type = "ε"; // Marcar este nodo para que se elimine en la siguiente pasada
                    }
                }
            }
        }
    }

    private boolean shouldBeRemovedWhenEmpty() {
        return !terminalSymbols.contains(type);
    }

    public void collapseSingleChildNodes() {
        collapseHelper(this);
    }

    private void collapseHelper(Node node) {
        for (Node child : node.children) {
            // Se colapsan recusivamente todos los hijos del hijo actual
            collapseHelper(child);

            // Si el hijo tiene exactamente un hijo y no es un nodo crucial
            if (child.children.size() == 1 && !isCrucialNode(child)) {
                // Se reemplaza el hijo por su único hijo
                Node grandchild = child.children.getFirst();
                child.type = grandchild.type;
                child.value = grandchild.value;
                child.children = grandchild.children;
                // Ahora se establece el padre de todos los nuevos hijos a child
                for (Node grandchildNode : child.children) {
                    grandchildNode.parent = child;
                }
            }
        }
    }

    private boolean isCrucialNode(Node node) {
        return node.type.equalsIgnoreCase("if") || node.type.equalsIgnoreCase("while") || node.type.equalsIgnoreCase("for") || node.type.equalsIgnoreCase("function") || node.type.equalsIgnoreCase("crida") || node.type.equalsIgnoreCase("condició");
    }

    public Token<Object> findChildByType(String varName) {
        for (Node child : children) {
            if (child.getType().equals(varName)) {
                return new Token<>((String) child.getValue(), child.getLine());
            }
        }
        return null;
    }

    public void optimizeTree() {
        //Girem la llista de fills per a que quedin en ordre invers
        Collections.reverse(children);
        for (Node child : children) {
            child.optimizeTree();
        }
    }
}
