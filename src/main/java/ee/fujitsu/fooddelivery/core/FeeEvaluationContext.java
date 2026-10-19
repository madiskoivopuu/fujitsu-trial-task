package ee.fujitsu.fooddelivery.core;

public record FeeEvaluationContext(
        VehicleType vehicleType,
        double airTemperatureCelsius,
        double windSpeedMetersPerSecond,
        WeatherPhenomenon weatherPhenomenon
) {}
