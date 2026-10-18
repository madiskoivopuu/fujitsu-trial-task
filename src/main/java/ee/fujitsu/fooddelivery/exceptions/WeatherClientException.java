package ee.fujitsu.fooddelivery.exceptions;

/**
 * Thrown when the weather client fails to fetch or parse weather data 
 */
public class WeatherClientException extends Exception {

    public WeatherClientException(String message) {
        super(message);
    }

    public WeatherClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
