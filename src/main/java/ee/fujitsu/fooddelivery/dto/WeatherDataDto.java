package ee.fujitsu.fooddelivery.dto;

public record WeatherDataDto(
    String stationName,
    Integer wmoCode,
    Double airTemperatureCelsius,
    Double windSpeedMetersPerSecond,
    String weatherPhenomenon,
    Integer observationTimestamp
) {}
