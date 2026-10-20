package ee.fujitsu.fooddelivery.core;

import static ee.fujitsu.fooddelivery.utils.FeeFactory.baseFee;
import static ee.fujitsu.fooddelivery.utils.FeeFactory.phenomenonFee;
import static ee.fujitsu.fooddelivery.utils.FeeFactory.temperatureFee;
import static ee.fujitsu.fooddelivery.utils.FeeFactory.windFee;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import ee.fujitsu.fooddelivery.core.fees.AirTemperatureFee;
import ee.fujitsu.fooddelivery.core.fees.ExtraFeeRule;
import ee.fujitsu.fooddelivery.core.fees.WeatherPhenomenonFee;
import ee.fujitsu.fooddelivery.core.fees.WindSpeedFee;
import ee.fujitsu.fooddelivery.exceptions.VehicleUsageDisallowedException;

class DeliveryFeeCalculatorIntegrationTests {
    private final DeliveryFeeCalculator calculator = new DeliveryFeeCalculator();

    @Test
    void shouldReturnBaseFeeWhenNoExtraRulesAreProvided() throws VehicleUsageDisallowedException {
        FeeEvaluationContext context = new FeeEvaluationContext(VehicleType.CAR, 10.0, 3.0, WeatherPhenomenon.REGULAR);

        BigDecimal fee = calculator.calculateFee(context, baseFee(VehicleType.CAR, "4.00"), List.of());

        assertThat(fee).isEqualByComparingTo("4.00");
    }

    @ParameterizedTest
    @CsvSource({"3.00, 0.50, 3.50", "0.10, 0.20, 0.30"})
    void shouldAddMatchingFeeWithExactDecimalArithmetic(String baseAmount, String extraAmount, String expected)
            throws VehicleUsageDisallowedException {
        FeeEvaluationContext context = new FeeEvaluationContext(VehicleType.BIKE, -5.0, 3.0, WeatherPhenomenon.REGULAR);
        AirTemperatureFee temperature = temperatureFee(VehicleType.BIKE, -10.0, 0.0, extraAmount);
        RegionalBaseFee base = baseFee(VehicleType.BIKE, baseAmount);

        BigDecimal fee = calculator.calculateFee(context, base, List.of(temperature));

        assertThat(fee).isEqualByComparingTo(expected);
        assertThat(base.getFee()).isEqualByComparingTo(baseAmount);
        assertThat(temperature.getFee()).isEqualByComparingTo(extraAmount);
    }

    @Test
    void shouldAddAllMatchingWeatherFees() throws VehicleUsageDisallowedException {
        FeeEvaluationContext context = new FeeEvaluationContext(VehicleType.BIKE, -15.0, 15.0, WeatherPhenomenon.SNOW);
        List<ExtraFeeRule> rules = List.of(
            temperatureFee(VehicleType.BIKE, -20.0, -10.0, "1.00"),
            windFee(VehicleType.BIKE, 10.0, 20.0, "0.50"),
            phenomenonFee(VehicleType.BIKE, WeatherPhenomenon.SNOW, "1.00")
        );

        BigDecimal fee = calculator.calculateFee(context, baseFee(VehicleType.BIKE, "3.00"), rules);

        assertThat(fee).isEqualByComparingTo("5.50");
    }

    @Test
    void shouldIgnoreNonMatchingRulesWhileAddingMatchingFees() throws VehicleUsageDisallowedException {
        FeeEvaluationContext context = new FeeEvaluationContext(VehicleType.BIKE, -5.0, 3.0, WeatherPhenomenon.RAIN);
        List<ExtraFeeRule> rules = List.of(
            temperatureFee(VehicleType.BIKE, -20.0, -10.0, "1.00"),
            temperatureFee(VehicleType.BIKE, -10.0, 0.0, "0.50"),
            windFee(VehicleType.BIKE, 10.0, 20.0, "0.50"),
            phenomenonFee(VehicleType.BIKE, WeatherPhenomenon.SNOW, "1.00"),
            phenomenonFee(VehicleType.BIKE, WeatherPhenomenon.RAIN, "0.50")
        );

        BigDecimal fee = calculator.calculateFee(context, baseFee(VehicleType.BIKE, "3.00"), rules);

        assertThat(fee).isEqualByComparingTo("4.00");
    }

