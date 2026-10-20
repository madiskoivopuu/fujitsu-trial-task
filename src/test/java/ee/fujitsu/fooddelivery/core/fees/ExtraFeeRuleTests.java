package ee.fujitsu.fooddelivery.core.fees;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.FeeEvaluationContext;
import ee.fujitsu.fooddelivery.core.VehicleType;

class ExtraFeeRuleTests {
    private static final Instant NOW = Instant.now();
    private static final Instant HOUR_LATER = NOW.plusSeconds(3600);

    @Test
    public void shouldOverlapWhenCityVehicleAndValidityMatch() {
        ExtraFeeRule rule = rule(NOW, HOUR_LATER);
        ExtraFeeRule other = rule(NOW, HOUR_LATER);

        assertThat(rule.conditionsOverlap(other)).isTrue();
    }

    @Test
    public void shouldNotOverlapWhenOnlyCityDiffers() {
        ExtraFeeRule rule = rule(NOW, HOUR_LATER);
        ExtraFeeRule other = rule(NOW, HOUR_LATER);
        other.setCity(City.TARTU);


        assertThat(rule.conditionsOverlap(other)).isFalse();
    }

    @Test
    public void shouldNotOverlapWhenOnlyVehicleDiffers() {
        ExtraFeeRule rule = rule(NOW, HOUR_LATER);
        ExtraFeeRule other = rule(NOW, HOUR_LATER);
        other.setVehicleType(VehicleType.SCOOTER);

        assertThat(rule.conditionsOverlap(other)).isFalse();
    }

    @ParameterizedTest(name = "Other rule starts {0} ns relative to first one's end: overlap = {1}")
    @CsvSource({"-1, true", "0, false", "1, false"})
    public void shouldCompareOtherValidFromWithThisValidTo(long offsetNanos, boolean expected) {
        ExtraFeeRule rule = rule(NOW, HOUR_LATER);
        ExtraFeeRule other = rule(HOUR_LATER.plusNanos(offsetNanos), HOUR_LATER.plusSeconds(3600));

        assertThat(rule.conditionsOverlap(other)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "Other ends {0} ns relative to this start: overlap = {1}")
    @CsvSource({"-1, false", "0, false", "1, true"})
    public void shouldCompareThisValidFromWithOtherValidTo(long offsetNanos, boolean expected) {
        ExtraFeeRule rule = rule(NOW, HOUR_LATER);
        ExtraFeeRule other = rule(NOW.minusSeconds(3600), NOW.plusNanos(offsetNanos));

        assertThat(rule.conditionsOverlap(other)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "This has no end; other ends {0} ns relative to this start: overlap = {1}")
    @CsvSource({"-1, false", "0, false", "1, true"})
    public void shouldStillCheckOtherValidToWhenThisValidToIsNull(long offsetNanos, boolean expected) {
        ExtraFeeRule rule = rule(NOW, null);
        ExtraFeeRule other = rule(NOW.minusSeconds(3600), NOW.plusNanos(offsetNanos));

        assertThat(rule.conditionsOverlap(other)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "Other has no end; other starts {0} ns relative to this end: overlap = {1}")
    @CsvSource({"-1, true", "0, false", "1, false"})
    public void shouldStillCheckOtherValidFromWhenOtherValidToIsNull(long offsetNanos, boolean expected) {
        ExtraFeeRule rule = rule(NOW, HOUR_LATER);
        ExtraFeeRule other = rule(HOUR_LATER.plusNanos(offsetNanos), null);

        assertThat(rule.conditionsOverlap(other)).isEqualTo(expected);
    }

    @Test
    public void shouldOverlapWhenBothValidityPeriodsHaveNoEnd() {
        ExtraFeeRule rule = rule(NOW, null);
        ExtraFeeRule other = rule(HOUR_LATER, null);

        assertThat(rule.conditionsOverlap(other)).isTrue();
    }

    @Test
    public void shouldNotOverlapWithNullRule() {
        assertThat(rule(NOW, HOUR_LATER).conditionsOverlap(null)).isFalse();
    }

    private ExtraFeeRule rule(Instant validFrom, Instant validTo) {
        ExtraFeeRule rule = new TestExtraFeeRule();
        rule.setCity(City.TALLINN);
        rule.setVehicleType(VehicleType.BIKE);
        rule.setValidFrom(validFrom);
        rule.setValidTo(validTo);
        return rule;
    }

    // Can't instantiate an abstract class, so we need to extend it and override the appliesWithin method
    private static class TestExtraFeeRule extends ExtraFeeRule {
        @Override
        public boolean appliesWithin(FeeEvaluationContext context) {
            return false;
        }
    }
}
