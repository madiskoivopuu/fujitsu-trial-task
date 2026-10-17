package ee.fujitsu.fooddelivery.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Decided to share the same entity for persistence and weather client DTO since this data is stored without transformation in the database.
// Using a separate persistence model for this simple use would duplicate the same fields and it would add indirection to the code
@Entity
public class WeatherDataDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stationName;
    private Integer wmoCode;
    private Double airTemperatureCelsius;
    private Double windSpeedMetersPerSecond;
    private String weatherPhenomenon;
    private Integer observationTimestamp;

    protected WeatherDataDto() {} // Required by JPA

    public WeatherDataDto(String stationName, Integer wmoCode, Double airTemperatureCelsius, 
            Double windSpeedMetersPerSecond, String weatherPhenomenon, Integer observationTimestamp) {
        this.stationName = stationName;
        this.wmoCode = wmoCode;
        this.airTemperatureCelsius = airTemperatureCelsius;
        this.windSpeedMetersPerSecond = windSpeedMetersPerSecond;
        this.weatherPhenomenon = weatherPhenomenon;
        this.observationTimestamp = observationTimestamp;
    }

    public Long getId() {
        return id;
    }

    public String getStationName() {
        return stationName;
    }

    public Integer getWmoCode() {
        return wmoCode;
    }

    public Double getAirTemperatureCelsius() {
        return airTemperatureCelsius;
    }

    public Double getWindSpeedMetersPerSecond() {
        return windSpeedMetersPerSecond;
    }

    public String getWeatherPhenomenon() {
        return weatherPhenomenon;
    }

    public Integer getObservationTimestamp() {
        return observationTimestamp;
    }
}
