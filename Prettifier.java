import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Prettifier {
    public static void main(String[] args) {
        //Help flag check
        if (args.length == 1 && args[0].equals("-h")) {
            printUsage();
            return;
        }
        if (args.length != 3) {
            printUsage();
            return;
        }

        //Save the file arguments as paths
        Path inputPath = Path.of(args[0]);
        Path outputPath = Path.of(args[1]);
        Path csvPath = Path.of(args[2]);

        String input = null;
        //Make a map of icao/iata code > airport object
        Map<String, Airport> airportLookup = new HashMap<>();


        //Reading input to list, converting to string
        try {
            input = readInput(inputPath);
        } catch (IOException e) {
            System.out.println("Input not found");
            return;
        }

        //Load airport data to maps
        try {
            loadAirports(csvPath, airportLookup);
        } catch (IOException e) {
            System.out.println("Airport lookup not found");
            return;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        //Write processed text to output file
        String processedText = processText(input, airportLookup);

        try {
            writeOutput(outputPath, processedText);
        } catch (IOException e) {
            System.out.println("Output writing failed");
            return;
        }
    }

    //REPLACING INPUT TEXT WITH CORRESPONDING VALUES FROM MAP
    //4 regex helper functions to replace input text values with wanted customer-friendly values from airport lookup maps
    public static String replaceIataWithCity(String input, Map<String, Airport> airportLookup) {
        Pattern pattern = Pattern.compile("\\*#([A-Z]{3})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            Airport airport = airportLookup.get(code);
            String replacement;
            if (airport != null) {
                replacement = airport.getMunicipality();
            } else {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public static String replaceIcaoWithCity(String input, Map<String, Airport> airportLookup) {
        Pattern pattern = Pattern.compile("\\*##([A-Z]{4})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            Airport airport = airportLookup.get(code);
            String replacement; 
            if (airport != null) {
                replacement = airport.getMunicipality();
            } else {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public static String replaceIataWithAirport(String input, Map<String, Airport> airportLookup) {
        Pattern pattern = Pattern.compile("#([A-Z]{3})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            Airport airport = airportLookup.get(code);
            String replacement;
            if (airport != null) {
                replacement = airport.getName();
            } else {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public static String replaceIcaoWithAirport(String input, Map<String, Airport> airportLookup) {
        Pattern pattern = Pattern.compile("##([A-Z]{4})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            Airport airport = airportLookup.get(code);
            String replacement;
            if (airport != null) {
                replacement = airport.getName();
            } else {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    //3 helper methods for replacing time and date
    public static String replaceDates(String input) {
        //".*?" makes it stop at the first ")"
        Pattern pattern = Pattern.compile("D\\((.*?)\\)");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String iso = matcher.group(1);
            String replacement;
            try {
                OffsetDateTime dateTime = OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd MMM yyyy");
                replacement = dateTime.format(format);
            } catch (DateTimeParseException e) {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static String replaceTimes12(String input) {
        Pattern pattern = Pattern.compile("T12\\((.*?)\\)");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String iso = matcher.group(1);
            String replacement;
            try {
                OffsetDateTime dateTime = OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mma");
                String timePart = dateTime.format(timeFormat);
                String offsetPart = formatOffset(dateTime);
                replacement = timePart + " (" + offsetPart + ")";
            } catch (DateTimeParseException e) {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static String replaceTimes24(String input) {
        Pattern pattern = Pattern.compile("T24\\((.*?)\\)");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String iso = matcher.group(1);
            String replacement;
            try {
                OffsetDateTime dateTime = OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
                String timePart = dateTime.format(timeFormat);
                String offsetPart = formatOffset(dateTime);
                replacement = timePart + " (" + offsetPart + ")";
            } catch (DateTimeParseException e) {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    //Helper method for formatting Offset time
    private static String formatOffset(OffsetDateTime dateTime) {
        String offset = dateTime.getOffset().getId();
        if (offset.equals("Z")) {
            return "+00:00";
        } else {
            return offset;
        }
    }

    //Helper method for normalizing and trimming whitespace
    private static String trimWhiteSpace(String input) {
        //turn whitespace characters to \n
        input = input.replaceAll("[\\v\\f\\r]", "\n");
        //trim 3 or more blank lines to 2
        input = input.replaceAll("\n{3,}", "\n\n");
        return input;
    }

    //Usage message print
    private static void printUsage() {
        System.out.println("itinerary usage:");
        System.out.println("$ java Prettifier.java ./input.txt ./output.txt /airport-lookup.csv");
    }
    
    //Method for input text processing
    private static String processText(String input, Map<String, Airport> airportLookup) {
        //Processing the input text to output text format. ORDER IS IMPORTANT!
        String result = input;
        result = replaceIcaoWithCity(result, airportLookup);
        result = replaceIataWithCity(result, airportLookup);
        result = replaceIataWithAirport(result, airportLookup);
        result = replaceIcaoWithAirport(result, airportLookup);
        result = replaceDates(result);
        result = replaceTimes12(result);
        result = replaceTimes24(result);
        result = trimWhiteSpace(result);
        return result.strip();
    }

    //Method for reading the input file into a string
    private static String readInput(Path inputPath) throws IOException {
        List<String> lines = Files.readAllLines(inputPath);
        return String.join("\n", lines);
    }

    //Method for writing the output file
    private static void writeOutput(Path outputPath, String text) throws IOException {
        Files.write(outputPath, text.getBytes());
    }

    //Method for filling the airport lookup map with codes and their corresponding airport data objects
    private static void loadAirports(Path csvPath, Map<String, Airport> airportLookup) 
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

            //Save airport object by iata and icao codes
            airportLookup.put(airport.getIata(), airport);
            airportLookup.put(airport.getIcao(), airport);
        }
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
