package ee.fujitsu.fooddelivery.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.VehicleType;
import ee.fujitsu.fooddelivery.core.fees.ExtraFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtraFeeRepository extends JpaRepository<ExtraFeeRule, Long> {
    /**
     * Returns the extra fees that were effective at the specified timestamp
     * for a given city and vehicle type
     *
     * @param targetTimestamp instant at which the fee must be valid
     * @return the fee with the latest validity start at or before the target timestamp,
     *         or an empty Optional if no matching fee exists
     */
    @Query("""
        SELECT extrafees FROM ExtraFeeRule extrafees
        WHERE extrafees.city = :city
          AND extrafees.vehicleType = :vehicleType
          AND extrafees.validFrom <= :targetTimestamp
          AND (extrafees.validTo IS NULL OR :targetTimestamp < extrafees.validTo)
        ORDER BY extrafees.validFrom DESC
        """)
    Optional<List<ExtraFeeRule>> findApplicableExtraFees(
        @Param("city") City city,
        @Param("vehicleType") VehicleType vehicleType,
        @Param("targetTimestamp") Instant targetTimestamp
    );
}
