package ee.fujitsu.fooddelivery.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.ExtraFees;

@Repository
public interface ExtraFeeRepository extends JpaRepository<ExtraFees, Long> {
    /**
     * Returns the extra fees that were effective at the specified timestamp
     *
     * @param targetTimestamp instant at which the fee must be valid
     * @return the fee with the latest validity start at or before the target timestamp,
     *         or an empty Optional if no matching fee exists
     */
    @Query("""
        SELECT extrafees FROM ExtraFees extrafees
        WHERE extrafees.validFrom <= :targetTimestamp
          AND (extrafees.validTo IS NULL OR :targetTimestamp < extrafees.validTo)
        ORDER BY extrafees.validFrom DESC
        LIMIT 1
        """)
    Optional<ExtraFees> findLatestExtraFeesAtOrBeforeTimestamp(
        @Param("targetTimestamp") Instant targetTimestamp
    );
}
