package entropydata.sdk;

import entropydata.sdk.client.ApiException;
import entropydata.sdk.client.model.Connector;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Entropy Data Connector API to store the state
 */
public class EntropyDataStateRepositoryRemote implements EntropyDataStateRepository {

  private static final Logger log = LoggerFactory.getLogger(EntropyDataConnectorRegistration.class);

  private final String connectorId;
  private final EntropyDataClient client;

  public EntropyDataStateRepositoryRemote(String connectorId, EntropyDataClient client) {
    this.connectorId = connectorId;
    this.client = client;
  }

  @Override
  public Map<String, Object> getState() {
    return client.getConnectorsApi().getConnector(connectorId).getState();
  }

  @Override
  public void saveState( Map<String, Object> state) {
    try {
      Connector connector = client.getConnectorsApi().getConnector(connectorId);
      connector.setState(state);
      client.getConnectorsApi().putConnector(connectorId, connector);
    } catch (ApiException e) {
      if (e.getCode() == 404) {
        log.error("Connector with id {} not found, please register it first", connectorId);
        throw e;
      } else {
        throw e;
      }
    }
  }

}
