package ee.fujitsu.fooddelivery.exceptions;

/**
 * Thrown when a fee coefficient could not be found in the database
 */
public class FeeNotFoundException extends Exception {

    public FeeNotFoundException(String message) {
        super(message);
    }

    public FeeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
