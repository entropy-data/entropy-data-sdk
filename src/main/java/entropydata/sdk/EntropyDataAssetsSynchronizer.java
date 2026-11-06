package entropydata.sdk;

import entropydata.sdk.EntropyDataAssetsProvider.AssetCallback;
import entropydata.sdk.client.model.Asset;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EntropyDataAssetsSynchronizer {

  private static final Logger log = LoggerFactory.getLogger(EntropyDataAssetsSynchronizer.class);

  private final String connectorId;
  private final EntropyDataClient client;
  private final EntropyDataConnectorRegistration connectorRegistration;
  private final EntropyDataAssetsProvider assetsProvider;
  private final EntropyDataAssetWriter assetWriter;
  private volatile boolean stopped = false;

  private Duration delay = Duration.parse("PT60M");

  public EntropyDataAssetsSynchronizer(
      String connectorId,
      EntropyDataClient client,
      EntropyDataAssetsProvider assetsProvider) {
    this(connectorId, client, assetsProvider, null);
  }

  public EntropyDataAssetsSynchronizer(
      String connectorId,
      EntropyDataClient client,
      EntropyDataAssetsProvider assetsProvider,
      String connectorVersion) {
    this.connectorId = connectorId;
    this.client = client;
    this.assetsProvider = assetsProvider;
    this.assetWriter = new EntropyDataAssetWriter(client.getAssetsApi());
    this.connectorRegistration = new EntropyDataConnectorRegistration(client, connectorId, "assets-synchronizer", connectorVersion);

    this.connectorRegistration.register();
  }

  public void start() {
    log.info("{}: start syncing assets", connectorId);

    // TODO error handling for connectorRegistration

    while (!this.stopped) {
      try {
        synchronizeAssets();
      } catch (Exception e) {
        // Errors (such as OutOfMemoryError) are deliberately not caught, so the process can terminate and be restarted
        log.error("{}: Failed to synchronize assets, now wait for {} to make the next attempt", connectorId, delay, e);
      }
      try {
        log.info("Waiting for {} until next sync ...", delay);
        Thread.sleep(delay.toMillis());
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  public void stop() {
    if (this.stopped) {
      log.info("{}: Already stopped asset synchronization", connectorId);
      return;
    }
    this.stopped = true;
    log.info("{}: Stopped syncing assets", connectorId);
  }

  public void synchronizeAssets() {
    assetsProvider.fetchAssets(new AssetCallback() {
      @Override
      public void onAssetUpdated(Asset asset) {
        saveAsset(asset);
      }

      @Override
      public void onAssetDeleted(String id) {
        deleteAsset(id);
      }
    });
  }

  public Duration getDelay() {
    return delay;
  }

  public void setDelay(Duration delay) {
    this.delay = delay;
  }

  public void saveAsset(Asset asset) {
    assetWriter.save(asset);
  }

  public void deleteAsset(String id) {
    assetWriter.delete(id);
  }

}
