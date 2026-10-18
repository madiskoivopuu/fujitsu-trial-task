package ee.fujitsu.fooddelivery.service;

import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ee.fujitsu.fooddelivery.client.WeatherDataClient;
import ee.fujitsu.fooddelivery.client.Observations;
import ee.fujitsu.fooddelivery.exceptions.WeatherClientException;
import ee.fujitsu.fooddelivery.mapper.StationToWeatherDataMapper;
import ee.fujitsu.fooddelivery.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2 
@RequiredArgsConstructor 
public class WeatherDataCollectionJob {
    private static final Set<String> SUPPORTED_STATIONS = Set.of(
        "Tallinn-Harku",
        "Tartu-Tõravere",
        "Pärnu"
    );

    private final WeatherDataClient weatherDataClient;
    private final WeatherDataRepository repository;
    private final StationToWeatherDataMapper mapper;

    /**
     * Periodically fetches weather data from the Estonian Environment Agency's API and saves weather data to database
     * Incomplete observation data is filtered out before saving to database
     */
    @Scheduled(cron="${job.weather-data-collection.cron-schedule}")
    public void collectWeatherData() {
        log.info("Weather data collection job started");
        Observations weatherData;

        try {
            weatherData = weatherDataClient.getCurrentWeatherObservations();
        } catch(WeatherClientException e) {
            log.warn("Service failed to collect weather data: ", e);
            return;
        }

        repository.saveAll(weatherData.stations().stream()
            .filter(station -> SUPPORTED_STATIONS.contains(station.stationName()))
            .filter(station -> station.airTemperatureCelsius() != null && station.weatherPhenomenon() != null && station.windSpeedMetersPerSecond() != null)
            .map(station -> mapper.toWeatherDataModel(station, weatherData.observationUnixTimestamp()))
            .toList()
        );

        log.info("Weather data collection job ended successfully");
    }
}
