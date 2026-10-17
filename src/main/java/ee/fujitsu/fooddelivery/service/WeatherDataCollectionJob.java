package ee.fujitsu.fooddelivery.service;

import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ee.fujitsu.fooddelivery.client.WeatherDataClient;
import ee.fujitsu.fooddelivery.dto.WeatherDataDto;
import ee.fujitsu.fooddelivery.entity.WeatherDataModel;
import ee.fujitsu.fooddelivery.exceptions.WeatherClientException;
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

    /**
     * Periodically fetches weather data from the Estonian Environment Agency's API and saves weather data to database
     */
    @Scheduled(cron="${job.weather-data-collection.cron-schedule}")
    public void collectWeatherData() {
        log.info("Weather data collection job started");
        List<WeatherDataDto> weatherData;

        try {
            weatherData = weatherDataClient.getCurrentWeatherObservations();
        } catch(WeatherClientException e) {
            log.warn("Service failed to collect weather data: ", e.getMessage());
            return;
        }

        repository.saveAll(weatherData.stream()
            .filter(dto -> SUPPORTED_STATIONS.contains(dto.stationName()))
            .filter(dto -> dto.airTemperatureCelsius() != null && dto.weatherPhenomenon() != null && dto.windSpeedMetersPerSecond() != null)
            .map(dto -> WeatherDataModel.from(dto))
            .toList()
        );

        log.info("Weather data collection job ended successfully");
    }
}
