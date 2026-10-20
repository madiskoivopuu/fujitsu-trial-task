package ee.fujitsu.fooddelivery.core.fees;

import static org.assertj.core.api.Assertions.assertThat;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.VehicleType;
import ee.fujitsu.fooddelivery.core.WeatherPhenomenon;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class WeatherPhenomenonFeeTests {
    @Test
    public void shouldNotOverlapWithDifferentPhenomenon() {
        WeatherPhenomenonFee rule = phenomenonFee(WeatherPhenomenon.SNOW);
        WeatherPhenomenonFee other = phenomenonFee(WeatherPhenomenon.SLEET);

        assertThat(rule.conditionsOverlap(other)).isFalse();
    }

    @Test
    public void shouldOverlapWithSamePhenomenon() {
        WeatherPhenomenonFee rule = phenomenonFee(WeatherPhenomenon.RAIN);
        WeatherPhenomenonFee other = phenomenonFee(WeatherPhenomenon.RAIN);

        assertThat(rule.conditionsOverlap(other)).isTrue();
    }

    @Test
    public void shouldNotOverlapWithDifferentCityButSamePhenomenon() {
        WeatherPhenomenonFee rule = phenomenonFee(WeatherPhenomenon.RAIN);
        WeatherPhenomenonFee other = phenomenonFee(WeatherPhenomenon.RAIN);
        other.setCity(City.TARTU);

        assertThat(rule.conditionsOverlap(other)).isFalse();
    }

    private WeatherPhenomenonFee phenomenonFee(WeatherPhenomenon phenomenon) {
        WeatherPhenomenonFee rule = new WeatherPhenomenonFee();
        rule.setCity(City.TALLINN);
        rule.setVehicleType(VehicleType.BIKE);
        rule.setValidFrom(Instant.now());
        rule.setValidTo(null);
        rule.setWeatherPhenomenon(phenomenon);
        return rule;
    }
}
