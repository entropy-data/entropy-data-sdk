package entropydata.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

/**
 * Verifies what a connector reports about itself when it registers. The Entropy Data API is mocked with WireMock.
 */
@Testcontainers
class EntropyDataConnectorRegistrationTests {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Container
  WireMockContainer wiremockServer = new WireMockContainer("wiremock/wiremock:3.9.2")
      .withMappingFromResource("wiremock/integration.json");

  @Test
  void testReportsTheConnectorVersion() throws Exception {
    var client = new EntropyDataClient(wiremockServer.getBaseUrl(), "APIKEY");

    new EntropyDataConnectorRegistration(client, "unittest", "assets-synchronizer", "0.9.0").register();

    var info = registeredConnectorInfo();
    assertThat(info.path("type").asText()).isEqualTo("assets-synchronizer");
    assertThat(info.path("connectorVersion").asText()).isEqualTo("0.9.0");
  }

  @Test
  void testRegistersWithoutAVersion() throws Exception {
    var client = new EntropyDataClient(wiremockServer.getBaseUrl(), "APIKEY");

    new EntropyDataConnectorRegistration(client, "unittest", "assets-synchronizer").register();

    var info = registeredConnectorInfo();
    assertThat(info.path("type").asText()).isEqualTo("assets-synchronizer");
    assertThat(info.hasNonNull("connectorVersion")).isFalse();
  }

  private com.fasterxml.jackson.databind.JsonNode registeredConnectorInfo() throws Exception {
    var request = HttpRequest.newBuilder()
        .uri(URI.create(wiremockServer.getBaseUrl() + "/__admin/requests"))
        .GET()
        .build();
    var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

    var requests = objectMapper.readTree(response.body()).path("requests");
    for (var entry : requests) {
      var loggedRequest = entry.path("request");
      if ("PUT".equals(loggedRequest.path("method").asText())) {
        return objectMapper.readTree(loggedRequest.path("body").asText()).path("info");
      }
    }
    throw new AssertionError("No connector was registered");
  }
}
