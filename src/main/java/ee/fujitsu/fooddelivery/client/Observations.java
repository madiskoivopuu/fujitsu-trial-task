package ee.fujitsu.fooddelivery.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;

import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonRootName("observations")
public record Observations(
    @JacksonXmlProperty(isAttribute = true, localName = "timestamp")
    Integer observationUnixTimestamp,
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty (localName = "station")
    List<Station> stations
) {}
