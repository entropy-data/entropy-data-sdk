package datameshmanager.sdk;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple in-memory implementation of DataMeshManagerStateRepository.
 */
public class DataMeshManagerStateRepositoryInMemory implements DataMeshManagerStateRepository {

  private final String agentId;

  private final Map<String, Map<String, Object>> state = new HashMap<>(); // key: agentId, value: state

  public DataMeshManagerStateRepositoryInMemory(String agentId) {
    this.agentId = agentId;
  }

  @Override
  public Map<String, Object> getState() {
    return this.state.getOrDefault(agentId, new HashMap<>());
  }

  @Override
  public void saveState(Map<String, Object> state) {
    this.state.put(agentId, state);
  }
}
