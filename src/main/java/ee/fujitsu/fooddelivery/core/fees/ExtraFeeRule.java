package ee.fujitsu.fooddelivery.core.fees;

import ee.fujitsu.fooddelivery.core.FeeEvaluationContext;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.VehicleType;
import ee.fujitsu.fooddelivery.exceptions.VehicleUsageDisallowedException;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "extra_fee_rule_type")
@Getter
@Setter
public abstract class ExtraFeeRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private City city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private Instant validFrom;
    private Instant validTo;

    @Column(nullable = false)
    private BigDecimal fee;

    @Column(nullable = false)
    private boolean vehicleUsageForbidden;

    /**
     * Checks whether the city, vehicle and validity conditions overlap with
     * another instance of an extra fee rule.
     * Subclasses should check extra conditions in addition to these base
     * conditions by overriding this method and adding their own checks
     * 
     * @param other Other fee to check conditions against
     */
    public boolean conditionsOverlap(ExtraFeeRule other) {
        return other != null
            && getCity() == other.getCity()
            && getVehicleType() == other.getVehicleType()
            && (getValidTo() == null || other.getValidFrom().isBefore(getValidTo()))
            && (other.getValidTo() == null || getValidFrom().isBefore(other.getValidTo()));
    }

    /**
     * Checks if this specific rule applies in a given context.
     *
     * @throws VehicleUsageDisallowedException when vehicle usage within a given
     *        context is not allowed
     */
    public abstract boolean appliesWithin(FeeEvaluationContext context) throws VehicleUsageDisallowedException;
}
