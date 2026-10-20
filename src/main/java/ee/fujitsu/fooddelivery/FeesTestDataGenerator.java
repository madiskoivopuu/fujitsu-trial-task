package ee.fujitsu.fooddelivery;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.RegionalBaseFee;
import ee.fujitsu.fooddelivery.core.VehicleType;
import ee.fujitsu.fooddelivery.core.WeatherPhenomenon;
import ee.fujitsu.fooddelivery.core.fees.AirTemperatureFee;
import ee.fujitsu.fooddelivery.core.fees.ExtraFeeRule;
import ee.fujitsu.fooddelivery.core.fees.WeatherPhenomenonFee;
import ee.fujitsu.fooddelivery.core.fees.WindSpeedFee;
import ee.fujitsu.fooddelivery.repository.BaseFeeRepository;
import ee.fujitsu.fooddelivery.repository.ExtraFeeRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("testdata")
public class FeesTestDataGenerator implements CommandLineRunner {
    private static final Instant VERSION_CHANGE = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant VALID_FROM = LocalDateTime.MIN.toInstant(ZoneOffset.UTC);
    private static final Instant VALID_TO = LocalDateTime.MAX.withNano(999_999_000).toInstant(ZoneOffset.UTC);
    private static final List<FeeVersion> FEE_VERSIONS = List.of(
        new FeeVersion(VALID_FROM, VERSION_CHANGE, new BigDecimal("0.50")),
        new FeeVersion(VERSION_CHANGE, VALID_TO, BigDecimal.ONE)
    );

    private final BaseFeeRepository baseFeeRepository;
    private final ExtraFeeRepository extraFeeRepository;

    /** Inserts two non-overlapping versions of the sample base and extra fees
     * 
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (baseFeeRepository.count() == 0) {
            List<RegionalBaseFee> fees = new ArrayList<>();
            for (FeeVersion version : FEE_VERSIONS) {
                fees.addAll(List.of(
                    baseFee(version, City.TALLINN, VehicleType.CAR, "4.00"),
                    baseFee(version, City.TALLINN, VehicleType.SCOOTER, "3.50"),
                    baseFee(version, City.TALLINN, VehicleType.BIKE, "3.00"),
                    baseFee(version, City.TARTU, VehicleType.CAR, "3.50"),
                    baseFee(version, City.TARTU, VehicleType.SCOOTER, "3.00"),
                    baseFee(version, City.TARTU, VehicleType.BIKE, "2.50"),
                    baseFee(version, City.PÄRNU, VehicleType.CAR, "3.00"),
                    baseFee(version, City.PÄRNU, VehicleType.SCOOTER, "2.50"),
                    baseFee(version, City.PÄRNU, VehicleType.BIKE, "2.00")
                ));
            }
            baseFeeRepository.saveAll(fees);
        }

        if (extraFeeRepository.count() == 0) {
            List<ExtraFeeRule> rules = new ArrayList<>();
            for (FeeVersion version : FEE_VERSIONS) {
                for (City city : City.values()) {
                    for (VehicleType vehicle : List.of(VehicleType.SCOOTER, VehicleType.BIKE)) {
                        rules.add(temperatureFee(version, city, vehicle, -Double.MAX_VALUE, -10.0, "1.00"));
                        rules.add(temperatureFee(version, city, vehicle, -10.0, Math.nextUp(0.0), "0.50"));

                        rules.add(phenomenonFee(version, city, vehicle, WeatherPhenomenon.SNOW, "1.00", false));
                        rules.add(phenomenonFee(version, city, vehicle, WeatherPhenomenon.SLEET, "1.00", false));
                        rules.add(phenomenonFee(version, city, vehicle, WeatherPhenomenon.RAIN, "0.50", false));
                        rules.add(phenomenonFee(version, city, vehicle, WeatherPhenomenon.GLAZE, "0.00", true));
                        rules.add(phenomenonFee(version, city, vehicle, WeatherPhenomenon.HAIL, "0.00", true));
                        rules.add(phenomenonFee(version, city, vehicle, WeatherPhenomenon.THUNDER, "0.00", true));
                    }

                    rules.add(windFee(version, city, 10.0, Math.nextUp(20.0), "0.50", false));
                    rules.add(windFee(version, city, Math.nextUp(20.0), Double.MAX_VALUE, "0.00", true));
                }
            }
            extraFeeRepository.saveAll(rules);
        }
    }

    private RegionalBaseFee baseFee(FeeVersion version, City city, VehicleType vehicle, String amount) {
        return new RegionalBaseFee(null, city, vehicle, version.validFrom(), version.validTo(), version.fee(amount));
    }

    private AirTemperatureFee temperatureFee(FeeVersion version, City city, VehicleType vehicle,
                                              double lower, double upper, String amount) {
        AirTemperatureFee rule = new AirTemperatureFee();
        initializeRule(rule, version, city, vehicle, amount, false);
        rule.setLowerBoundInclusive(lower);
        rule.setUpperBoundExclusive(upper);
        return rule;
    }

    private WindSpeedFee windFee(FeeVersion version, City city, double lower, double upper,
                                  String amount, boolean forbidden) {
        WindSpeedFee rule = new WindSpeedFee();
        initializeRule(rule, version, city, VehicleType.BIKE, amount, forbidden);
        rule.setLowerBoundInclusive(lower);
        rule.setUpperBoundExclusive(upper);
        return rule;
    }

    private WeatherPhenomenonFee phenomenonFee(FeeVersion version, City city, VehicleType vehicle,
                                               WeatherPhenomenon phenomenon,
                                               String amount, boolean forbidden) {
        WeatherPhenomenonFee rule = new WeatherPhenomenonFee();
        initializeRule(rule, version, city, vehicle, amount, forbidden);
        rule.setWeatherPhenomenon(phenomenon);
        return rule;
    }

    private void initializeRule(ExtraFeeRule rule, FeeVersion version, City city, VehicleType vehicle,
                                String amount, boolean forbidden) {
        rule.setCity(city);
        rule.setVehicleType(vehicle);
        rule.setValidFrom(version.validFrom());
        rule.setValidTo(version.validTo());
        rule.setFee(version.fee(amount));
        rule.setVehicleUsageForbidden(forbidden);
    }

    private record FeeVersion(Instant validFrom, Instant validTo, BigDecimal multiplier) {
        BigDecimal fee(String amount) {
            return new BigDecimal(amount).multiply(multiplier);
        }
    }
}
