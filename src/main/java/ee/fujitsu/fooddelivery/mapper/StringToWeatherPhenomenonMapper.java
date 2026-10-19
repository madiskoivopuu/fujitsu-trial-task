package ee.fujitsu.fooddelivery.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import ee.fujitsu.fooddelivery.core.WeatherPhenomenon;
import ee.fujitsu.fooddelivery.entity.WeatherDataModel;
import org.springframework.stereotype.Component;


@Component
public class StringToWeatherPhenomenonMapper {
    /**
     * Maps the weather phenomenon from Estonian Environment Agency's observations API to
     * our own WeatherPhenomenon enum
     *
     * @param value weather phenomenon from the agency's observations API
     *
     * @return WeatherPhenomenon enum
     */
    public WeatherPhenomenon toWeatherPhenomenon(String value) {
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
                WeatherPhenomenon.REGULAR;
        };
    }
}
