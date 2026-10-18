package ee.fujitsu.fooddelivery.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.entity.WeatherDataModel;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherDataModel, Long> {
    /**
     * Gets the weather observations for a specific city at or before a given timestamp
     * 
     * @param city delivery city
     * @param targetTimestamp targetTimestamp latest acceptable observation time
     * @return the city's latest weather observation at or before the target instant,
     * or an empty Optional if no such observation exists.
     */
    @Query("""
        SELECT weather FROM WeatherDataModel weather
        WHERE weather.stationName = :#{#city.stationName}
          AND weather.observationTimestamp <= :targetTimestamp
        ORDER BY weather.observationTimestamp DESC, weather.id DESC
        LIMIT 1
        """)
    Optional<WeatherDataModel> findLatestWeatherDataByCityAtOrBefore(
        @Param("city") City city,
        @Param("targetTimestamp") Instant targetTimestamp
    );
}
