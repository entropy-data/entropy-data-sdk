package datameshmanager.sdk;

import datameshmanager.sdk.DataMeshManagerAssetsProvider.AssetCallback;
import datameshmanager.sdk.client.ApiException;
import datameshmanager.sdk.client.model.Asset;
import java.time.Duration;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataMeshManagerAssetsSynchronizer {

  private static final Logger log = LoggerFactory.getLogger(DataMeshManagerAssetsSynchronizer.class);

  private final String agentId;
  private final DataMeshManagerClient client;
  private final DataMeshManagerAgentRegistration agentRegistration;
  private final DataMeshManagerAssetsProvider assetsProvider;
  private volatile boolean stopped = false;

  private Duration delay = Duration.parse("PT60M");

  public DataMeshManagerAssetsSynchronizer(
      String agentId,
      DataMeshManagerClient client,
      DataMeshManagerAssetsProvider assetsProvider) {
    this.agentId = agentId;
    this.client = client;
    this.assetsProvider = assetsProvider;
    this.agentRegistration = new DataMeshManagerAgentRegistration(client, agentId, "assets-synchronizer");

    this.agentRegistration.register();
  }

  public void start() {
    log.info("{}: start syncing assets", agentId);

    // TODO error handling for agentRegistration
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
      log.info("{}: Already stopped asset synchronization", agentId);
      return;
    }
    this.stopped = true;
    log.info("{}: Stopped syncing assets", agentId);
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
