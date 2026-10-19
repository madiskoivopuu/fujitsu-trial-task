package ee.fujitsu.fooddelivery.service;

import static ee.fujitsu.fooddelivery.utils.FeeFactory.baseFee;
import static ee.fujitsu.fooddelivery.utils.FeeFactory.phenomenonFee;
import static ee.fujitsu.fooddelivery.utils.FeeFactory.temperatureFee;
import static ee.fujitsu.fooddelivery.utils.FeeFactory.windFee;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.VehicleType;
import ee.fujitsu.fooddelivery.core.WeatherPhenomenon;
import ee.fujitsu.fooddelivery.entity.WeatherDataModel;
import ee.fujitsu.fooddelivery.exceptions.FeeNotFoundException;
import ee.fujitsu.fooddelivery.exceptions.VehicleUsageDisallowedException;
import ee.fujitsu.fooddelivery.exceptions.WeatherDataNotFoundException;
import ee.fujitsu.fooddelivery.repository.BaseFeeRepository;
import ee.fujitsu.fooddelivery.repository.ExtraFeeRepository;
import ee.fujitsu.fooddelivery.repository.WeatherDataRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeliveryFeeServiceIntegrationTests {
    private static final Instant TARGET_TIME = Instant.parse("2026-01-15T12:00:00Z");
    private static final Instant VALID_FROM = TARGET_TIME.minusSeconds(3600);

    @Autowired
    private DeliveryFeeService service;

    @Autowired
    private BaseFeeRepository baseFeeRepository;

    @Autowired
    private ExtraFeeRepository extraFeeRepository;

    @Autowired
    private WeatherDataRepository weatherDataRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldAddAllApplicableWeatherFeesToBaseFee() throws Exception {
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.BIKE, "3.00", VALID_FROM, null));
        extraFeeRepository.save(temperatureFee(City.TALLINN, VehicleType.BIKE, -20.0, -10.0, "1.00", VALID_FROM, null));
        extraFeeRepository.save(windFee(City.TALLINN, VehicleType.BIKE, 10.0, 20.0, "0.50", false, VALID_FROM, null));
        extraFeeRepository.save(phenomenonFee(City.TALLINN, VehicleType.BIKE, WeatherPhenomenon.SNOW, "1.00", false, VALID_FROM, null));
        saveObservation(City.TALLINN, -15.0, 12.2, "Moderate snowfall", TARGET_TIME);
        flushAndClear();

        BigDecimal fee = service.loadFeesAndPerformCalculation(City.TALLINN, VehicleType.BIKE, TARGET_TIME);

        assertThat(fee).isEqualByComparingTo("5.50");
    }

    @Test
    void shouldReturnBaseFeeWhenNoExtraFeesAreConfiguredForVehicle() throws Exception {
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.CAR, "4.00", VALID_FROM, null));
        extraFeeRepository.save(windFee(City.TALLINN, VehicleType.BIKE, 20.0, Double.MAX_VALUE, "0.00", true, VALID_FROM, null));
        extraFeeRepository.save(phenomenonFee(City.TALLINN, VehicleType.BIKE, WeatherPhenomenon.HAIL, "0.00", true, VALID_FROM, null));
        saveObservation(City.TALLINN, -15.0, 25.0, "Hail", TARGET_TIME);
        flushAndClear();

        BigDecimal fee = service.loadFeesAndPerformCalculation(City.TALLINN, VehicleType.CAR, TARGET_TIME);

        assertThat(fee).isEqualByComparingTo("4.00");
    }

    @Test
    void shouldReturnBaseFeeWhenWeatherDoesNotMatchConfiguredRules() throws Exception {
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.BIKE, "3.00", VALID_FROM, null));
        extraFeeRepository.save(temperatureFee(City.TALLINN, VehicleType.BIKE, -20.0, 0.0, "1.00", VALID_FROM, null));
        extraFeeRepository.save(windFee(City.TALLINN, VehicleType.BIKE, 10.0, 20.0, "0.50", false, VALID_FROM, null));
        extraFeeRepository.save(phenomenonFee(City.TALLINN, VehicleType.BIKE, WeatherPhenomenon.SNOW, "1.00", false, VALID_FROM, null));
        saveObservation(City.TALLINN, 10.0, 3.0, "Clear", TARGET_TIME);
        flushAndClear();

        BigDecimal fee = service.loadFeesAndPerformCalculation(City.TALLINN, VehicleType.BIKE, TARGET_TIME);

        assertThat(fee).isEqualByComparingTo("3.00");
    }

    @Test
    void shouldUseOnlyFeesForRequestedCityAndVehicle() throws Exception {
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.BIKE, "3.00", VALID_FROM, null));
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.SCOOTER, "3.50", VALID_FROM, null));
        extraFeeRepository.save(temperatureFee(City.TALLINN, VehicleType.BIKE, -20.0, 0.0, "1.00", VALID_FROM, null));
        extraFeeRepository.save(temperatureFee(City.TALLINN, VehicleType.SCOOTER, -20.0, 0.0, "7.00", VALID_FROM, null));
        baseFeeRepository.save(baseFee(City.TARTU, VehicleType.BIKE, "2.50", VALID_FROM, null));
        extraFeeRepository.save(temperatureFee(City.TARTU, VehicleType.BIKE, -20.0, 0.0, "5.00", VALID_FROM, null));
        saveObservation(City.TALLINN, -15.0, 3.0, "Clear", TARGET_TIME);
        flushAndClear();

        BigDecimal fee = service.loadFeesAndPerformCalculation(City.TALLINN, VehicleType.BIKE, TARGET_TIME);

        assertThat(fee).isEqualByComparingTo("4.00");
    }

    @Test
    void shouldSwitchToNewFeesAtValidityBoundaryAndIgnoreFutureFees() throws Exception {
        Instant future = TARGET_TIME.plusSeconds(3600);
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.BIKE, "3.00", VALID_FROM, TARGET_TIME));
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.BIKE, "4.00", TARGET_TIME, future));
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.BIKE, "9.00", future, null));
        extraFeeRepository.save(temperatureFee(City.TALLINN, VehicleType.BIKE, -10.0, 0.0, "1.00", VALID_FROM, TARGET_TIME));
        extraFeeRepository.save(temperatureFee(City.TALLINN, VehicleType.BIKE, -10.0, 0.0, "0.50", TARGET_TIME, future));
        extraFeeRepository.save(temperatureFee(City.TALLINN, VehicleType.BIKE, -10.0, 0.0, "2.00", future, null));
        saveObservation(City.TALLINN, -5.0, 3.0, "Clear", TARGET_TIME);
        flushAndClear();

        BigDecimal fee = service.loadFeesAndPerformCalculation(City.TALLINN, VehicleType.BIKE, TARGET_TIME);

        assertThat(fee).isEqualByComparingTo("4.50");
    }

    @Test
    void shouldUseClosestEarlierWeatherForRequestedCityWhenSelectingTemperatureFee() throws Exception {
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.BIKE, "3.00", VALID_FROM, null));
        extraFeeRepository.save(temperatureFee(City.TALLINN, VehicleType.BIKE, -20.0, -10.0, "1.00", VALID_FROM, null));
        extraFeeRepository.save(temperatureFee(City.TALLINN, VehicleType.BIKE, -10.0, 0.0, "0.50", VALID_FROM, null));
        saveObservation(City.TALLINN, -8.9, 3.0, "Clear", TARGET_TIME.minusSeconds(120));
        saveObservation(City.TALLINN, -9.7, 3.0, "Clear", TARGET_TIME.minusSeconds(60));
        saveObservation(City.TALLINN, -10.1, 3.0, "Clear", TARGET_TIME.plusSeconds(60));
        saveObservation(City.TALLINN, -9.8, 3.0, "Clear", TARGET_TIME);
        saveObservation(City.TARTU, -12.1, 3.0, "Clear", TARGET_TIME.minusSeconds(60));
        saveObservation(City.TARTU, -12.4, 3.0, "Clear", TARGET_TIME.plusSeconds(60));
        saveObservation(City.TARTU, -12.3, 3.0, "Clear", TARGET_TIME);
        flushAndClear();

        BigDecimal fee = service.loadFeesAndPerformCalculation(City.TALLINN, VehicleType.BIKE, TARGET_TIME);

        assertThat(fee).isEqualByComparingTo("3.50");
    }

    @Test
    void shouldRejectBikeWhenWindRuleForbidsUsage() {
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.BIKE, "3.00", VALID_FROM, null));
        extraFeeRepository.save(windFee(City.TALLINN, VehicleType.BIKE, 20.0, Double.MAX_VALUE, "0.00", true, VALID_FROM, null));
        saveObservation(City.TALLINN, 10.0, 25.0, "Clear", TARGET_TIME);
        flushAndClear();

        assertThatThrownBy(() -> service.loadFeesAndPerformCalculation(City.TALLINN, VehicleType.BIKE, TARGET_TIME))
            .isInstanceOf(VehicleUsageDisallowedException.class);
    }

    @ParameterizedTest
    @CsvSource({"Glaze, GLAZE", "Hail, HAIL", "Thunderstorm, THUNDER"})
    void shouldRejectVehicleWhenMappedPhenomenonForbidsUsage(String observation, WeatherPhenomenon phenomenon) {
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.SCOOTER, "3.50", VALID_FROM, null));
        extraFeeRepository.save(phenomenonFee(City.TALLINN, VehicleType.SCOOTER, phenomenon, "0.00", true, VALID_FROM, null));
        saveObservation(City.TALLINN, 10.0, 3.0, observation, TARGET_TIME);
        flushAndClear();

        assertThatThrownBy(() -> service.loadFeesAndPerformCalculation(City.TALLINN, VehicleType.SCOOTER, TARGET_TIME))
            .isInstanceOf(VehicleUsageDisallowedException.class);
    }

    @Test
    void shouldThrowWhenBaseFeeIsMissing() {
        assertThatThrownBy(() -> service.loadFeesAndPerformCalculation(City.TALLINN, VehicleType.BIKE, TARGET_TIME))
            .isInstanceOf(FeeNotFoundException.class)
            .hasMessage("Regional base fee could not be found");
    }

    @Test
    void shouldThrowWhenNoWeatherExistsAtOrBeforeTargetTime() {
        baseFeeRepository.save(baseFee(City.TALLINN, VehicleType.BIKE, "3.00", VALID_FROM, null));
        extraFeeRepository.save(temperatureFee(City.TALLINN, VehicleType.BIKE, -10.0, 0.0, "0.50", VALID_FROM, null));
        saveObservation(City.TALLINN, -5.0, 3.0, "Clear", TARGET_TIME.plusSeconds(60));
        saveObservation(City.TARTU, -5.0, 3.0, "Clear", TARGET_TIME);
        flushAndClear();

        assertThatThrownBy(() -> service.loadFeesAndPerformCalculation(City.TALLINN, VehicleType.BIKE, TARGET_TIME))
            .isInstanceOf(WeatherDataNotFoundException.class);
    }

    private void saveObservation(City city, double temperature, double windSpeed, String phenomenon, Instant timestamp) {
        weatherDataRepository.save(new WeatherDataModel(
            city.getStationName(), 26038, temperature, windSpeed, phenomenon, timestamp
        ));
    }

    private void flushAndClear() {
        entityManager.flush(); 
        entityManager.clear(); 
    }
}
