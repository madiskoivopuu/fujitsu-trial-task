package ee.fujitsu.fooddelivery.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.fujitsu.fooddelivery.client.WeatherDataClient;
import ee.fujitsu.fooddelivery.client.Observations;
import ee.fujitsu.fooddelivery.client.Station;
import ee.fujitsu.fooddelivery.entity.WeatherDataModel;
import ee.fujitsu.fooddelivery.exceptions.WeatherClientException;
import ee.fujitsu.fooddelivery.mapper.StationToWeatherDataMapper;
import ee.fujitsu.fooddelivery.repository.WeatherDataRepository;

@ExtendWith (MockitoExtension.class)
public class WeatherDataCollectionJobTests {
    @Mock 
    private WeatherDataClient weatherDataClient;

    @Mock 
    private WeatherDataRepository repository;

    private WeatherDataCollectionJob weatherDataCollectionJob;

    @BeforeEach
    void setupTests() {
        weatherDataCollectionJob = new WeatherDataCollectionJob(
            weatherDataClient, repository, new StationToWeatherDataMapper()
        );
    }

    @Test 
    public void shouldSaveOnlySupportedStationDataWhenWeatherDataIsFetched() throws WeatherClientException {
        Observations weatherData = new Observations(
            1792257127,
            List.of(
                new Station("Tallinn-Harku", 26038, 10.8, 3.2, "Light rain"),
                new Station("Pakri", 26029, 10.5, 11.5, "Overcast"),
                new Station("Tartu-Tõravere", 26242, 10.0, 1.4, "Clear"),
                new Station("Pärnu", 41803, 10.7, 7.2, "Overcast")
            )
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
    public void shouldSaveOnlyCompleteWeatherDataWhenSomeWeatherDataIsIncomplete() throws WeatherClientException {
        Observations weatherData = new Observations(
            1792257127,
            List.of(
                new Station("Tallinn-Harku", 26038, 10.8, 3.2, "Light rain"),
                new Station("Pakri", 26029, 10.5, 11.5, "Overcast"),
                new Station("Tartu-Tõravere", 26242, 10.0, 1.4, "Clear"),
                new Station("Osmussaare", 26027, null, 7.2, null)
            )
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
