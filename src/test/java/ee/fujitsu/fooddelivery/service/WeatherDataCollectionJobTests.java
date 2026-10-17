package ee.fujitsu.fooddelivery.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.fujitsu.fooddelivery.client.WeatherDataClient;
import ee.fujitsu.fooddelivery.dto.WeatherDataDto;
import ee.fujitsu.fooddelivery.entity.WeatherDataModel;
import ee.fujitsu.fooddelivery.exceptions.WeatherClientException;
import ee.fujitsu.fooddelivery.repository.WeatherDataRepository;

@ExtendWith (MockitoExtension.class)
public class WeatherDataCollectionJobTests {
    @Mock 
    private WeatherDataClient weatherDataClient;

    @Mock 
    private WeatherDataRepository repository;

    @InjectMocks 
    private WeatherDataCollectionJob weatherDataCollectionJob;

    @Test 
    public void weatherDataFromSupportedStationsShouldBeSaved() throws WeatherClientException {
        List<WeatherDataDto> weatherData = List.of(
            new WeatherDataDto("Tallinn-Harku", 26038, 10.8, 3.2, "Light rain", 1792257127),
            new WeatherDataDto("Pakri", 26029, 10.5, 11.5, "Overcast", 1792257127),
            new WeatherDataDto("Tartu-Tõravere", 26242, 10.0, 1.4, "Clear", 1792257127),
            new WeatherDataDto("Pärnu", 41803, 10.7, 7.2, "Overcast", 1792257127)
        );

        when(weatherDataClient.getCurrentWeatherObservations()).thenReturn(weatherData);

        weatherDataCollectionJob.collectWeatherData();

        List<WeatherDataModel> expectedWeatherData = List.of(
            new WeatherDataModel("Tallinn-Harku", 26038, 10.8, 3.2, "Light rain", 1792257127),
            new WeatherDataModel("Tartu-Tõravere", 26242, 10.0, 1.4, "Clear", 1792257127),
            new WeatherDataModel("Pärnu", 41803, 10.7, 7.2, "Overcast", 1792257127)
        );
        verify(repository).saveAll(expectedWeatherData);
    }

    @Test 
    public void incompleteWeatherDataShouldNotBeSaved() throws WeatherClientException {
        List<WeatherDataDto> weatherData = List.of(
            new WeatherDataDto("Tallinn-Harku", 26038, 10.8, 3.2, "Light rain", 1792257127),
            new WeatherDataDto("Pakri", 26029, 10.5, 11.5, "Overcast", 1792257127),
            new WeatherDataDto("Tartu-Tõravere", 26242, 10.0, 1.4, "Clear", 1792257127),
            new WeatherDataDto("Osmussaare", 26027, null, 7.2, null, 1792257127)
        );

        when(weatherDataClient.getCurrentWeatherObservations()).thenReturn(weatherData);

        weatherDataCollectionJob.collectWeatherData();

        List<WeatherDataModel> expectedWeatherData = List.of(
            new WeatherDataModel("Tallinn-Harku", 26038, 10.8, 3.2, "Light rain", 1792257127),
            new WeatherDataModel("Tartu-Tõravere", 26242, 10.0, 1.4, "Clear", 1792257127)
        );
        verify(repository).saveAll(expectedWeatherData);
    }
}
