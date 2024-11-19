package datameshmanager.sdk;

import datameshmanager.sdk.client.model.Asset;

/**
 * Implementations of this interface fetch assets (tables, views, schemas, ...) from the data platform or data catalog. They convert them to the Asset format of the Data Mesh Manager, and can either send them for creation/update or for deletion to synchronize the asset metadata.
 */
public interface DataMeshManagerAssetsProvider {
  void fetchAssets(AssetCallback callback);

  interface AssetCallback {
    void onAssetUpdated(Asset asset);
    void onAssetDeleted(String id);
  }

}

