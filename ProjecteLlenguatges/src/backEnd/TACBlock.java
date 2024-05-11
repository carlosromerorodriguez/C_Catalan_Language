package backEnd;

import java.util.ArrayList;
import java.util.List;

public class TACBlock {
    private List<TACEntry> entries;

    public TACBlock() {
        entries = new ArrayList<>();
    }

    public void add(TACEntry entry) {
        entries.add(entry);
    }

    public void processCondition(String endLabel) {
        //TODO: Mètode que processarà la condició de salt del bloc en cas de ser un bloc condicional
    }

    public void printBlock() {
        for (TACEntry entry : entries) {
            System.out.println("\t"+entry.toString());
        }
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
