package ee.fujitsu.fooddelivery.core.fees;

import ee.fujitsu.fooddelivery.core.FeeEvaluationContext;
import ee.fujitsu.fooddelivery.core.WeatherPhenomenon;
import ee.fujitsu.fooddelivery.exceptions.VehicleUsageDisallowedException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** A configurable weather phenomenon fee for a specific vehicle. */
@Entity
@DiscriminatorValue("weather_phenomenon")
@Getter
@Setter
public class WeatherPhenomenonFee extends ExtraFeeRule {
    @Enumerated(EnumType.STRING)
    @Column(name = "weather_phenomenon")
    private WeatherPhenomenon weatherPhenomenon;

    @Override
    public boolean appliesWithin(FeeEvaluationContext context) throws VehicleUsageDisallowedException {
        boolean matches = this.getVehicleType() == context.vehicleType()
            && weatherPhenomenon == context.weatherPhenomenon();
        if (matches && isVehicleUsageForbidden()) {
            throw new VehicleUsageDisallowedException("Usage of selected vehicle type is forbidden");
        }
        return matches;
    }

    @Override
    public boolean conditionsOverlap(ExtraFeeRule other) {
        return other instanceof WeatherPhenomenonFee rule
            && super.conditionsOverlap(other)
            && weatherPhenomenon == rule.weatherPhenomenon;
    }
}
