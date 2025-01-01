package datameshmanager.sdk;

import datameshmanager.sdk.client.ApiException;
import datameshmanager.sdk.client.model.Connector;
import datameshmanager.sdk.client.model.ConnectorInfo;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataMeshManagerConnectorRegistration {

  private static final Logger log = LoggerFactory.getLogger(DataMeshManagerConnectorRegistration.class);

  private final DataMeshManagerClient client;

  /**
   * The unique identifier of the connector.
   * This is used to identify the connector in the Data Mesh Manager and to exchange state.
   * The identifier should be constant across restarts of the connector.
   */
  private final String id;

  /**
   * The type of the connector (e.g. databricks-assets, databricks-access, aws-costs, ...).
   */
  private final String type;

  public DataMeshManagerConnectorRegistration(DataMeshManagerClient client, String connectorId, String type) {
    this.client = client;
    this.id = Objects.requireNonNull(connectorId, "Connector ID is required");
    this.type = Objects.requireNonNull(type, "Connector type is required");
  }

  public void register() {

    Connector connector;
    try {
      log.debug("Checking if integration connector {} already exists", id);
      connector = client.getConnectorsApi().getConnector(id);
    } catch (ApiException e) {
      if (e.getCode() == 404) {
        connector = new Connector();
      } else {
        throw e;
      }
    }

    connector
        .id(id)
        .info(new ConnectorInfo()
            .type(type)
        );

    log.info("Registering integration connector {}", id);
    client.getConnectorsApi().putConnector(id, connector);
  }

  public void delete() {
    log.info("Deleting integration connector {}", this.id);
    try {
      client.getConnectorsApi().deleteConnector(this.id);
    } catch (ApiException e) {
      if (e.getCode() == 404) {
        log.error("Integration connector with id {} already deleted", this.id);
      } else {
        throw e;
      }
    }
  }

}
