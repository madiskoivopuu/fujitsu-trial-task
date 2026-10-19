package ee.fujitsu.fooddelivery.core.fees;

import ee.fujitsu.fooddelivery.core.FeeEvaluationContext;
import ee.fujitsu.fooddelivery.exceptions.VehicleUsageDisallowedException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/** A configurable wind speed fee for a specific vehicle. */
@Entity
@DiscriminatorValue("wind_speed")
@Getter
@Setter
public class WindSpeedFee extends ExtraFeeRule {
    @Column(name = "wind_lower_bound")
    private double lowerBoundInclusive;
    @Column(name = "wind_upper_bound")
    private double upperBoundExclusive;

    @Override
    public boolean appliesWithin(FeeEvaluationContext context) throws VehicleUsageDisallowedException {
        boolean matches = NumberRangeHelpers.numberWithinRange(this.lowerBoundInclusive, this.upperBoundExclusive, context.windSpeedMetersPerSecond());
        if (matches && isVehicleUsageForbidden()) {
            throw new VehicleUsageDisallowedException("Usage of selected vehicle type is forbidden");
        }

        return matches;
    }

    @Override
    public boolean conditionsOverlap(ExtraFeeRule other) {
        return other instanceof WindSpeedFee rule
                && super.conditionsOverlap(rule)
                && NumberRangeHelpers.rangesOverlap(
                    this.lowerBoundInclusive, this.upperBoundExclusive,
                    rule.getLowerBoundInclusive(), rule.getUpperBoundExclusive()
            );
    }
}
