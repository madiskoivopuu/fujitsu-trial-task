package ee.fujitsu.fooddelivery.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ee.fujitsu.fooddelivery.configuration.BeansConfig;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.xml.XmlMapper;

class WeatherResponseMappingTests {
    private XmlMapper xmlMapper;

    @BeforeEach
    void setupTests() {
        xmlMapper = new BeansConfig().xmlMapper();

    }

    @Test
    void shouldMapToCorrectFieldsWhenXmlIsParsed() throws JacksonException {
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
        InputStream stream = new ByteArrayInputStream(inputXml.getBytes(StandardCharsets.UTF_8));
        Observations expectedObservations = new Observations(
            1792244247,
            List.of(
                new Station("Kuressaare linn", null, 10.8, null, null),
                new Station("Tallinn-Harku", 26038, 9.7, 1.9, "Light shower")
            )
        );

        Observations observations = xmlMapper.readValue(stream, Observations.class);

        assertEquals(expectedObservations, observations);
    }
}
