package ee.fujitsu.fooddelivery.entity;

import ee.fujitsu.fooddelivery.dto.WeatherDataDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity 
@Data 
public class WeatherDataModel {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stationName;
    private Integer wmoCode;
    private Double airTemperatureCelsius;
    private Double windSpeedMetersPerSecond;
    private String weatherPhenomenon;
    private Integer observationTimestamp;

    public WeatherDataModel(String stationName, Integer wmoCode, Double airTemperatureCelsius, 
            Double windSpeedMetersPerSecond, String weatherPhenomenon, Integer observationTimestamp) {
        this.stationName = stationName;
        this.wmoCode = wmoCode;
        this.airTemperatureCelsius = airTemperatureCelsius;
        this.windSpeedMetersPerSecond = windSpeedMetersPerSecond;
        this.weatherPhenomenon = weatherPhenomenon;
        this.observationTimestamp = observationTimestamp;
    }

    public static WeatherDataModel from(WeatherDataDto dto) {
        return new WeatherDataModel(
            dto.stationName(),
            dto.wmoCode(),
            dto.airTemperatureCelsius(),
            dto.windSpeedMetersPerSecond(),
            dto.weatherPhenomenon(),
            dto.observationTimestamp()
        );
    }
}
