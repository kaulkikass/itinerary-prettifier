import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Prettifier {
    public static void main(String[] args) {
        //Help flag check
        if (args.length == 1 && args[0].equals("-h")) {
            printUsage();
            return;
        }
        //Atleast 3 arguments and not more than 4 check
        if (args.length < 3 || args.length > 4) {
            printUsage();
            return;
        }

        //Save the file arguments as paths
        Path inputPath = Path.of(args[0]);
        Path outputPath = Path.of(args[1]);
        Path csvPath = Path.of(args[2]);

        //Optional details flag check
        boolean details = false;
        if (args.length == 4) {
            if (args[3].equals("--details")) {
                details = true;
            } else {
                printUsage();
                return;
            }
        }

        String input = null;
        

        //Reading input to list, converting to string
        try {
            input = FileUtils.readInput(inputPath);
        } catch (IOException e) {
            System.out.println("Input not found");
            return;
        }

        //Map for code > airport object
        Map<String, Airport> airportLookup = new HashMap<>();
        //Load airport data to map
        try {
            airportLookup = AirportDataParser.loadAirports(csvPath);
        } catch (IOException e) {
            System.out.println("Airport lookup not found");
            return;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        //Processing the text
        String processedText = TextFormatter.processText(input, airportLookup, details);
        //Write processed text to output file
        try {
            FileUtils.writeOutput(outputPath, processedText);
        } catch (IOException e) {
            System.out.println("Output writing failed");
            return;
        }
    }

    //Usage message print
    private static void printUsage() {
        System.out.println("itinerary usage:");
        System.out.println("Default usage:");
        System.out.println("$ java Prettifier.java ./input.txt ./output.txt /airport-lookup.csv");
        System.out.println("Bonus usage(iso country):");
        System.out.println("$ java Prettifier.java ./input.txt ./output.txt /airport-lookup.csv --details");
    }
}
