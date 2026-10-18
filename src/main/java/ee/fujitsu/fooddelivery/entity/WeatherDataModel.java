package ee.fujitsu.fooddelivery.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Data 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherDataModel {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stationName;
    private Integer wmoCode;
    private Double airTemperatureCelsius;
    private Double windSpeedMetersPerSecond;
    private String weatherPhenomenon;
    private Instant observationTimestamp;

    public WeatherDataModel(String stationName, Integer wmoCode, Double airTemperatureCelsius, 
            Double windSpeedMetersPerSecond, String weatherPhenomenon, Instant observationTimestamp) {
        this.stationName = stationName;
        this.wmoCode = wmoCode;
        this.airTemperatureCelsius = airTemperatureCelsius;
        this.windSpeedMetersPerSecond = windSpeedMetersPerSecond;
        this.weatherPhenomenon = weatherPhenomenon;
        this.observationTimestamp = observationTimestamp;
    }

}
