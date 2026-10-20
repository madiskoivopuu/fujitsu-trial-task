package ee.fujitsu.fooddelivery.core.fees;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.VehicleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AirTemperatureFeeTests {
    @ParameterizedTest
    @CsvSource({
            "10.0, 20.0, true",   // identical
            "12.0, 18.0, true",   // contained within
            "5.0,  25.0, true",   // contains the original
            "15.0, 25.0, true",   // partial overlap above
            "5.0,  15.0, true",   // partial overlap below
            "20.0, 30.0, false",  // touches upper boundary
            "0.0,  10.0, false",  // touches lower boundary
            "25.0, 30.0, false",  // entirely above
            "0.0,  5.0,  false"   // entirely below
    })
    public void shouldDetectTemperatureRangeOverlap(double otherWindSpeedMin, double otherWindSpeedMax, boolean expected) {
        AirTemperatureFee rule = temperatureFee(10.0, 20.0);
        AirTemperatureFee other = temperatureFee(otherWindSpeedMin, otherWindSpeedMax);

        assertThat(rule.conditionsOverlap(other)).isEqualTo(expected);
    }

    @Test
    public void shouldNotOverlapWhenCitiesDifferDespiteMatchingTemperatureRanges() {
        AirTemperatureFee rule = temperatureFee(10.0, 20.0);
        AirTemperatureFee other = temperatureFee(10.0, 20.0);
        other.setCity(City.TARTU);

        assertThat(rule.conditionsOverlap(other)).isFalse();
    }

    @Test
    public void shouldNotOverlapWithDifferentRuleType() {
        AirTemperatureFee rule = temperatureFee(10.0, 20.0);

        assertThat(rule.conditionsOverlap(new WindSpeedFee())).isFalse();
    }

    private AirTemperatureFee temperatureFee(double minTemperature, double maxTemperatureExclusive) {
        AirTemperatureFee rule = new AirTemperatureFee();
        rule.setCity(City.TALLINN);
        rule.setVehicleType(VehicleType.BIKE);
        rule.setValidFrom(Instant.now());
        rule.setValidTo(null);
        rule.setLowerBoundInclusive(minTemperature);
        rule.setUpperBoundExclusive(maxTemperatureExclusive);
        return rule;
    }
}
