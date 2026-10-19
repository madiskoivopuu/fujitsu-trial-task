package ee.fujitsu.fooddelivery.controller;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.VehicleType;
import ee.fujitsu.fooddelivery.exceptions.FeeNotFoundException;
import ee.fujitsu.fooddelivery.exceptions.VehicleUsageDisallowedException;
import ee.fujitsu.fooddelivery.exceptions.WeatherDataNotFoundException;
import ee.fujitsu.fooddelivery.service.DeliveryFeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping(path = "/api/delivery-fee", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Log4j2 
public class DeliveryFeeController {
    private final DeliveryFeeService deliveryFeeService;

    /**
     * Calculates the total delivery fee for the specified city and vehicle type,
     * using the base fees effective at the target time and the latest weather
     * observation at or before that time.
     *
     * @param city delivery city: TALLINN, TARTU, or PÄRNU
     * @param vehicleType delivery vehicle: CAR, SCOOTER, or BIKE
     * @param targetTime optional value, used to select the fees and weather observations 
     *        that were valid at that specific time.
     * @return a response containing the total delivery fee or an error response containing 
     *        a reason why the request could not complete successfully
     */
    @GetMapping
    public ResponseEntity<?> calculateDeliveryFee(
        @RequestParam("city") City city,
        @RequestParam("vehicleType") VehicleType vehicleType,
        @RequestParam(value = "targetTime", required = false) Instant targetTime) {
        try {
            return ResponseEntity.ok(
                new DeliveryFeeResponse(deliveryFeeService.loadFeesAndPerformCalculation(city, vehicleType, targetTime))
            );
        } catch(VehicleUsageDisallowedException e) {
            return ResponseEntity.status(422).body(
                new DeliveryFeeFailure(e.getMessage())
            );
        } catch(WeatherDataNotFoundException e) {
            return ResponseEntity.status(422).body(
                new DeliveryFeeFailure("Weather data was not found for the given target time")
            );
        } catch(FeeNotFoundException e) { // could be invalid targetTime, but also the fees might be missing which isn't supposed to happen
            log.warn("Issue finding fees while calculating delivery fee", e);
            return ResponseEntity.status(422).body(
                new DeliveryFeeFailure("Delivery fee could not be calculated with given parameters")
            );
        }
    }

    public record DeliveryFeeResponse(BigDecimal fee) {}

    public record DeliveryFeeFailure(String error) {}
}
