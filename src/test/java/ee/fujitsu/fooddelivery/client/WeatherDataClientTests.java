package ee.fujitsu.fooddelivery.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.fujitsu.fooddelivery.exceptions.WeatherClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.xml.XmlMapper;

@ExtendWith(MockitoExtension.class)
class WeatherDataClientTests {
    @Mock
    private HttpClient client;
    @Mock
    private HttpResponse<InputStream> mockResponse;
    @Mock
    private InputStream responseBody;
    @Mock
    private XmlMapper xmlMapper;
    private WeatherDataClient weatherDataClient;

    @BeforeEach
    void setupTests() {
        weatherDataClient = new WeatherDataClient(
            URI.create("https://example.com/observations.xml"), client, xmlMapper
        );
    }

    @Test
    void shouldReturnUnmodifiedDataWhenResponseIsReceived() throws Exception {
        Observations observations = new Observations(
            1792244247,
            List.of(
                new Station("Tallinn-Harku", 26038, 10.8, 3.2, "Light rain"),
                new Station("Kaev-1052", 0, null, null, null)
            )
        );
        stubResponse(200);
        when(xmlMapper.readValue(responseBody, Observations.class)).thenReturn(observations);

        Observations result = weatherDataClient.getCurrentWeatherObservations();

        assertSame(observations, result);
        verify(xmlMapper).readValue(responseBody, Observations.class);
        verify(responseBody).close();
    }

    @ParameterizedTest
    @ValueSource(ints = {204, 302, 404, 500})
    void shouldThrowExceptionWhenResponseIsNot200(int statusCode) throws Exception {
        stubResponse(statusCode);

        WeatherClientException exception = assertThrows(
            WeatherClientException.class,
            () -> weatherDataClient.getCurrentWeatherObservations()
        );

        assertEquals("Weather API returned HTTP " + statusCode, exception.getMessage());
        verifyNoInteractions(xmlMapper);
        verify(responseBody).close();
    }

    @Test
    void shouldThrowWrappedExceptionWhenIOExceptionIsCaught() throws Exception {
        IOException cause = new IOException("Connection failed");
        when(client.<InputStream>send(any(), any())).thenThrow(cause);

        WeatherClientException exception = assertThrows(
            WeatherClientException.class,
            () -> weatherDataClient.getCurrentWeatherObservations()
        );

        assertEquals("Failed to retrieve weather observations", exception.getMessage());
        assertSame(cause, exception.getCause());
        verifyNoInteractions(xmlMapper);
    }

    @Test
    void shouldThrowWrappedExceptionAndCloseResponseWhenJacksonExceptionIsThrown() throws Exception {
        JacksonException cause = mock(JacksonException.class);
        stubResponse(200);
        when(xmlMapper.readValue(responseBody, Observations.class)).thenThrow(cause);

        WeatherClientException exception = assertThrows(
            WeatherClientException.class,
            () -> weatherDataClient.getCurrentWeatherObservations()
        );

        assertEquals("Failed to parse XML from API response", exception.getMessage());
        assertSame(cause, exception.getCause());
        verify(responseBody).close();
    }

    @Test
    void shouldRestoreInterruptFlagAndPreserveCauseWhenRequestInterrupted() throws Exception {
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
            verifyNoInteractions(xmlMapper);
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
