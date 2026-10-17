package ee.fujitsu.fooddelivery.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.SAXException;

import ee.fujitsu.fooddelivery.dto.WeatherDataDto;
import ee.fujitsu.fooddelivery.exceptions.WeatherClientException;

@ExtendWith(MockitoExtension.class)
class WeatherDataClientTests {

    private static final URI WEATHER_API_URL =
        URI.create("https://example.com/weather/observations.xml");

    @Mock
    private HttpClient client;
    @Mock
    private HttpResponse<InputStream> mockResponse;
    @Mock
    private InputStream responseBody;
    @Mock
    private WeatherResponseParser responseParser;

    private WeatherDataClient weatherDataClient;

    @BeforeEach
    void setupTests() {
        weatherDataClient = new WeatherDataClient(WEATHER_API_URL, client, responseParser);
    }

    @Test
    void weatherClientShouldReturnUnmodifiedData() throws Exception {
        List<WeatherDataDto> observations = List.of(
            new WeatherDataDto("Tallinn-Harku", 26038, 10.8, 3.2, "Light rain", 1792244247),
            new WeatherDataDto("Kaev-1052", 0, null, null, null, 1792244247)
        );
        stubResponse(200);
        when(responseParser.parseObservations(responseBody)).thenReturn(observations);

        List<WeatherDataDto> result = weatherDataClient.getCurrentWeatherObservations();

        assertSame(observations, result);
        verify(responseParser).parseObservations(responseBody);
        verify(responseBody).close();
    }

    @Test
    void emptyObservationsShouldReturnEmptyList() throws Exception {
        stubResponse(200);
        when(responseParser.parseObservations(responseBody)).thenReturn(List.of());

        List<WeatherDataDto> result = weatherDataClient.getCurrentWeatherObservations();

        assertTrue(result.isEmpty());
        verify(responseBody).close();
    }

    @ParameterizedTest
    @ValueSource(ints = {204, 302, 404, 500})
    void non200ResponseShouldThrowException(int statusCode) throws Exception {
        stubResponse(statusCode);

        WeatherClientException exception = assertThrows(
            WeatherClientException.class,
            () -> weatherDataClient.getCurrentWeatherObservations()
        );

        assertEquals("Weather API returned HTTP " + statusCode, exception.getMessage());
        verifyNoInteractions(responseParser);
        verify(responseBody).close();
    }

    @Test
    void requestIOExceptionShouldBeWrappedWithOriginalCause() throws Exception {
        IOException cause = new IOException("Connection failed");
        when(client.<InputStream>send(any(), any())).thenThrow(cause);

        WeatherClientException exception = assertThrows(
            WeatherClientException.class,
            () -> weatherDataClient.getCurrentWeatherObservations()
        );

        assertEquals("Failed to retrieve weather observations", exception.getMessage());
        assertSame(cause, exception.getCause());
        verifyNoInteractions(responseParser);
    }

    private static Stream<Exception> parserFailures() {
        return Stream.of(
            new SAXException("An invalid XML character (Unicode: 0x1) was found in the node's character data content"),
            new ParserConfigurationException("Parser configuration failed"),
            new NumberFormatException("For input string: \"1.3a\""),
            new IOException("Response body could not be read")
        );
    }

    @ParameterizedTest
    @MethodSource("parserFailures")
    void parserFailureShouldBeWrappedAndCloseResponseBody(Exception cause) throws Exception {
        stubResponse(200);
        when(responseParser.parseObservations(responseBody)).thenThrow(cause);

        WeatherClientException exception = assertThrows(
            WeatherClientException.class,
            () -> weatherDataClient.getCurrentWeatherObservations()
        );

        assertSame(cause, exception.getCause());
        verify(responseBody).close();
    }

    @Test
    void interruptedRequestShouldRestoreInterruptFlagAndPreserveCause() throws Exception {
        InterruptedException cause = new InterruptedException("Request interrupted");
        when(client.<InputStream>send(any(), any())).thenThrow(cause);

        try {
            WeatherClientException exception = assertThrows(
                WeatherClientException.class,
                () -> weatherDataClient.getCurrentWeatherObservations()
            );

            assertEquals("Weather API request was interrupted", exception.getMessage());
            assertSame(cause, exception.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
            verifyNoInteractions(responseParser);
        } finally {
            // Prevent this test's interrupt flag from affecting subsequent tests.
            Thread.interrupted();
        }
    }

    private void stubResponse(int statusCode) throws IOException, InterruptedException {
        when(client.<InputStream>send(any(), any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(statusCode);
        when(mockResponse.body()).thenReturn(responseBody);
    }
}
