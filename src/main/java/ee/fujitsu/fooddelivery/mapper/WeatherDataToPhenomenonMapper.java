package ee.fujitsu.fooddelivery.mapper;

import ee.fujitsu.fooddelivery.core.WeatherConditions;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import ee.fujitsu.fooddelivery.core.WeatherPhenomenon;
import ee.fujitsu.fooddelivery.entity.WeatherDataModel;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WeatherDataToPhenomenonMapper {
    default WeatherPhenomenon mapWeatherPhenomenon(String value) {
        if (value == null) {
            return WeatherPhenomenon.UNKNOWN;
        }

        return switch (value) {
            case "Light snowfall", "Moderate snowfall", "Heavy snowfall", 
                "Light snow shower", "Moderate snow shower", "Heavy snow shower" -> 
                WeatherPhenomenon.SNOW;

            case "Light shower", "Moderate shower", "Heavy shower", 
                "Light rain", "Moderate rain", "Heavy rain" ->
                WeatherPhenomenon.RAIN;

            case "Thunder", "Thunderstorm" -> 
                WeatherPhenomenon.THUNDER;

            case "Light sleet", "Moderate sleet" ->
                WeatherPhenomenon.SLEET;

            case "Glaze" -> 
                WeatherPhenomenon.GLAZE;

            case "Hail" -> 
                WeatherPhenomenon.HAIL;

            default -> 
                WeatherPhenomenon.UNKNOWN;
        };
    }

    public WeatherConditions toWeatherPhenomenon(WeatherDataModel model);
}
