package ee.fujitsu.fooddelivery.mapper;

import java.time.Instant;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ee.fujitsu.fooddelivery.client.Station;
import ee.fujitsu.fooddelivery.entity.WeatherDataModel;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StationToWeatherDataMapper {
    default Instant toInstant(Integer epochSeconds) {
        return epochSeconds == null
            ? null
            : Instant.ofEpochSecond(epochSeconds);
    }

    @Mapping(ignore = true, target = "id")
    public WeatherDataModel toWeatherDataModel(Station station, Integer observationTimestamp);
}
