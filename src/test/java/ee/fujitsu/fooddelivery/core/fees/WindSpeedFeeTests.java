package ee.fujitsu.fooddelivery.core.fees;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.FeeEvaluationContext;
import ee.fujitsu.fooddelivery.core.VehicleType;

class WindSpeedFeeTests {
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
    void shouldDetectWindRangeOverlap(double otherWindSpeedMin, double otherWindSpeedMax, boolean expected) {
        WindSpeedFee rule = windFee(10.0, 20.0);
        WindSpeedFee other = windFee(otherWindSpeedMin, otherWindSpeedMax);

        assertThat(rule.conditionsOverlap(other)).isEqualTo(expected);
    }

    @Test
    void shouldNotOverlapWhenCitiesDifferDespiteMatchingWindRanges() {
        WindSpeedFee rule = windFee(10.0, 20.0);
        WindSpeedFee other = windFee( 10.0, 20.0);
        other.setCity(City.TARTU);

        assertThat(rule.conditionsOverlap(other)).isFalse();
    }

    @Test
    void shouldNotOverlapWithDifferentRuleType() {
        WindSpeedFee rule = windFee(10.0, 20.0);

        assertThat(rule.conditionsOverlap(new AirTemperatureFee())).isFalse();
    }

    private WindSpeedFee windFee(double windSpeedMin, double windSpeedMaxExclusive) {
        WindSpeedFee rule = new WindSpeedFee();
        rule.setCity(City.TALLINN);
        rule.setVehicleType(VehicleType.BIKE);
        rule.setValidFrom(Instant.now());
        rule.setValidTo(null);
        rule.setLowerBoundInclusive(windSpeedMin);
        rule.setUpperBoundExclusive(windSpeedMaxExclusive);
        return rule;
    }
}
