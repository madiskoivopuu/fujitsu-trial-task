package ee.fujitsu.fooddelivery.exceptions;

/**
 * Thrown when weather conditions do not allow the usage of a specific vehicle
 */
public class VehicleUsageDisallowedException extends Exception {

    public VehicleUsageDisallowedException(String message) {
        super(message);
    }

    public VehicleUsageDisallowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
