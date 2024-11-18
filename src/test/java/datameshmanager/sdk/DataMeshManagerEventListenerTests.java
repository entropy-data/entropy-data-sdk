package datameshmanager.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import datameshmanager.sdk.client.ApiException;
import datameshmanager.sdk.client.api.AccessApi;
import datameshmanager.sdk.client.api.DataProductsApi;
import datameshmanager.sdk.client.model.Access;
import datameshmanager.sdk.client.model.AccessActivatedEvent;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

/**
 * A full integration test that starts the DataMeshManagerEventListener, polls for events and gets the Access and DataProduct resources via
 * API calls. The Data Mesh Manager API is mocked with WireMock.
 */
@Testcontainers
class DataMeshManagerEventListenerTests {

  private static final Logger log = LoggerFactory.getLogger(DataMeshManagerEventListenerTests.class);

  @Container
  WireMockContainer wiremockServer = new WireMockContainer("wiremock/wiremock:3.9.2")
      .withMappingFromResource("wiremock/events.json")
      .withMappingFromResource("wiremock/dataproduct.json")
      .withMappingFromResource("wiremock/access.json")
      .withMappingFromResource("wiremock/integration.json");

  @Test
  void testEventListener() throws Exception {

    var eventsCount = new AtomicInteger(0);
    var dataPlatformRole = new String[1];

    var client = new DataMeshManagerClient(new DataMeshManagerClientProperties(
        wiremockServer.getBaseUrl(),
        "APIKEY"
    ));
    DataMeshManagerEventHandler testEventHandler = new DataMeshManagerEventHandler() {
      @Override
      public void onAccessActivatedEvent(AccessActivatedEvent event) {
        assertNotNull(event);
        eventsCount.incrementAndGet();
        log.info("Received AccessActivatedEvent: {}", event);

        var apiClient = client.getApiClient();
        AccessApi accessApi = new AccessApi(apiClient);
        var dataProductApi = new DataProductsApi(apiClient);

        log.info("Getting access: {}", event.getId());
        try {
          Access access = accessApi.getAccess(event.getId());
          assertThat(access).isNotNull();
          log.info("Getting data product: {}", access.getProvider().getDataProductId());
          var providerDataProduct = dataProductApi.getDataProduct(access.getProvider().getDataProductId());
          assertThat(providerDataProduct).isNotNull();
          dataPlatformRole[0] = providerDataProduct.getCustom().get("platformRole");
        } catch (ApiException e) {
          log.error("Error getting access", e);
        }
      }
    };
    DataMeshManagerStateRepositoryInMemory stateRepository = new DataMeshManagerStateRepositoryInMemory();
    var eventListener = new DataMeshManagerEventListener("unittest", client, testEventHandler, stateRepository);

    new Thread(eventListener::start).start();
    Thread.sleep(1000);
    eventListener.stop();

    assertThat(eventsCount.get()).isEqualTo(1);
    assertThat(dataPlatformRole[0]).isEqualTo("dp_my_example_data_product_role");
  }

}
