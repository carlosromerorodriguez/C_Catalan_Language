package frontEnd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.HashMap;
import java.util.Map;

public class PreProcessing {
    private final String filePath;
    private ErrorHandler errorHandler;
    private HashMap<String, String> tokensDictionary = new HashMap<>();
    int a;

    private void CreateMap() {
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

    public PreProcessing(ErrorHandler errorHandler, String filePath) {
        this.filePath = filePath;
        this.errorHandler = errorHandler;
        CreateMap();
    }

    public List<CodeLine> readFile() {
        List<CodeLine> codeLines = new ArrayList<>();
        try {
            codeLines = this.readAllLines();
        } catch (IOException e) {
            errorHandler.recordError("File not found", 0);
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
                    //penultimeLine.replace(operand.getKey(), operand.getValue());
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
            for (Map.Entry<String, List<List<String>>> entry : productions.entrySet()) {
                System.out.println("Name: " + entry.getKey());
                System.out.println("Estructura:");
                for (List<String> lista : entry.getValue()) {
                    System.out.println(lista);
                }
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return productions;
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
}

/* ------ FUNCIÓ ESBORRAR COMENTARIS COMPRIMIDA ------

public static String removeComments(String code) {
    String scanComments = "(xiuxiueja[^\n]*)|(comenta[\\s\\S]*?ficomenta)";
    String emptyLines = code.replaceAll(scanComments, "");
    return emptyLines.replaceAll("(?m)^[ \t]*\r?\n", "");
}
*/