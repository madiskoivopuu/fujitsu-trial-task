package ee.fujitsu.fooddelivery.service;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ee.fujitsu.fooddelivery.client.WeatherDataClient;
import ee.fujitsu.fooddelivery.dto.WeatherDataDto;
import ee.fujitsu.fooddelivery.exceptions.WeatherClientException;
import ee.fujitsu.fooddelivery.repository.WeatherDataRepository;

@Service
public class WeatherDataCollectionService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherDataCollectionService.class);
    private static final Set<String> SUPPORTED_STATIONS = Set.of(
        "Tallinn-Harku",
        "Tartu-Tõravere",
        "Pärnu"
    );

    private WeatherDataClient weatherDataClient;
    private WeatherDataRepository repository;

    public WeatherDataCollectionService(WeatherDataClient weatherDataClient, WeatherDataRepository repository) {
        this.weatherDataClient = weatherDataClient;
        this.repository = repository;
    }

    @Scheduled(cron="${job.weather-data-collection.cron-schedule}")
    private void collectWeatherData() {
        logger.info("Weather data collection job started");
        List<WeatherDataDto> weatherData;

        try {
            weatherData = weatherDataClient.getCurrentWeatherObservations();
        } catch(WeatherClientException e) {
            logger.warn("Service failed to collect weather data: ", e.getMessage());
            return;
        }

        repository.saveAll(weatherData.stream()
            .filter(dto -> SUPPORTED_STATIONS.contains(dto.getStationName()))
            .filter(dto -> dto.getAirTemperatureCelsius() != null && dto.getWeatherPhenomenon() != null && dto.getWindSpeedMetersPerSecond() != null)
            .toList()
        );

        logger.info("Weather data collection job ended");
    }
}
