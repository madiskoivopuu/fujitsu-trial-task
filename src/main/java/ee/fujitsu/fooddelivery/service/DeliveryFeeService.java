package ee.fujitsu.fooddelivery.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import ee.fujitsu.fooddelivery.core.City;
import ee.fujitsu.fooddelivery.core.DeliveryFeeCalculator;
import ee.fujitsu.fooddelivery.core.ExtraFees;
import ee.fujitsu.fooddelivery.core.RegionalBaseFee;
import ee.fujitsu.fooddelivery.core.VehicleType;
import ee.fujitsu.fooddelivery.entity.WeatherDataModel;
import ee.fujitsu.fooddelivery.exceptions.FeeNotFoundException;
import ee.fujitsu.fooddelivery.exceptions.VehicleUsageDisallowedException;
import ee.fujitsu.fooddelivery.exceptions.WeatherDataNotFoundException;
import ee.fujitsu.fooddelivery.mapper.WeatherDataToWeatherConditionsMapper;
import ee.fujitsu.fooddelivery.repository.BaseFeeRepository;
import ee.fujitsu.fooddelivery.repository.WeatherDataRepository;
import ee.fujitsu.fooddelivery.repository.ExtraFeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2 
@RequiredArgsConstructor 
public class DeliveryFeeService {
    private final BaseFeeRepository baseFeeRepository;
    private final ExtraFeeRepository extraFeeRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final WeatherDataToWeatherConditionsMapper weatherDataMapper;

    private final DeliveryFeeCalculator calculator;

    /**
     * Loads the active fees valid at the target time and weather conditions at or before closest to the target date.
     * Then performs the delivery fee calculation based on those fees
     * @param city delivery city: {@code TALLINN}, {@code TARTU}, or {@code PÄRNU}
     * @param vehicleType delivery vehicle: {@code CAR}, {@code SCOOTER}, or {@code BIKE}
     * @param targetTime time used to select the fees and weather observations that were 
     *        valid at that specific time.
     * @return The delivery fee, based on the weather and fees that were used at target time
     * @throws VehicleUsageDisallowedException when use of the selected vehicle is not allowed due to weather conditions
     * @throws WeatherDataNotFoundException when weather data is not found for a specific city at a given target time
     * @throws FeeNotFoundException when regional base fees or extra fees are missing at a given target time
     */
    public BigDecimal loadFeesAndPerformCalculation(City city, VehicleType vehicleType, Instant targetTime) throws VehicleUsageDisallowedException, WeatherDataNotFoundException, FeeNotFoundException {
        if(targetTime == null)
            targetTime = Instant.now();

        RegionalBaseFee regionalBaseFee = baseFeeRepository.findLatestBaseFeeAtOrBeforeTimestamp(city, vehicleType, targetTime)
            .orElseThrow(() -> new FeeNotFoundException("Regional base fee could not be found"));
        ExtraFees extraFees = extraFeeRepository.findLatestExtraFeesAtOrBeforeTimestamp(targetTime)
            .orElseThrow(() -> new FeeNotFoundException("Extra fees could not be found"));
        WeatherDataModel weatherData = weatherDataRepository.findLatestWeatherDataByCityAtOrBefore(city, targetTime)
            .orElseThrow(() -> new WeatherDataNotFoundException("Weather data at current timestamp could not be found"));

        return calculator.calculateFee(vehicleType, weatherDataMapper.toWeatherPhenomenon(weatherData), regionalBaseFee, extraFees);
    }
}
