package ee.fujitsu.fooddelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ee.fujitsu.fooddelivery.entity.WeatherDataModel;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherDataModel, Long> {
    
}
