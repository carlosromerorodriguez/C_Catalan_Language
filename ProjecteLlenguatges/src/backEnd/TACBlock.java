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
        for(TACEntry entry : entries) {
            if(entry.getType().equals("CONDITION")) {
                // Afegim el GOTO endLabel
                entry.setDestination(endLabel);
            }
        }
    }

    public void printBlock() {
        for (TACEntry entry : entries) {
            System.out.println("\t"+entry.toString());
        }
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<TACEntry> getEntries() {
        return this.entries;
    }
}
