package ee.fujitsu.fooddelivery.configuration;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ee.fujitsu.fooddelivery.client.WeatherResponseParser;

@Configuration 
public class BeansConfig {
    
    @Bean 
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean 
    public WeatherResponseParser weatherResponseParser() {
        return new WeatherResponseParser();
    }
}
