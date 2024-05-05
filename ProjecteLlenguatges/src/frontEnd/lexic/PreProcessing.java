package frontEnd.lexic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import frontEnd.global.ErrorHandler;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PreProcessing {
    private final String filePath;
    private final ErrorHandler errorHandler;
    private final HashMap<String, String> tokensDictionary;
    int a;

    public PreProcessing(ErrorHandler errorHandler, String filePath) {
        this.filePath = filePath;
        this.errorHandler = errorHandler;
        this.tokensDictionary = new HashMap<>();
        this.addSpacesToSomeTokens();
    }

    private void addSpacesToSomeTokens() {
        // Operators
        this.tokensDictionary.put("(", " ( ");
        this.tokensDictionary.put(")", " ) ");
        this.tokensDictionary.put("+", " + ");
        this.tokensDictionary.put("-", " - ");
        this.tokensDictionary.put("·", " · ");
        this.tokensDictionary.put("/", " / ");
        this.tokensDictionary.put(",", " , ");
        this.tokensDictionary.put("<", " < ");
        this.tokensDictionary.put(">", " > ");
        this.tokensDictionary.put("=", " = ");
        this.tokensDictionary.put(":", " : ");
    }

    private List<CodeLine> readAllLines() throws IOException {
        List<CodeLine> codeLines = new ArrayList<>();

        try (Stream<String> lines = Files.lines(Paths.get(this.filePath))) {
            int lineNumber = 1;
            for (String content : lines.toList()) {
                codeLines.add(new CodeLine(lineNumber++, content));
            }
        }

        return codeLines;
    }

    public List<CodeLine> readFile() {
        List<CodeLine> codeLines;
        try {
            codeLines = this.readAllLines();
        } catch (IOException e) {
            errorHandler.recordError("File not found", 0);
            return new ArrayList<>();
        }

        List<CodeLine> resultLines = new ArrayList<>();
        boolean comentaInLine = false;

        for (CodeLine codeLine : codeLines) {
            String line = codeLine.getContentLine();

            if (line.trim().isEmpty() || line.equals("\n")) {
                continue;
            }

            if (line.contains("xiuxiueja") && !comentaInLine) {
                String[] line_split = line.split("xiuxiueja", 2);
                line = line_split[0];
                if (line_split.length > 1 && !line_split[0].isEmpty()) {
                    resultLines.add(new CodeLine(codeLine.getLine(), line));
                }
            } else if (line.contains("ficomenta")) {
                String[] line_split = line.split("ficomenta", 2);
                line = line_split[1];
                if (line_split.length > 2 && !line.isEmpty()) {
                    resultLines.add(new CodeLine(codeLine.getLine(), line));
                }
                comentaInLine = false;
            } else if (line.contains("comenta")) {
                String[] line_split = line.split("comenta", 2);
                line = line_split[0];
                if (line_split.length > 1 && !line.isEmpty()) {
                    resultLines.add(new CodeLine(codeLine.getLine(), line));
                }
                comentaInLine = true;
            } else {
                if (!comentaInLine) {
                    resultLines.add(new CodeLine(codeLine.getLine(), line));
                }
            }

            String penultimeLine = resultLines.get(resultLines.size() - 1).getContentLine();
            for(Map.Entry<String, String> operand : tokensDictionary.entrySet()) {
                if (penultimeLine.contains(operand.getKey())) {
                    penultimeLine = penultimeLine.replace(operand.getKey(), operand.getValue());
                }
            }
        }

        return resultLines;
    }

    public Map<String, List<List<String>>> loadGrammar(String path){
        Gson gson = new GsonBuilder().create();
        Map<String, List<List<String>>> productions = new HashMap<>();
        try (FileReader reader = new FileReader(path)) {
            Type mapType = new TypeToken<Map<String, List<List<String>>>>() {}.getType();
            productions = gson.fromJson(reader, mapType);
        } catch (IOException e) {
            this.errorHandler.recordError("Grammar file not found", 0);
        }
        return productions;
    }
}

/* ------ FUNCIÓ ESBORRAR COMENTARIS COMPRIMIDA ------

public static String removeComments(String code) {
    String scanComments = "(xiuxiueja[^\n]*)|(comenta[\\s\\S]*?ficomenta)";
    String emptyLines = code.replaceAll(scanComments, "");
    return emptyLines.replaceAll("(?m)^[ \t]*\r?\n", "");
}
*/