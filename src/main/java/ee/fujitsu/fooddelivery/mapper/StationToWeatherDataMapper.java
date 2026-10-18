package ee.fujitsu.fooddelivery.mapper;

import org.springframework.stereotype.Component;

import ee.fujitsu.fooddelivery.client.Station;
import ee.fujitsu.fooddelivery.entity.WeatherDataModel;

@Component 
public class StationToWeatherDataMapper {
    public WeatherDataModel toWeatherDataModel(Station station, Integer observationTimestamp) {
        return new WeatherDataModel(
            station.stationName(),
            station.wmoCode(),
            station.airTemperatureCelsius(),
            station.windSpeedMetersPerSecond(),
            station.weatherPhenomenon(),
            observationTimestamp
        );
    }
}
