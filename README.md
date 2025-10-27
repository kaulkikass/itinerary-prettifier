# Itinerary Prettifier

A CLI tool that takes flight itinerary data as input, prettifies the contents for customers and outputs the customer version.

It replaces airport codes with airport names or cities, formats ISO8601 date/time strings to customer-friendly variants, normalizes and cleans excess whitespace.

Also has an option to display country name.

## Features

Airport name lookup - Replaces #IATA/##ICAO codes with airport names.
City name lookup - Replaces *#IATA/*##ICAO with city names.
Date and time formatting - D(YYYY-MM-DDThh:mm±hh:mm) → dd MMM yyyy and hh:mma (offset) or HH:mm (offset).
Whitespace normalizing and trimming - Collapses excess blank lines and normalises \v, \f, \r to \n.
Optional --details feature - --details appends ISO country codes next to airport and city names.
Optional ANSI styling - When output printed to stdout, dates/times/offsets are coloured for readability

## Usage

Default usage:
$ java Prettifier.java ./input.txt ./output.txt ./airport-lookup.csv

Optional --details country functionality:
$ java Prettifier.java ./input.txt ./output.txt ./airport-lookup.csv --details

Examples:
$ java Prettifier.java input.txt output.txt airports.csv
$ java Prettifier.java input.txt output.txt airports.csv --details