package ee.fujitsu.fooddelivery.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import ee.fujitsu.fooddelivery.dto.WeatherDataDto;
import ee.fujitsu.fooddelivery.exceptions.WeatherClientException;

@Component
public class WeatherDataClient {

    private final URI weatherApiUrl;
    private final HttpClient client;
    private final WeatherResponseParser responseParser;

    public WeatherDataClient(@Value("${weather-api.url}") URI weatherApiUrl, HttpClient client,
            WeatherResponseParser responseParser) {
        this.weatherApiUrl = weatherApiUrl;
        this.client = client;
        this.responseParser = responseParser;
    }

    /**
     * Fetches the weather data at current timestamp for all weather stations from Estonian Environment Agency's API.
     * @return List of weather observations from each weather station. 
     *      Individual observation fields may be null when some data from a weather station is unavailable
     * @throws WeatherClientException If the response cannot be fetched or parsed
     */
    public List<WeatherDataDto> getCurrentWeatherObservations() throws WeatherClientException {
        List<WeatherDataDto> weatherData = new ArrayList<>();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(weatherApiUrl)
            .build();

        try {
            HttpResponse<InputStream> response = client.send(request,
                HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new WeatherClientException("Weather API returned HTTP " + response.statusCode());
            }

            try (InputStream body = response.body()) {
                weatherData = responseParser.parseObservations(body);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherClientException("Weather API request was interrupted", e);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new WeatherClientException("Failed to retrieve weather observations", e);
        } catch(NumberFormatException e) {
            throw new WeatherClientException("Failed to parse numeric value in weather response", e);
        }

        return weatherData;
    }
}
