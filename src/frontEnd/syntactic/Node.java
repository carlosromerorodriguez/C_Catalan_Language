package frontEnd.syntactic;

import frontEnd.lexic.Token;

import java.util.*;

/**
 * Node class that represents a node in the AST
 */
public class Node {
    /**
     * The Type of the node.
     */
    private String type;
    /**
     * The Children of the node.
     */
    private List<Node> children;
    /**
     * The Parent of the node.
     */
    private Node parent;
    /**
     * The Line where the node is in the code file
     */
    private int line;
    /**
     * The Value of the node.
     */
    private Object value;
    /**
     * The Terminal symbols.
     */
    Set<String> terminalSymbols = new HashSet<>(Arrays.asList(
            "+", "-", "*", "/", "=", ";", ",", ":", "(", ")", "{", "}", "GREATER", "LOWER", "LOWER_EQUAL", "GREATER_EQUAL", "!", "==", "!=",
            "RETORN", "FUNCTION", "START", "END", "LITERAL", "VAR_NAME", "FOR", "DE", "FINS", "VAR_TYPE", "IF",
            "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR", "CALÇOT", "VOID", "FUNCTION_MAIN", "SUMANT", "RESTANT", "ENDELSE", "ENDIF",
            "PRINT", "STRING"
    ));

    /**
     * Instantiates a new Node.
     *
     * @param type the type
     * @param line the line
     */
    public Node(String type, int line) {
        this.type = type;
        this.children = new ArrayList<>();
        this.parent = null;
        this.line = line;
    }

    /**
     * Adds a child to the node.
     *
     * @param child the child
     */
    public void addChild(Node child) {
        if(terminalSymbols.contains(type)) {
            return;
        }
        children.add(child);
        child.parent = this;
    }

    /**
     * Gets children.
     *
     * @return the children
     */
    public List<Node> getChildren() {
        return children;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Prints the node
     */
    public void printTree() {
        print("", true);
    }

    /**
     * Prints the node
     */
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


    /**
     * Get line int.
     *
     * @return the int
     */
    public int getLine(){
        return this.line;
    }

    /**
     * Sets line.
     *
     * @param line the line
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Prunes epsilon paths of the node
     */
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

    /**
     * Method to check if the node should be removed when empty
     *
     * @return boolean indicating if the node should be removed when empty
     */
    private boolean shouldBeRemovedWhenEmpty() {
        return !terminalSymbols.contains(type);
    }

    /**
     * Collapses single child nodes.
     */
    public void collapseSingleChildNodes() {
        collapseHelper(this);
    }

    /**
     * Helper method to collapse single child nodes.
     *
     * @param node the node
     */
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

    /**
     * Method to check if a node is crucial and we cant collapse it
     *
     * @param node the node to check
     *
     * @return boolean indicating if the node is crucial
     */
    private boolean isCrucialNode(Node node) {
        return node.type.equalsIgnoreCase("if") || node.type.equalsIgnoreCase("while") || node.type.equalsIgnoreCase("for") || node.type.equalsIgnoreCase("function") || node.type.equalsIgnoreCase("crida") || node.type.equalsIgnoreCase("condició");
    }

    /**
     * Optimizes tree by reversing the children list
     */
    public void optimizeTree() {
        //Girem la llista de fills per a que quedin en ordre invers
        Collections.reverse(children);
        for (Node child : children) {
            child.optimizeTree();
        }
    }
}
