package ee.fujitsu.fooddelivery.core;

public record WeatherConditions(
    double airTemperatureCelsius,
    double windSpeedMetersPerSecond,
    WeatherPhenomenon weatherPhenomenon
) {}
