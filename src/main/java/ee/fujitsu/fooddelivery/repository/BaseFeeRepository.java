package ee.fujitsu.fooddelivery.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.RegionalBaseFee;
import ee.fujitsu.fooddelivery.core.VehicleType;

@Repository
public interface BaseFeeRepository extends JpaRepository<RegionalBaseFee, Long> {
    /**
     * Returns the regional base fee effective at the specified timestamp for the
     * given city and vehicle type.
     *
     * @param city delivery city
     * @param vehicleType type of vehicle used for the delivery
     * @param targetTimestamp instant at which the fee must be valid
     * @return the fee with the latest validity start at or before the target timestamp,
     *         or an empty Optional if no matching fee exists
     */
    @Query("""
        SELECT basefee FROM RegionalBaseFee basefee
        WHERE basefee.city = :city
          AND basefee.vehicleType = :vehicleType
          AND basefee.validFrom <= :targetTimestamp
          AND (basefee.validTo IS NULL OR :targetTimestamp < basefee.validTo)
        ORDER BY basefee.validFrom DESC
        """)
    Optional<RegionalBaseFee> findActiveBaseFee(
        @Param("city") City city,
        @Param("vehicleType") VehicleType vehicleType,
        @Param("targetTimestamp") Instant targetTimestamp
    );
}
