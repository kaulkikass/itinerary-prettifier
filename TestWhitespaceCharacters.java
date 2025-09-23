import java.nio.file.*;

public class TestWhitespaceCharacters {
    public static void main(String[] args) throws Exception {
    String text = "Start\u000BMiddle\fNext\rEnd";
    Files.write(Path.of("test-all.txt"), text.getBytes());
    }
}
