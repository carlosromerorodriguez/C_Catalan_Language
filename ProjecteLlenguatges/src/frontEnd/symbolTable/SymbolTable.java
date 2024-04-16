package frontEnd.symbolTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    private Stack<Map<String, SymbolTableEntry>> scopeStack;

    public SymbolTable() {
        this.scopeStack = new Stack<>();
        this.scopeStack.push(new HashMap<>()); // Creem l'àmbit global inicial
    }

    // Entra a un nou àmbit
    public void enterScope() {
        scopeStack.push(new HashMap<>());
    }

    // Surt de l'àmbit actual
    public void leaveScope() {
        if (scopeStack.size() > 1) { // Assegura que no eliminem l'àmbit global
            scopeStack.pop();
        }
    }

    // Afegeix una entrada a l'àmbit actual
    public void addEntry(SymbolTableEntry entry) {
        if (entry == null || entry.getName() == null) {
            throw new IllegalArgumentException("Symbol entry or name cannot be null.");
        }
        scopeStack.peek().put(entry.getName(), entry);
    }

    // Obté una entrada buscant des de l'àmbit actual cap als àmbits més globals
    public SymbolTableEntry getEntry(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, SymbolTableEntry> currentScope = scopeStack.get(i);
            if (currentScope.containsKey(name)) {
                return currentScope.get(name);
            }
        }
        return null; // No s'ha trobat en cap àmbit
    }

    // Elimina una entrada de l'àmbit actual (opcional)
    public void removeEntry(String name) {
        if (scopeStack.peek().containsKey(name)) {
            scopeStack.peek().remove(name);
        }
    }

    @Override
    public String toString() {
        return "SymbolTable{" +
                "scopeStack=" + scopeStack +
                '}';
    }
}

