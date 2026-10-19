package ee.fujitsu.fooddelivery.core;

import java.math.BigDecimal;
import java.util.List;

import ee.fujitsu.fooddelivery.core.fees.ExtraFeeRule;
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
     * @param context parameters that will be used to calculate the fee (e.g weather
     *                wind speed, weather phenomenon)
     * @param baseFee regional base fee for the given delivery city, vehicle type
     *                and delivery time
     * @param extraFees weather based extra fee amounts valid at the delivery time
     * @return total delivery fee in euros, including the base fee and all applicable
     *         weather based extra fees
     * @throws VehicleUsageDisallowedException if the weather conditions disallow the
     *         use of the selected vehicle
     */
    public BigDecimal calculateFee(FeeEvaluationContext context,
                                   RegionalBaseFee baseFee, List<ExtraFeeRule> extraFees) throws VehicleUsageDisallowedException {

        BigDecimal feesTotal = baseFee.getFee();
        for (ExtraFeeRule fee : extraFees) {
            if (fee.appliesWithin(context)) {
                feesTotal = feesTotal.add(fee.getFee());
            }
        }
        return feesTotal;
    }
}
