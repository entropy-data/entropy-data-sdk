package datameshmanager.sdk;

import datameshmanager.sdk.client.model.Asset;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface DataMeshManagerAssetsProvider {
  void publishAssetsToConsumer(Consumer<Asset> consumer);
}
