import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        //Reading input to list, converting to string
        /* 
        try {
        List<String> lines = Files.readAllLines(Path.of("input.txt"));
        String input = String.join("\n", lines);
        System.out.println(input);
        } catch (IOException e) {
            System.out.println("Input not found");
        }
        */

        try {
            List<String> lines = Files.readAllLines(Path.of("airports_lookup.csv"));
            String[] headerColumns = lines.get(0).split(",");
            
            //Make a hashmap for header columns and their indexes
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headerColumns.length; i++) {
                headerMap.put(headerColumns[i].trim(), i);
            }

            //Check that headerMap has every required header
            String[] requiredHeaders = {"name", "iso_country", "municipality", "icao_code", "iata_code", "coordinates"};
            for (String requiredHeader : requiredHeaders) {
                if (!headerMap.containsKey(requiredHeader)) {
                    System.out.println("Airport lookup malformed");
                }
            }
            
            //Read the data rows
            List<String> dataFields = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                boolean quotes = false;
                StringBuilder currentField = new StringBuilder();
                String dataRow = lines.get(i);
                for (int j = 0; j < dataRow.length(); j++) {
                    char currentChar = dataRow.charAt(j);
                    if (currentChar == '"') {
                        quotes = !quotes;
                    }
                }

            }

            System.out.println(Arrays.toString(headerColumns));
        } catch (IOException e) {
            System.out.println("error");
        }


    }
}
