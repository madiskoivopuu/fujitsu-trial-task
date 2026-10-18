package ee.fujitsu.fooddelivery.configuration;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ee.fujitsu.fooddelivery.core.DeliveryFeeCalculator;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlReadFeature;

@Configuration 
public class BeansConfig {
    
    @Bean 
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public XmlMapper xmlMapper() {
        return XmlMapper.builder()
            .enable(XmlReadFeature.EMPTY_ELEMENT_AS_NULL)
            .build();
    }

    @Bean 
    public DeliveryFeeCalculator deliveryFeeCalculator() {
        return new DeliveryFeeCalculator();
    }
}
