package ee.fujitsu.fooddelivery.core;

import java.math.BigDecimal;

import ee.fujitsu.fooddelivery.exceptions.VehicleUsageDisallowedException;
import org.springframework.stereotype.Service;

/**
 * DeliveryFeeCalculator calculates the total delivery fee for a specific vehicle based on 
 * the city and weather based extra fees
 */
@Service
public class DeliveryFeeCalculator {
    /**
     * Calculates the total delivery fee for a vehicle by adding applicable weather
     * based extra fees to the regional base fee.
     *
     * @param vehicle type of vehicle used for the delivery
     * @param conditions weather conditions in the delivery city
     * @param baseFee regional base fee selected for the delivery city, vehicle type
     *                and delivery time
     * @param fees weather based extra fee amounts valid at the delivery time
     * @return total delivery fee in euros, including the base fee and all applicable
     *         weather based extra fees
     * @throws VehicleUsageDisallowedException if the weather conditions disallow the
     *         use of the selected vehicle
     */
    public BigDecimal calculateFee(VehicleType vehicle, WeatherConditions conditions,
                                   RegionalBaseFee baseFee, ExtraFees fees) throws VehicleUsageDisallowedException {

        return baseFee.getFee()
                .add(getAirTempExtraFee(vehicle, fees, conditions.airTemperatureCelsius()))
                .add(getWindSpeedExtraFee(vehicle, fees, conditions.windSpeedMetersPerSecond()))
                .add(getWeatherPhenomenonFee(vehicle, fees, conditions.weatherPhenomenon()));
    }

    private BigDecimal getAirTempExtraFee(VehicleType vehicle, ExtraFees fees, double airTemperatureCelsius) {
        if(vehicle != VehicleType.BIKE && vehicle != VehicleType.SCOOTER) 
            return BigDecimal.ZERO;

        if(airTemperatureCelsius < -10)
            return fees.getFeeForTempLessThanMinusTenDegrees();
        else if(-10 <= airTemperatureCelsius && airTemperatureCelsius <= 0)
            return fees.getFeeForTempBetweenMinusTenAndZeroDegrees();
        else
            return BigDecimal.ZERO;
    }

    private BigDecimal getWindSpeedExtraFee(VehicleType vehicle, ExtraFees fees, double windSpeedMetersPerSecond) throws VehicleUsageDisallowedException {
        if(vehicle != VehicleType.BIKE)
            return BigDecimal.ZERO;

        if(10 <= windSpeedMetersPerSecond && windSpeedMetersPerSecond <= 20)
            return fees.getFeeForWindSpeedBetween10And20Mps();
        else if(windSpeedMetersPerSecond > 20)
            throw new VehicleUsageDisallowedException("Usage of selected vehicle type is forbidden");
        else
            return BigDecimal.ZERO;
    }

    private BigDecimal getWeatherPhenomenonFee(VehicleType vehicle, ExtraFees fees, WeatherPhenomenon phenomenon) throws VehicleUsageDisallowedException {
        if(vehicle != VehicleType.BIKE && vehicle != VehicleType.SCOOTER) 
            return BigDecimal.ZERO;

        return switch (phenomenon) {
            case WeatherPhenomenon.SNOW, WeatherPhenomenon.SLEET -> fees.getFeeForSnowAndSleet();
            case WeatherPhenomenon.RAIN -> fees.getFeeForRain();
            case WeatherPhenomenon.GLAZE, WeatherPhenomenon.HAIL, WeatherPhenomenon.THUNDER -> throw new VehicleUsageDisallowedException("Usage of selected vehicle type is forbidden");
            default -> BigDecimal.ZERO;
        };
    }
}
