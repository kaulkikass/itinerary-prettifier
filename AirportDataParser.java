import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirportDataParser {

    //Method for filling the airport lookup map with codes and their corresponding airport data objects
    public static Map<String, Airport> loadAirports(Path csvPath) 
    throws IOException, IllegalArgumentException {

        //Reading the airport CSV line by line to a string
        List<String> lines = Files.readAllLines(csvPath);
        //Check if csv file has atleast 2 rows (header and data)
        if (lines.size() < 2) {
            throw new IllegalArgumentException("Airport lookup malformed");
        }

        //Saving the first line as header string array
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
                throw new IllegalArgumentException("Airport lookup malformed");
            }
        }

        //Getting indexes for every header field
        int nameIndex = headerMap.get("name");
        int cityIndex = headerMap.get("municipality");
        int iataIndex = headerMap.get("iata_code");
        int icaoIndex = headerMap.get("icao_code");
        int countryIndex = headerMap.get("iso_country");
        int coordinatesIndex = headerMap.get("coordinates");

        //Make a map of icao/iata code > airport object
        Map<String, Airport> airportLookup = new HashMap<>();
        
        //Read and parse the data rows
        for (int i = 1; i < lines.size(); i++) {
            String dataRow = lines.get(i);
            List<String> dataFields = parseCsvLine(dataRow);

            //Validate each data row size
            if (dataFields.size() != headerMap.size()) {
                throw new IllegalArgumentException("Airport lookup malformed");
            }

            //Validate no empty fields of data
            for (String field : dataFields) {
                if (field.trim().isEmpty()) {
                    throw new IllegalArgumentException("Airport lookup malformed");
                }
            }

            //Creating a new Airport object with data from the row
            Airport airport = new Airport(
                dataFields.get(nameIndex),
                dataFields.get(cityIndex),
                dataFields.get(iataIndex),
                dataFields.get(icaoIndex),
                dataFields.get(countryIndex),
                dataFields.get(coordinatesIndex)
            );

            //Save airport object by iata and icao codes into airport lookup map
            airportLookup.put(airport.getIata(), airport);
            airportLookup.put(airport.getIcao(), airport);
        }
        return airportLookup;
    }

    //Method for splitting a single line with quote handling
    private static List<String> parseCsvLine(String line) {
        List<String> dataFields = new ArrayList<>();
        boolean quotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);
            if (currentChar == '"') {
                quotes = !quotes;
            } else if (currentChar == ',' && !quotes) {
                dataFields.add(currentField.toString().trim());
                currentField.setLength(0);
            } else {
                currentField.append(currentChar);
            }
        }
        dataFields.add(currentField.toString().trim());
        return dataFields;

    }
}
