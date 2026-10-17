package ee.fujitsu.fooddelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ee.fujitsu.fooddelivery.dto.WeatherDataDto;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherDataDto, Long> {
    
}
