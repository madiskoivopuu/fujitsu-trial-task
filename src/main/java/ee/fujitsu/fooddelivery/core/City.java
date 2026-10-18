package ee.fujitsu.fooddelivery.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// The city model is now directly coupled to the station names of the 3rd party weather API
// This seems fine for a demo app since we only have 3 stations and one weather API
@Getter
@RequiredArgsConstructor
public enum City {
    TALLINN("Tallinn-Harku"),
    TARTU("Tartu-Tõravere"),
    PÄRNU("Pärnu");

    private final String stationName;
}
