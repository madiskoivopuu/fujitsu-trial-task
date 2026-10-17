package ee.fujitsu.fooddelivery.exceptions;

public class WeatherClientException extends Exception {

    public WeatherClientException(String message) {
        super(message);
    }

    public WeatherClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
