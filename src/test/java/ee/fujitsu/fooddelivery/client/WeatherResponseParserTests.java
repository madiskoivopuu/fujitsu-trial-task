package ee.fujitsu.fooddelivery.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.SAXException;

import ee.fujitsu.fooddelivery.dto.WeatherDataDto;

@ExtendWith(MockitoExtension.class)
class WeatherResponseParserTests {
    private WeatherResponseParser responseParser;

    @BeforeEach
    void setupTests() {
        responseParser = new WeatherResponseParser();
    }

    @Test
    void parsingShouldReturnCorrectData() throws IOException, ParserConfigurationException, SAXException {
        String inputXml = """
        <observations timestamp="1792244247">
            <station>
                <name>Kuressaare linn</name>
                <wmocode/>
                <longitude>22.48944444411111</longitude>
                <latitude>58.26416666666667</latitude>
                <phenomenon/>
                <visibility/>
                <precipitations/>
                <airpressure/>
                <relativehumidity>87</relativehumidity>
                <airtemperature>10.8</airtemperature>
                <winddirection/>
                <windspeed/>
                <windspeedmax/>
                <waterlevel/>
                <waterlevel_eh2000/>
                <watertemperature/>
                <uvindex/>
                <sunshineduration/>
                <globalradiation/>
            </station>
            <station>
                <name>Tallinn-Harku</name>
                <wmocode>26038</wmocode>
                <longitude>24.602891666624284</longitude>
                <latitude>59.398122222355134</latitude>
                <phenomenon>Light shower</phenomenon>
                <visibility>35</visibility>
                <precipitations>0.1</precipitations>
                <airpressure>1019.8</airpressure>
                <relativehumidity>90.2</relativehumidity>
                <airtemperature>9.7</airtemperature>
                <winddirection>4</winddirection>
                <windspeed>1.9</windspeed>
                <windspeedmax>3.2</windspeedmax>
                <waterlevel/>
                <waterlevel_eh2000/>
                <watertemperature/>
                <uvindex/>
                <sunshineduration>0</sunshineduration>
                <globalradiation>32</globalradiation>
            </station>
        </observations>
        """;
        InputStream stream = new ByteArrayInputStream(inputXml.getBytes());
        List<WeatherDataDto> expectedObservations = List.of(
            new WeatherDataDto("Kuressaare linn", null, 10.8, null, null, 1792244247),
            new WeatherDataDto("Tallinn-Harku", 26038, 9.7, 1.9, "Light shower", 1792244247)
        );

        List<WeatherDataDto> observations = responseParser.parseObservations(stream);

        assertTrue(observations.equals(expectedObservations));
    }

    @Test
    void parsingMalformedXmlShouldThrowException() throws IOException, ParserConfigurationException, SAXException {
        String inputXml = """
        <observations timestamp="1792244247">
            <station>
                <name>Kuressaare linn</name>
                <wmocode/>
                <longitude>22.48944444411111</longitude>
                <latitude>58.26416666666667</latitude>
                <phenomenon/>
                <visibility/>
                <precipitations/>
                <airpressure/>
                <relativehumidity>87</relativehumidity>
                <airtemper
        """;
        InputStream stream = new ByteArrayInputStream(inputXml.getBytes());

        assertThrows(
            SAXException.class, 
            () -> responseParser.parseObservations(stream));
    }
}
