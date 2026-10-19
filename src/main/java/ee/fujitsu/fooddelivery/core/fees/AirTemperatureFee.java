package ee.fujitsu.fooddelivery.core.fees;

import ee.fujitsu.fooddelivery.core.FeeEvaluationContext;
import ee.fujitsu.fooddelivery.exceptions.VehicleUsageDisallowedException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/** A configurable temperature range for an extra fee. */
@Entity
@DiscriminatorValue("air_temperature")
@Getter
@Setter
public class AirTemperatureFee extends ExtraFeeRule {
    @Column(name = "temperature_lower_bound")
    private double lowerBoundInclusive;
    @Column(name = "temperature_upper_bound")
    private double upperBoundExclusive;

    @Override
    public boolean appliesWithin(FeeEvaluationContext context) throws VehicleUsageDisallowedException {
        boolean matches = this.getVehicleType() == context.vehicleType()
            && NumberRangeHelpers.numberWithinRange(this.lowerBoundInclusive, this.upperBoundExclusive, context.airTemperatureCelsius());
        if (matches && isVehicleUsageForbidden()) {
            throw new VehicleUsageDisallowedException("Usage of selected vehicle type is forbidden");
        }
        return matches;
    }

    @Override
    public boolean conditionsOverlap(ExtraFeeRule obj) {
        return obj instanceof AirTemperatureFee rule
            && super.conditionsOverlap(rule)
            && NumberRangeHelpers.rangesOverlap(
                this.lowerBoundInclusive, this.upperBoundExclusive,
                rule.getLowerBoundInclusive(), rule.getUpperBoundExclusive()
            );
    }
}
