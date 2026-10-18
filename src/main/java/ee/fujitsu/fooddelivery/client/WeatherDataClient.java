package ee.fujitsu.fooddelivery.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ee.fujitsu.fooddelivery.exceptions.WeatherClientException;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.xml.XmlMapper;

@Component
@RequiredArgsConstructor  
public class WeatherDataClient {
    @Value("${weather-api.url}")
    private final URI weatherApiUrl;
    private final HttpClient client;
    private final XmlMapper mapper;

    /**
     * Fetches the latest available weather data for all weather stations from Estonian Environment Agency's API
     * @return An observations object which contains the unix timestamp and a list of observations for all weather stations
     * @throws WeatherClientException If the request fails, the API returns a non-200 status code or the response cannot be parsed
     */
    public Observations getCurrentWeatherObservations() throws WeatherClientException {
        Observations weatherData;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(weatherApiUrl)
            .build();

        try {
            HttpResponse<InputStream> response = client.send(request,
                HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    throw new WeatherClientException("Weather API returned HTTP " + response.statusCode());
                }

                weatherData = mapper.readValue(body, Observations.class);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherClientException("Weather API request was interrupted", e);
        } catch (IOException e) {
            throw new WeatherClientException("Failed to retrieve weather observations", e);
        } catch(JacksonException e) {
            throw new WeatherClientException("Failed to parse XML from API response", e);
        }

        return weatherData;
    }
}
