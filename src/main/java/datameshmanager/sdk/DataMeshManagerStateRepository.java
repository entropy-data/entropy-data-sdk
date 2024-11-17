package datameshmanager.sdk;

import java.util.Map;

public interface DataMeshManagerStateRepository {

  Map<String, Object> getState(String id);
  void saveState(String id, Map<String, Object> state);

}
