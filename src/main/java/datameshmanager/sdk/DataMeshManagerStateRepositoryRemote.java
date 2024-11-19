package datameshmanager.sdk;

import datameshmanager.sdk.client.ApiException;
import datameshmanager.sdk.client.model.IntegrationAgent;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Data Mesh Manager Integration Agent API to store the state
 */
public class DataMeshManagerStateRepositoryRemote implements DataMeshManagerStateRepository {

  private static final Logger log = LoggerFactory.getLogger(DataMeshManagerAgentRegistration.class);

  private final String agentId;
  private final DataMeshManagerClient client;

  public DataMeshManagerStateRepositoryRemote(String agentId, DataMeshManagerClient client) {
    this.agentId = agentId;
    this.client = client;
  }

  @Override
  public Map<String, Object> getState() {
    return client.getIntegrationsApi().getIntegrationAgent(agentId).getState();
  }

  @Override
  public void saveState( Map<String, Object> state) {
    try {
      IntegrationAgent agent = client.getIntegrationsApi().getIntegrationAgent(agentId);
      agent.setState(state);
      client.getIntegrationsApi().putIntegrationAgent(agentId, agent);
    } catch (ApiException e) {
      if (e.getCode() == 404) {
        log.error("Integration agent with id {} not found, please register it first", agentId);
        throw e;
      } else {
        throw e;
      }
    }
  }

}
