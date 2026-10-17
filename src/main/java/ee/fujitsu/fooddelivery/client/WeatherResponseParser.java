package ee.fujitsu.fooddelivery.client;
import ee.fujitsu.fooddelivery.dto.WeatherDataDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor  
public class WeatherResponseParser {
    /**
     * Parses weather observations from the Estonian Environment Agency's XML response (API: https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php). 
     * @param dataStream Input stream from the response which contains XML
     * @return Parsed weather observations, individual fields for each station might be left empty when their values are missing
     * @throws ParserConfigurationException If the XML parser cannot be configured
     * @throws IOException If an error occurs while reading the input stream
     * @throws SAXException If the XML is malformed or cannot be parsed
     * @throws NumberFormatException If a numeric value exists but it cannot be converted from string to a number
     */
    public List<WeatherDataDto> parseObservations(InputStream dataStream) throws ParserConfigurationException, IOException, SAXException, NumberFormatException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        Element observations = factory.newDocumentBuilder().parse(dataStream).getDocumentElement();
        Integer timestamp = Integer.valueOf(observations.getAttribute("timestamp"));
        NodeList stations = observations.getChildNodes();
        List<WeatherDataDto> weatherData = new ArrayList<>(stations.getLength());

        for (int i = 0; i < stations.getLength(); i++) {
            Node stationNode = stations.item(i);
            if(stationNode.getNodeType() != Node.ELEMENT_NODE) 
                continue;
            Element station = (Element)stationNode;

            String wmoCode = getFirstElementText(station, "wmocode");
            String windSpeed = getFirstElementText(station, "windspeed");
            String airTemperature = getFirstElementText(station, "airtemperature");
            weatherData.add(new WeatherDataDto(
                getFirstElementText(station, "name"),
                wmoCode == null ? null : Integer.valueOf(wmoCode),
                airTemperature == null ? null : Double.valueOf(airTemperature),
                windSpeed == null ? null : Double.valueOf(windSpeed),
                getFirstElementText(station, "phenomenon"),
                timestamp
            ));
        }

        return weatherData;
    }

    private static String getFirstElementText(Element parent, String tagName) {
        NodeList elements = parent.getElementsByTagName(tagName);
        if (elements.getLength() == 0) {
            return null;
        }

        String value = elements.item(0).getTextContent().trim();
        return value.isEmpty() ? null : value;
    }
}
