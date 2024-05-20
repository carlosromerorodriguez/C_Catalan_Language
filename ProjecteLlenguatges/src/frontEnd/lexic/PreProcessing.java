package frontEnd.lexic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import global.errors.ErrorHandler;

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

/**
 *
 * Class that preprocesses the code before the lexical analysis.
 */
public class PreProcessing {
    /**
     * Path of the file to preprocess.
     */
    private final String filePath;
    /**
     * Error handler to record errors.
     */
    private final ErrorHandler errorHandler;
    /**
     * Dictionary of tokens to add spaces to.
     */
    private final HashMap<String, String> tokensDictionary;

    /**
     * Constructor of the class.
     * @param errorHandler Error handler to record errors.
     * @param filePath Path of the file to preprocess.
     */
    public PreProcessing(ErrorHandler errorHandler, String filePath) {
        this.filePath = filePath;
        this.errorHandler = errorHandler;
        this.tokensDictionary = new HashMap<>();
        this.addSpacesToSomeTokens();
    }

    /**
     * Adds spaces to some tokens.
     */
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

    /**
     * Reads all the lines of the file.
     * @return List of code lines.
     * @throws IOException If the file is not found.
     */
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

    /**
     * Reads the file and preprocesses it.
     * @return List of code lines.
     */
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

    /**
     * Loads the grammar from a file.
     * @param path Path of the file.
     * @return Map with the grammar.
     */
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