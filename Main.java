import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        //Reading input to list, converting to string
        try {
        List<String> lines = Files.readAllLines(Path.of("input.txt"));
        String input = String.join("\n", lines);
        System.out.println(input);
        } catch (IOException e) {
            System.out.println("Input not found");
        }

        


    }
}
