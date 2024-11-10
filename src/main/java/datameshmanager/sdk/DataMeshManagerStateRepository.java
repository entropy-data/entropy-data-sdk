package datameshmanager.sdk;

public interface DataMeshManagerStateRepository {

  String getLastEventId(String eventListenerId);
  String saveLastEventId(String eventListenerId, String lastEventId);

}
