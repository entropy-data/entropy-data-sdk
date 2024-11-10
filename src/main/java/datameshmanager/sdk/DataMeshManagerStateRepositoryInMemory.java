package datameshmanager.sdk;

import java.util.HashMap;
import java.util.Map;

public class DataMeshManagerStateRepositoryInMemory implements DataMeshManagerStateRepository {

  private final Map<String, String> lastEventIds = new HashMap<>();

  @Override
  public String getLastEventId(String eventListenerId) {
    return lastEventIds.get(eventListenerId);
  }

  @Override
  public String saveLastEventId(String eventListenerId, String lastEventId) {
    return lastEventIds.put(eventListenerId, lastEventId);
  }

}
