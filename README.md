# Delivery Fee Calculation API

This project implements Fujitsu's trial task: an API for calculating delivery fees based on the city, vehicle type, weather and active base/extra fees for a specific time.
The application consists of two main components:
- A weather data collection service, which periodically retrieves and stores weather observations from the Estonian Environment Agency's observations API
- A delivery fee calculation service that applies regional base fees and weather related extra fees for each calculation

## Application setup & running locally

This application requires Java 25 and Maven to be installed.  
Start the application initially with the `testdata` profile to generate some fees and weather conditions. The fee calculator requires weather and fee data to be present in H2 database in order to calculate a delivery fee

`mvn spring-boot:run "-Dspring-boot.run.profiles=testdata"`  

This command will only populate an empty H2 database located at `database/fooddelivery.mv.db` with sample weather and fee data.   
The fee versions change at 1.7.2026. Weather observations are provided for 28.06.2026 and 5.7.2026, allowing calculations to be tested under different weather conditions and fee versions.

## Running tests

Tests for this project can be executed with the following command:
`mvn test "-Dspring.profiles.active=test"`

This project also uses JaCoCo to generate test coverage reports:
`mvn clean verify "-Dspring.profiles.active=test"`

Afterwards you can view the coverage report by opening `target/site/jacoco/index.html` in a browser.

## API documentation

Documentation about the delivery fee calculation endpoint can be found at [Swagger UI](http://localhost:8080/swagger-ui.html)  
For the sake of simplicity (and since there is only one endpoint), the endpoint is also documented below

`GET /api/delivery-fee`

| Query parameter | Required | Values |
| --- | --- | --- |
| `city` | Yes | One of:`TALLINN`, `TARTU`, `PÄRNU` (case-sensitive) |
| `vehicleType` | Yes | One of: `CAR`, `SCOOTER`, `BIKE` (case-sensitive) |
| `targetTime` | No | ISO-8601 datetime with time offset, e.g. `2026-01-15T12:00:00Z`; defaults to the current time |

Example: `GET /api/delivery-fee?city=TALLINN&vehicleType=CAR&targetTime=2026-06-29T13:00:00Z`.

A successful response is `200` with a JSON body such as `{"fee":2.00}` (euros).  
Missing or invalid parameters return `400`.  
Prohibited vehicle usage, missing weather observations, or unavailable fees return `422` with an `error` message. Calculations require fees valid at the requested time and a weather observation at or before that time.

## Limitations & issues
Fee management - base fee and extra fee related management CRUD APIs are not implemented. The fees can currently only be added by running the application with `"-Dspring-boot.run.profiles=testdata"` option, which causes WeatherTestDataGenerator and FeeTestDataGenerator to populate the database with weather observations and fees
