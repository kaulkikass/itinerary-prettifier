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

public class Main {
    public static void main(String[] args) {
        String input = null;
        //Make 2 maps. 1 for iata/icao code > airport name lookup, other for iata/icao code > city lookup
        Map<String, String> airportNameMap = new HashMap<>();
        Map<String, String> cityNameMap = new HashMap<>();


        //Reading input to list, converting to string
        try {
        List<String> lines = Files.readAllLines(Path.of("input.txt"));
        input = String.join("\n", lines);
        System.out.println(input);
        } catch (IOException e) {
            System.out.println("Input not found");
            return;
        }

        //Airport lookup CSV 
        try {
            //Reading the airport CSV line by line to a string
            List<String> lines = Files.readAllLines(Path.of("airports_lookup.csv"));
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
                    System.out.println("Airport lookup malformed");
                }
            }

            //Getting indexes for every header field
            int nameIndex = headerMap.get("name");
            int cityIndex = headerMap.get("municipality");
            int iataIndex = headerMap.get("iata_code");
            int icaoIndex = headerMap.get("icao_code");
            
            //Read the data rows
            for (int i = 1; i < lines.size(); i++) {
                List<String> dataFields = new ArrayList<>();
                boolean quotes = false;
                StringBuilder currentField = new StringBuilder();
                String dataRow = lines.get(i);
                for (int j = 0; j < dataRow.length(); j++) {
                    char currentChar = dataRow.charAt(j);
                    if (currentChar == '"') {
                        quotes = !quotes;
                    } else if (currentChar == ',' && !quotes) {
                        dataFields.add(currentField.toString());
                        currentField.setLength(0);
                    } else {
                        currentField.append(currentChar);
                    }
                }
                dataFields.add(currentField.toString());

                //Validate each data row size
                if (dataFields.size() != headerMap.size()) {
                    System.out.println("Airport lookup malformed");
                    return;
                }

                //Validate no empty fields of data
                for (String field : dataFields) {
                    if (field.trim().isEmpty()) {
                        System.out.println("Airport lookup malformed");
                        return;
                    }
                }

                //Save value of each data field in correct header field
                String name = dataFields.get(nameIndex);
                String city = dataFields.get(cityIndex);
                String iata = dataFields.get(iataIndex);
                String icao = dataFields.get(icaoIndex);

                //Save each iata and icao code with corresponding city and airport name
                airportNameMap.put(iata, name);
                airportNameMap.put(icao, name);
                cityNameMap.put(iata, city);
                cityNameMap.put(icao, city);

            }

        } catch (IOException e) {
            System.out.println("Airport lookup not found");
            return;
        }

        //Processing the input text to output text format. ORDER IS IMPORTANT!
        String processedText = replaceIcaoWithCity(input, cityNameMap);
        processedText = replaceIataWithCity(processedText, cityNameMap);
        processedText = replaceIataWithAirport(processedText, airportNameMap);
        processedText = replaceIcaoWithAirport(processedText, airportNameMap);
        processedText = replaceDates(processedText);
        processedText = replaceTimes12(processedText);
        processedText = replaceTimes24(processedText);
        processedText = trimWhiteSpace(processedText);


        //Write processed text to output file
        try {
            Files.write(Path.of("output.txt"), processedText.getBytes());
        } catch (IOException e) {
            System.out.println("Couldn't write output");
            return;
        }
    }

    //REPLACING INPUT TEXT WITH CORRESPONDING VALUES FROM MAP
    //4 regex helper functions to replace input text values with wanted customer-friendly values from airport lookup maps
    public static String replaceIataWithCity(String input, Map<String, String> cityNameMap) {
        Pattern pattern = Pattern.compile("\\*#([A-Z]{3})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            String replacement = cityNameMap.getOrDefault(code, matcher.group(0));
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);

        return result.toString();
    }

    public static String replaceIcaoWithCity(String input, Map<String, String> cityNameMap) {
        Pattern pattern = Pattern.compile("\\*##([A-Z]{4})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            String replacement = cityNameMap.getOrDefault(code, matcher.group(0));
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);

        return result.toString();
    }

    public static String replaceIataWithAirport(String input, Map<String, String> airportNameMap) {
        Pattern pattern = Pattern.compile("#([A-Z]{3})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            String replacement = airportNameMap.getOrDefault(code, matcher.group(0));
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);

        return result.toString();
    }

    public static String replaceIcaoWithAirport(String input, Map<String, String> airportNameMap) {
        Pattern pattern = Pattern.compile("##([A-Z]{4})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            String replacement = airportNameMap.getOrDefault(code, matcher.group(0));
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
}
