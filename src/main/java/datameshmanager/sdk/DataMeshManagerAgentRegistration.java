package datameshmanager.sdk;

import datameshmanager.sdk.client.model.IntegrationAgent;
import datameshmanager.sdk.client.model.IntegrationAgentHealth;
import datameshmanager.sdk.client.model.IntegrationAgentInfo;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataMeshManagerAgentRegistration {

  private static final Logger log = LoggerFactory.getLogger(DataMeshManagerAgentRegistration.class);

  private final DataMeshManagerClient client;

  /**
   * The unique identifier of the agent.
   * This is used to identify the agent in the Data Mesh Manager and to exchange state.
   * The identifier should be constant across restarts of the agent.
   */
  private final String id;

  /**
   * The type of the agent (e.g. databricks-assets, databricks-access, aws-costs, ...).
   */
  private final String type;

  public DataMeshManagerAgentRegistration(DataMeshManagerClient client, String id, String type) {
    this.client = client;
    this.id = Objects.requireNonNull(id, "Agent ID is required");
    this.type = Objects.requireNonNull(type, "Agent type is required");
  }

  public void register() {
    IntegrationAgent integrationAgent = client.getIntegrationsApi().getIntegrationAgent(id);

    if (integrationAgent == null) {
      integrationAgent = new IntegrationAgent();
    }

    integrationAgent
        .id(id)
        .info(new IntegrationAgentInfo()
            .type(type)
        )
        .health(new IntegrationAgentHealth()
            .startedAt(OffsetDateTime.now())
            .status("UP")
        );

    client.getIntegrationsApi().putIntegrationAgent(id, integrationAgent);
  }

  public void stop() {
    IntegrationAgent integrationAgent = client.getIntegrationsApi().getIntegrationAgent(id);
    if (integrationAgent.getHealth() == null) {
      integrationAgent.setHealth(new IntegrationAgentHealth());
    }
    integrationAgent.getHealth().setStatus("STOPPED");
    integrationAgent.getHealth().setErrorMessage(null);
    log.info("Publish integration agent {} in status STOPPED", id);
    client.getIntegrationsApi().putIntegrationAgent(id, integrationAgent);
  }

  public void up(String id) {
    IntegrationAgent integrationAgent = client.getIntegrationsApi().getIntegrationAgent(id);
    if (integrationAgent.getHealth() == null) {
      integrationAgent.setHealth(new IntegrationAgentHealth());
    }
    integrationAgent.getHealth().setStatus("UP");
    integrationAgent.getHealth().setErrorMessage(null);
    log.info("Publish integration agent {} in status UP", id);
    client.getIntegrationsApi().putIntegrationAgent(id, integrationAgent);
  }

  public void error(String id, String message) {
    IntegrationAgent integrationAgent = client.getIntegrationsApi().getIntegrationAgent(id);
    if (integrationAgent.getHealth() == null) {
      integrationAgent.setHealth(new IntegrationAgentHealth());
    }
    integrationAgent.getHealth().setStatus("ERROR");
    integrationAgent.getHealth().setErrorMessage(message);
    log.info("Publish integration agent {} in status ERROR: {}", id, message);
    client.getIntegrationsApi().putIntegrationAgent(id, integrationAgent);
  }

  public void reset(String id) {
    IntegrationAgent integrationAgent = client.getIntegrationsApi().getIntegrationAgent(id);
    if (integrationAgent.getHealth() == null) {
      integrationAgent.setHealth(new IntegrationAgentHealth());
    }
    integrationAgent.getHealth().setStatus("UP");
    integrationAgent.getHealth().setErrorMessage(null);
    integrationAgent.getHealth().setUpdatedAt(null);
    integrationAgent.setState(null);
    log.info("Publish integration agent {} to reset", id);
    client.getIntegrationsApi().putIntegrationAgent(id, integrationAgent);
  }

  public void delete(String id) {
    log.info("Deleting integration agent {}", id);
    client.getIntegrationsApi().deleteIntegrationAgent(id);
  }

}
