import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatter {

    //ANSI 
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String UL = "\u001B[4m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";

    //Method for ANSI styling
    public static String ansiFormatting(String text) {
    String out = text;

    //Timezone offsets (+/-HH:MM)
    out = out.replaceAll("\\((\\+|-)\\d{2}:\\d{2}\\)", CYAN + BOLD + "$0" + RESET);

    //12-hour times. Example "03:30AM"
    out = out.replaceAll("\\b(0?[1-9]|1[0-2]):[0-5]\\d[AP]M\\b", YELLOW + "$0" + RESET);

    //24-hour times. Example "17:54"
    out = out.replaceAll("\\b([01]\\d|2[0-3]):[0-5]\\d\\b", YELLOW + "$0" + RESET);

    //Dates. Example "11 Apr 2002"
    out = out.replaceAll("\\b\\d{2} [A-Za-z]{3} \\d{4}\\b", UL + GREEN + "$0" + RESET);

    //Bold "from" and "to"
    out = out.replaceAll("\\bfrom\\b", BOLD + "from" + RESET);
    out = out.replaceAll("\\bto\\b", BOLD + "to" + RESET);

    return out;
    }

    //Method for input text processing
    public static String processText(String input, Map<String, Airport> airportLookup, boolean details) {
        //Processing the input text to output text format. ORDER IS IMPORTANT!
        String result = input;
        result = replaceIcaoWithCity(result, airportLookup,details);
        result = replaceIataWithCity(result, airportLookup, details);
        result = replaceIataWithAirport(result, airportLookup, details);
        result = replaceIcaoWithAirport(result, airportLookup, details);
        result = replaceDates(result);
        result = replaceTimes12(result);
        result = replaceTimes24(result);
        result = trimWhiteSpace(result);
        return result.strip();
    }

    //REPLACING INPUT TEXT WITH CORRESPONDING VALUES FROM MAP
    //4 regex helper functions to replace input text values with wanted customer-friendly values from airport lookup maps
    private static String replaceIataWithCity(String input, Map<String, Airport> airportLookup, boolean details) {
        Pattern pattern = Pattern.compile("\\*#([A-Z]{3})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            Airport airport = airportLookup.get(code);
            String replacement;
            if (airport != null) {
                String base = airport.getMunicipality();
                replacement = displayName(base, airport, details);
            } else {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String replaceIcaoWithCity(String input, Map<String, Airport> airportLookup, boolean details) {
        Pattern pattern = Pattern.compile("\\*##([A-Z]{4})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            Airport airport = airportLookup.get(code);
            String replacement; 
            if (airport != null) {
                String base = airport.getMunicipality();
                replacement = displayName(base, airport, details);
            } else {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String replaceIataWithAirport(String input, Map<String, Airport> airportLookup, boolean details) {
        Pattern pattern = Pattern.compile("#([A-Z]{3})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            Airport airport = airportLookup.get(code);
            String replacement;
            if (airport != null) {
                String base = airport.getName();
                replacement = displayName(base, airport, details);
            } else {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String replaceIcaoWithAirport(String input, Map<String, Airport> airportLookup, boolean details) {
        Pattern pattern = Pattern.compile("##([A-Z]{4})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1);
            Airport airport = airportLookup.get(code);
            String replacement;
            if (airport != null) {
                String base = airport.getName();
                replacement = displayName(base, airport, details);
            } else {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    //3 helper methods for replacing time and date
    private static String replaceDates(String input) {
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

    private static String replaceTimes12(String input) {
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

    private static String replaceTimes24(String input) {
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

    //Helper for displaying the airport with or without country
    private static String displayName (String base, Airport a, boolean details) {
        if (!details) {
            return base;
        }
        return base + " (" + a.getIsoCountry() + ")";
    }
}