    @Test
    void shouldIgnoreForbiddenRulesWhenTheirConditionsDoNotMatch() throws VehicleUsageDisallowedException {
        FeeEvaluationContext context = new FeeEvaluationContext(VehicleType.BIKE, 10.0, 3.0, WeatherPhenomenon.REGULAR);
        AirTemperatureFee temperature = temperatureFee(VehicleType.BIKE, -10.0, 0.0, "0.00");
        temperature.setVehicleUsageForbidden(true);
        WindSpeedFee wind = windFee(VehicleType.BIKE, 20.0, Double.MAX_VALUE, "0.00");
        wind.setVehicleUsageForbidden(true);
        WeatherPhenomenonFee snow = phenomenonFee(VehicleType.BIKE, WeatherPhenomenon.SNOW, "0.00");
        snow.setVehicleUsageForbidden(true);

        BigDecimal fee = calculator.calculateFee(
            context, baseFee(VehicleType.BIKE, "3.00"), List.of(temperature, wind, snow)
        );

        assertThat(fee).isEqualByComparingTo("3.00");
    }

    @ParameterizedTest
    @CsvSource({
        "-10.001, 3.00",
        "-10.0,   3.50",
        "-9.999,  3.50",
        "-0.001,  3.50",
        "0.0,     3.00",
        "0.001,   3.00"
    })
    void shouldRespectConfiguredTemperatureRange(double temperature, String expected)
            throws VehicleUsageDisallowedException {
        FeeEvaluationContext context = new FeeEvaluationContext(VehicleType.BIKE, temperature, 3.0, WeatherPhenomenon.REGULAR);
        AirTemperatureFee rule = temperatureFee(VehicleType.BIKE, -10.0, 0.0, "0.50");

        BigDecimal fee = calculator.calculateFee(context, baseFee(VehicleType.BIKE, "3.00"), List.of(rule));

        assertThat(fee).isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "9.999,  3.00",
        "10.0,   3.50",
        "10.001, 3.50",
        "19.999, 3.50",
        "20.0,   3.00",
        "20.001, 3.00"
    })
    void shouldRespectConfiguredWindSpeedRange(double windSpeed, String expected)
            throws VehicleUsageDisallowedException {
        FeeEvaluationContext context = new FeeEvaluationContext(VehicleType.BIKE, 10.0, windSpeed, WeatherPhenomenon.REGULAR);
        WindSpeedFee rule = windFee(VehicleType.BIKE, 10.0, 20.0, "0.50");

        BigDecimal fee = calculator.calculateFee(context, baseFee(VehicleType.BIKE, "3.00"), List.of(rule));

        assertThat(fee).isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @EnumSource(value = WeatherPhenomenon.class, names = {"GLAZE", "HAIL", "THUNDER"})
    void shouldRejectForbiddenWeatherPhenomenon(WeatherPhenomenon phenomenon) {
        FeeEvaluationContext context = new FeeEvaluationContext(VehicleType.SCOOTER, 10.0, 3.0, phenomenon);
        WeatherPhenomenonFee forbidden = phenomenonFee(VehicleType.SCOOTER, phenomenon, "0.00");
        forbidden.setVehicleUsageForbidden(true);

        assertThatThrownBy(() -> calculator.calculateFee(
            context, baseFee(VehicleType.SCOOTER, "3.50"), List.of(forbidden)
        )).isInstanceOf(VehicleUsageDisallowedException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldRejectForbiddenWeatherPhenomenonRegardlessOfRuleOrder(boolean forbiddenRuleFirst) {
        FeeEvaluationContext context = new FeeEvaluationContext(VehicleType.BIKE, -5.0, 25.0, WeatherPhenomenon.GLAZE);
        AirTemperatureFee temperature = temperatureFee(VehicleType.BIKE, -10.0, 0.0, "0.50");
        WindSpeedFee windSpeedFee = windFee(VehicleType.BIKE, 20.0, Double.MAX_VALUE, "0.00");
        WeatherPhenomenonFee forbidden = phenomenonFee(VehicleType.BIKE, WeatherPhenomenon.GLAZE, "0.00");
        forbidden.setVehicleUsageForbidden(true);
        List<ExtraFeeRule> rules = forbiddenRuleFirst
                ? List.of(forbidden, windSpeedFee, temperature)
                : List.of(temperature, windSpeedFee, forbidden);

        assertThatThrownBy(() -> calculator.calculateFee(context, baseFee(VehicleType.BIKE, "3.00"), rules))
                .isInstanceOf(VehicleUsageDisallowedException.class);
    }
}
