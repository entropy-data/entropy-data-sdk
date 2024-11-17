package datameshmanager.sdk;

import datameshmanager.sdk.client.ApiException;
import datameshmanager.sdk.client.model.IntegrationAgent;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Data Mesh Manager Integration Agent API to store the state
 */
public class DataMeshManagerStateRepositoryRemote implements DataMeshManagerStateRepository {

  private static final Logger log = LoggerFactory.getLogger(DataMeshManagerAgentRegistration.class);

  private final DataMeshManagerClient client;

  public DataMeshManagerStateRepositoryRemote(DataMeshManagerClient client) {
    this.client = client;
  }

  @Override
  public Map<String, Object> getState(String id) {
    return client.getIntegrationsApi().getIntegrationAgent(id).getState();
  }

  @Override
  public void saveState(String id, Map<String, Object> state) {
    try {
      IntegrationAgent agent = client.getIntegrationsApi().getIntegrationAgent(id);
      agent.setState(state);
      client.getIntegrationsApi().putIntegrationAgent(id, agent);
    } catch (ApiException e) {
      if (e.getCode() == 404) {
        log.error("Integration agent with id {} not found, please register it first", id);
        throw e;
      } else {
        throw e;
      }
    }
  }

}
