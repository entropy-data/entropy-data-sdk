package datameshmanager.sdk;

import datameshmanager.sdk.client.model.Asset;
import java.util.stream.Stream;

public interface DataMeshManagerAssetsProvider {
   // TODO evtl. supplier
  Stream<Asset> streamAssets();

}
