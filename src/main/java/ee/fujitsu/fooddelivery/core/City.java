package ee.fujitsu.fooddelivery.core;

// The city model is now directly coupled to the station names of the 3rd party weather API
// This seems fine for a demo app since we only have 3 stations and one weather API
public enum City {
    TALLINN("Tallinn-Harku"),
    TARTU("Tartu-Tõravere"),
    PÄRNU("Pärnu");

    private final String stationName;

    City(String stationName) {
        this.stationName = stationName;
    }

    public String getStationName() {
        return stationName;
    }
}
