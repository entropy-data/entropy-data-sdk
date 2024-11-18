package datameshmanager.sdk;

import datameshmanager.sdk.client.ApiException;
import datameshmanager.sdk.client.model.Asset;
import jakarta.annotation.PreDestroy;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;


public class DataMeshManagerAssetsSynchronizer {

  private static final Logger log = LoggerFactory.getLogger(DataMeshManagerAssetsSynchronizer.class);

  private final DataMeshManagerClient client;
  private final DataMeshManagerAgentRegistration agentRegistration;
  private final DataMeshManagerAssetsProvider assetsProvider;

  public DataMeshManagerAssetsSynchronizer(String id, DataMeshManagerClient client,
      DataMeshManagerAssetsProvider assetsProvider) {
    this.client = client;
    this.assetsProvider = assetsProvider;
    this.agentRegistration = new DataMeshManagerAgentRegistration(client, id, "assets-asynchronizer");

    this.agentRegistration.register();
  }

  public void start() {
    this.agentRegistration.up();
    // TODO loop with delay
    synchronizeDatabricksAssets();
  }

  public void stop() {
    this.agentRegistration.stop();
    log.info("Stopped asset synchronization");
  }

  protected void synchronizeDatabricksAssets() {
    assetsProvider.streamAssets().forEach(this::saveAsset);
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
