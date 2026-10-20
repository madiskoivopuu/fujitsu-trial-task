package ee.fujitsu.fooddelivery.configuration;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(
    title = "Delivery Fee API",
    version = "1.0",
    description = "Calculates delivery fees using the regional base fee and applicable extra fees based on weather conditions."
))
public class OpenApiConfig {
}
