package ee.fujitsu.fooddelivery.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties (ignoreUnknown = true)
public record Station(
    @JacksonXmlProperty(localName = "name")
    String stationName,

    @JacksonXmlProperty(localName = "wmocode")
    Integer wmoCode,

    @JacksonXmlProperty(localName = "airtemperature")
    Double airTemperatureCelsius,

    @JacksonXmlProperty(localName = "windspeed")
    Double windSpeedMetersPerSecond,

    @JacksonXmlProperty(localName = "phenomenon")
    String weatherPhenomenon
) {}
