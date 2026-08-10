package entropydata.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

/**
 * Verifies that a failed synchronization run does not terminate the synchronization loop. The Entropy Data API is mocked with WireMock.
 */
@Testcontainers
class EntropyDataAssetsSynchronizerTests {

  @Container
  WireMockContainer wiremockServer = new WireMockContainer("wiremock/wiremock:3.9.2")
      .withMappingFromResource("wiremock/integration.json");

  @Test
  void testSynchronizationContinuesAfterFailedRun() throws Exception {
    var client = new EntropyDataClient(wiremockServer.getBaseUrl(), "APIKEY");
    var attempts = new AtomicInteger(0);

    EntropyDataAssetsProvider failingOnFirstRun = callback -> {
      if (attempts.incrementAndGet() == 1) {
        throw new RuntimeException("Simulated failure of the data platform");
      }
    };

    var synchronizer = new EntropyDataAssetsSynchronizer("unittest", client, failingOnFirstRun);
    synchronizer.setDelay(Duration.ofMillis(100));

    var thread = new Thread(synchronizer::start);
    thread.start();

    try {
      for (int i = 0; i < 100 && attempts.get() < 2; i++) {
        Thread.sleep(100);
      }
    } finally {
      synchronizer.stop();
      thread.interrupt();
      thread.join(Duration.ofSeconds(5).toMillis());
    }

    assertThat(attempts.get()).isGreaterThanOrEqualTo(2);
    assertThat(thread.isAlive()).isFalse();
  }

}
