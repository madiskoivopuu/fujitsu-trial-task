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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping(path = "/api/delivery-fee", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Log4j2 
@Tag(name = "Delivery fees", description = "Delivery fee calculation by city, vehicle and time")
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
    @Operation(
        summary = "Calculate a delivery fee",
        description = "Uses the fees active at the target time and the latest weather observation "
            + "at or before that time. If targetTime is omitted, the current time is used to fetch"
            + "active fees and weather conditions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Delivery fee calculated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = DeliveryFeeResponse.class),
                examples = @ExampleObject(value = "{\"fee\":4.00}"))),
        @ApiResponse(responseCode = "400",
            description = "A required parameter is missing, an enum value is invalid (values are case-sensitive), "
                + "or targetTime is not a valid ISO-8601 datetime with a time offset.",
            content = @Content),
        @ApiResponse(responseCode = "422",
            description = "Weather conditions prohibit the vehicle from being used for the delivery or the required weather data/fees are unavailable.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = DeliveryFeeFailure.class),
                examples = {
                    @ExampleObject(name = "Missing weather data",
                        value = "{\"error\":\"Weather data was not found for the given target time\"}"),
                    @ExampleObject(name = "Missing fees",
                        value = "{\"error\":\"Delivery fee could not be calculated with given parameters\"}")
                }))
    })
    public ResponseEntity<?> calculateDeliveryFee(
        @Parameter(description = "Delivery city (case-sensitive)", example = "TALLINN")
        @RequestParam("city") City city,
        @Parameter(description = "Delivery vehicle (case-sensitive)", example = "CAR")
        @RequestParam("vehicleType") VehicleType vehicleType,
        @Parameter(description = "Optional ISO-8601 datetime a time offset used to select fees and weather observations."
            + "If left unspecified, it defaults to the current time.", example = "2026-10-20T12:00:00Z",
            schema = @Schema(type = "string", format = "date-time"))
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

    public record DeliveryFeeResponse(
        @Schema(description = "Total delivery fee in euros", example = "4.00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal fee
    ) {}

    public record DeliveryFeeFailure(
        @Schema(description = "Reason the delivery fee could not be calculated",
            example = "Weather data was not found for the given target time",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String error
    ) {}
}
