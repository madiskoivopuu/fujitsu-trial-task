package ee.fujitsu.fooddelivery.core;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtraFees {
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant validFrom;
    private BigDecimal feeForTempLessThanMinusTenDegrees;
    private BigDecimal feeForTempBetweenMinusTenAndZeroDegrees;
    private BigDecimal feeForWindSpeedBetween10And20Mps;
    private BigDecimal feeForRain;
    private BigDecimal feeForSnowAndSleet;
}
