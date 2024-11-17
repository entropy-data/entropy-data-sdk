package datameshmanager.sdk;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple in-memory implementation of DataMeshManagerStateRepository.
 */
public class DataMeshManagerStateRepositoryInMemory implements DataMeshManagerStateRepository {

  private final Map<String, Map<String, Object>> state = new HashMap<>();

  @Override
  public Map<String, Object> getState(String id) {
    return this.state.getOrDefault(id, new HashMap<>());
  }

  @Override
  public void saveState(String id, Map<String, Object> state) {
    this.state.put(id, state);
  }
}
