public class Airport {
    private final String name;
    private final String municipality;
    private final String iata;
    private final String icao;
    private final String isoCountry;
    private final String coordinates;

    public Airport(String name, String municipality, String iata, String icao, String isoCountry, String coordinates) {
        this.name = name;
        this.municipality = municipality;
        this.iata = iata;
        this.icao = icao;
        this.isoCountry = isoCountry;
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }
    public String getMunicipality() {
        return municipality;
    }
    public String getIata() {
        return iata;
    }
    public String getIcao() {
        return icao;
    }
    public String getIsoCountry() {
        return isoCountry;
    }
    public String getCoordinates() {
        return coordinates;
    }
}
