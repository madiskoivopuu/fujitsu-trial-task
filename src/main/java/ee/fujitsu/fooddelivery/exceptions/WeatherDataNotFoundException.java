package ee.fujitsu.fooddelivery.exceptions;

/**
 * Thrown when weather data for a specific city at a certain timestamp could not be found
 */
public class WeatherDataNotFoundException extends Exception {

    public WeatherDataNotFoundException(String message) {
        super(message);
    }

    public WeatherDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
