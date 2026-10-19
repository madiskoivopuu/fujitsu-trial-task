package ee.fujitsu.fooddelivery.utils;

import java.math.BigDecimal;
import java.time.Instant;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.RegionalBaseFee;
import ee.fujitsu.fooddelivery.core.VehicleType;
import ee.fujitsu.fooddelivery.core.WeatherPhenomenon;
import ee.fujitsu.fooddelivery.core.fees.AirTemperatureFee;
import ee.fujitsu.fooddelivery.core.fees.ExtraFeeRule;
import ee.fujitsu.fooddelivery.core.fees.WeatherPhenomenonFee;
import ee.fujitsu.fooddelivery.core.fees.WindSpeedFee;

/** Methods to simplify the creation of basefee/extrafee objects */
public final class FeeFactory {
    private FeeFactory() {
    }

    public static RegionalBaseFee baseFee(VehicleType vehicle, String amount) {
        return baseFee(City.TALLINN, vehicle, amount, Instant.EPOCH, null);
    }

    public static RegionalBaseFee baseFee(City city, VehicleType vehicle, String amount, Instant from, Instant to) {
        return new RegionalBaseFee(null, city, vehicle, from, to, new BigDecimal(amount));
    }

    public static AirTemperatureFee temperatureFee(VehicleType vehicle, double lower, double upper, String amount) {
        return temperatureFee(City.TALLINN, vehicle, lower, upper, amount, Instant.EPOCH, null);
    }

    public static AirTemperatureFee temperatureFee(City city, VehicleType vehicle, double lower, double upper,
            String amount, Instant from, Instant to) {
        AirTemperatureFee rule = new AirTemperatureFee();
        initializeRule(rule, city, vehicle, amount, false, from, to);
        rule.setLowerBoundInclusive(lower);
        rule.setUpperBoundExclusive(upper);
        return rule;
    }

    public static WindSpeedFee windFee(VehicleType vehicle, double lower, double upper, String amount) {
        return windFee(City.TALLINN, vehicle, lower, upper, amount, false, Instant.EPOCH, null);
    }

    public static WindSpeedFee windFee(City city, VehicleType vehicle, double lower, double upper,
            String amount, boolean forbidden, Instant from, Instant to) {
        WindSpeedFee rule = new WindSpeedFee();
        initializeRule(rule, city, vehicle, amount, forbidden, from, to);
        rule.setLowerBoundInclusive(lower);
        rule.setUpperBoundExclusive(upper);
        return rule;
    }

    public static WeatherPhenomenonFee phenomenonFee(VehicleType vehicle, WeatherPhenomenon phenomenon, String amount) {
        return phenomenonFee(City.TALLINN, vehicle, phenomenon, amount, false, Instant.EPOCH, null);
    }

    public static WeatherPhenomenonFee phenomenonFee(City city, VehicleType vehicle, WeatherPhenomenon phenomenon,
            String amount, boolean forbidden, Instant from, Instant to) {
        WeatherPhenomenonFee rule = new WeatherPhenomenonFee();
        initializeRule(rule, city, vehicle, amount, forbidden, from, to);
        rule.setWeatherPhenomenon(phenomenon);
        return rule;
    }

    private static void initializeRule(ExtraFeeRule rule, City city, VehicleType vehicle, String amount,
            boolean forbidden, Instant from, Instant to) {
        rule.setCity(city);
        rule.setVehicleType(vehicle);
        rule.setValidFrom(from);
        rule.setValidTo(to);
        rule.setFee(new BigDecimal(amount));
        rule.setVehicleUsageForbidden(forbidden);
    }
}
