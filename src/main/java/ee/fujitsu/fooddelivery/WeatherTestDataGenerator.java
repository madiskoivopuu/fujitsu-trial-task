package ee.fujitsu.fooddelivery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.entity.WeatherDataModel;
import ee.fujitsu.fooddelivery.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("testdata")
public class WeatherTestDataGenerator implements CommandLineRunner {
    private static final Instant FIRST_TIMESTAMP = Instant.parse("2026-06-28T00:00:00Z");
    private static final Instant SECOND_TIMESTAMP = Instant.parse("2026-07-05T00:00:00Z");

    private final WeatherDataRepository weatherDataRepository;

    /** Inserts sample observations before and after the sample fee version change. */
    @Override
    @Transactional
    public void run(String... args) {
        if (weatherDataRepository.count() == 0) {
            List<WeatherDataModel> observations = new ArrayList<>();
            for (City city : City.values()) {
                observations.add(new WeatherDataModel(
                    city.getStationName(), wmoCode(city), -1.0, 12.0, "Light rain", FIRST_TIMESTAMP
                ));
            }
            // temperature & wind speed change
            for (City city : City.values()) {
                observations.add(new WeatherDataModel(
                        city.getStationName(), wmoCode(city), 1.0, 8.7, "Light rain", SECOND_TIMESTAMP
                ));
            }
            weatherDataRepository.saveAll(observations);
        }
    }

    private int wmoCode(City city) {
        return switch (city) {
            case TALLINN -> 26038;
            case TARTU -> 26242;
            case PÄRNU -> 41803;
        };
    }
}
