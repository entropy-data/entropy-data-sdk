package entropydata.sdk;

import entropydata.sdk.EntropyDataAssetsProvider.AssetCallback;
import entropydata.sdk.client.ApiException;
import entropydata.sdk.client.model.Asset;
import java.time.Duration;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EntropyDataAssetsSynchronizer {

  private static final Logger log = LoggerFactory.getLogger(EntropyDataAssetsSynchronizer.class);

  private final String connectorId;
  private final EntropyDataClient client;
  private final EntropyDataConnectorRegistration connectorRegistration;
  private final EntropyDataAssetsProvider assetsProvider;
  private volatile boolean stopped = false;

  private Duration delay = Duration.parse("PT60M");

  public EntropyDataAssetsSynchronizer(
      String connectorId,
      EntropyDataClient client,
      EntropyDataAssetsProvider assetsProvider) {
    this.connectorId = connectorId;
    this.client = client;
    this.assetsProvider = assetsProvider;
    this.connectorRegistration = new EntropyDataConnectorRegistration(client, connectorId, "assets-synchronizer");

    this.connectorRegistration.register();
  }

  public void start() {
    log.info("{}: start syncing assets", connectorId);

    // TODO error handling for connectorRegistration
    // TODO error handling during while loop

    while (!this.stopped) {
      synchronizeAssets();
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
    try {
      Asset existingAsset = this.client.getAssetsApi().getAsset(asset.getId());
      if (Objects.deepEquals(asset, existingAsset)) {
        log.info("Asset {} already exists and unchanged", asset.getId());
        return;
      }
    } catch (ApiException e) {
      if (e.getCode() == 404) {
        log.debug("Asset {} does not exist, so continue", asset.getId());
      } else {
        throw e;
      }
    }

    log.info("Saving asset {}", asset.getId());
    client.getAssetsApi().addAsset(asset.getId(), asset);
  }

  public void deleteAsset(String id) {
    log.info("Deleting asset {}", id);
    client.getAssetsApi().deleteAsset(id);
  }

}
