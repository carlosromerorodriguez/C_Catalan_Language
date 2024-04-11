package frontEnd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PreProcessing {
    private final String filePath;
    private ErrorHandler errorHandler;

    public PreProcessing(ErrorHandler errorHandler, String filePath) {
        this.filePath = filePath;
        this.errorHandler = errorHandler;
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
                line = line_split[0].replaceAll(" ", "").trim();
                if (line_split.length > 1 && !line_split[0].isEmpty()) {
                    resultLines.add(new CodeLine(codeLine.getLine(), line));
                }
            } else if (line.contains("ficomenta")) {
                String[] line_split = line.split("ficomenta", 2);
                line = line_split[1].replaceAll(" ", "").trim();
                if (line_split.length > 2 && !line.isEmpty()) {
                    resultLines.add(new CodeLine(codeLine.getLine(), line));
                }
                comentaInLine = false;
            } else if (line.contains("comenta")) {
                String[] line_split = line.split("comenta", 2);
                line = line_split[0].replaceAll(" ", "").trim();
                if (line_split.length > 1 && !line.isEmpty()) {
                    resultLines.add(new CodeLine(codeLine.getLine(), line));
                }
                comentaInLine = true;
            } else {
                if (!comentaInLine) {
                    resultLines.add(new CodeLine(codeLine.getLine(), line.replaceAll(" ", "").trim()));
                }
            }
        }

        return resultLines;
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
