package datameshmanager.sdk;

import java.util.Map;

public interface DataMeshManagerStateRepository {

  Map<String, Object> getState();
  void saveState(Map<String, Object> state);

}
