import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileUtils {

    //Method for reading the input file into a string
    public static String readInput(Path inputPath) throws IOException {
        List<String> lines = Files.readAllLines(inputPath);
        return String.join("\n", lines);
    }

    //Method for writing the output file
    public static void writeOutput(Path outputPath, String text) throws IOException {
        Files.write(outputPath, text.getBytes());
    }
}
