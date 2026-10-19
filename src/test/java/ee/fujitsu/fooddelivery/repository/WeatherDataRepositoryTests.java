package ee.fujitsu.fooddelivery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import ee.fujitsu.fooddelivery.core.WeatherPhenomenon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import ee.fujitsu.fooddelivery.entity.WeatherDataModel;
import ee.fujitsu.fooddelivery.utils.RepositoryTestUtils;
import jakarta.persistence.EntityManager;
import ee.fujitsu.fooddelivery.core.City;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WeatherDataRepositoryTests {
    @Autowired
    private WeatherDataRepository repository;

    @Autowired 
    private EntityManager entityManager;

    @ParameterizedTest
    @CsvSource({
        "TALLINN, Tallinn-Harku",
        "TARTU, Tartu-Tõravere",
        "PÄRNU, Pärnu"
    })
    void shouldFindExactTimestampForEachCity(City city, String stationName) {
        saveObservation(stationName, Instant.ofEpochSecond(100));
        WeatherDataModel exact = saveObservation(stationName, Instant.ofEpochSecond(200));
        saveObservation(stationName, Instant.ofEpochSecond(300));
        RepositoryTestUtils.flushAndClear(entityManager);

        assertThat(repository.findLatestWeatherDataByCityAtOrBefore(city, Instant.ofEpochSecond(200)))
            .contains(exact);
    }

    @Test
    void shouldFindClosestEarlierObservationForRequestedCity() {
        saveObservation("Tallinn-Harku", Instant.ofEpochSecond(100));
        WeatherDataModel closest = saveObservation("Tallinn-Harku", Instant.ofEpochSecond(200));
        saveObservation("Tallinn-Harku", Instant.ofEpochSecond(300));
        RepositoryTestUtils.flushAndClear(entityManager);

        assertThat(repository.findLatestWeatherDataByCityAtOrBefore(City.TALLINN, Instant.ofEpochSecond(299)))
            .contains(closest);
    }

    @Test
    void shouldReturnMostRecentlySavedObservationWhenTimestampIsInFuture() {
        Instant timestamp = Instant.ofEpochSecond(200);
        saveObservation("Tallinn-Harku", timestamp);
        WeatherDataModel latest = saveObservation("Tallinn-Harku", timestamp);
        RepositoryTestUtils.flushAndClear(entityManager);

        assertThat(repository.findLatestWeatherDataByCityAtOrBefore(City.TALLINN, timestamp.plusSeconds(1200)))
            .contains(latest);
    }

    @Test
    void shouldReturnEmptyWhenOnlyFutureObservationsExist() {
        saveObservation("Tallinn-Harku", Instant.ofEpochSecond(300));
        RepositoryTestUtils.flushAndClear(entityManager);

        assertThat(repository.findLatestWeatherDataByCityAtOrBefore(City.TALLINN, Instant.ofEpochSecond(200)))
            .isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenCityHasNoObservations() {
        saveObservation("Tartu-Tõravere", Instant.ofEpochSecond(100));
        RepositoryTestUtils.flushAndClear(entityManager);

        assertThat(repository.findLatestWeatherDataByCityAtOrBefore(City.TALLINN, Instant.ofEpochSecond(200)))
            .isEmpty();
    }

    private WeatherDataModel saveObservation(String stationName, Instant timestamp) {
        return repository.save(new WeatherDataModel(stationName, 26038, 10.0, 3.0, "Clear", timestamp));
    }
}
