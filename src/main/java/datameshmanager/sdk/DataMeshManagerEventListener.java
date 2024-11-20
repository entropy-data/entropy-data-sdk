package datameshmanager.sdk;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import datameshmanager.sdk.client.ApiException;
import datameshmanager.sdk.client.model.AccessActivatedEvent;
import datameshmanager.sdk.client.model.AccessApprovedEvent;
import datameshmanager.sdk.client.model.AccessCreatedEvent;
import datameshmanager.sdk.client.model.AccessDeactivatedEvent;
import datameshmanager.sdk.client.model.AccessDeletedEvent;
import datameshmanager.sdk.client.model.AccessRejectedEvent;
import datameshmanager.sdk.client.model.AccessRequestedEvent;
import datameshmanager.sdk.client.model.AccessUpdatedEvent;
import datameshmanager.sdk.client.model.AssetCreatedEvent;
import datameshmanager.sdk.client.model.AssetDeletedEvent;
import datameshmanager.sdk.client.model.AssetUpdatedEvent;
import datameshmanager.sdk.client.model.CloudEvent;
import datameshmanager.sdk.client.model.CloudEventData;
import datameshmanager.sdk.client.model.DataContractCreatedEvent;
import datameshmanager.sdk.client.model.DataContractDeletedEvent;
import datameshmanager.sdk.client.model.DataContractUpdatedEvent;
import datameshmanager.sdk.client.model.DataProductCreatedEvent;
import datameshmanager.sdk.client.model.DataProductDeletedEvent;
import datameshmanager.sdk.client.model.DataProductUpdatedEvent;
import datameshmanager.sdk.client.model.DefinitionCreatedEvent;
import datameshmanager.sdk.client.model.DefinitionDeletedEvent;
import datameshmanager.sdk.client.model.DefinitionUpdatedEvent;
import datameshmanager.sdk.client.model.OutputPortCreatedEvent;
import datameshmanager.sdk.client.model.OutputPortDeletedEvent;
import datameshmanager.sdk.client.model.OutputPortUpdatedEvent;
import datameshmanager.sdk.client.model.SourceSystemCreatedEvent;
import datameshmanager.sdk.client.model.SourceSystemDeletedEvent;
import datameshmanager.sdk.client.model.SourceSystemUpdatedEvent;
import datameshmanager.sdk.client.model.TagCreatedEvent;
import datameshmanager.sdk.client.model.TagDeletedEvent;
import datameshmanager.sdk.client.model.TagUpdatedEvent;
import datameshmanager.sdk.client.model.TeamCreatedEvent;
import datameshmanager.sdk.client.model.TeamDeletedEvent;
import datameshmanager.sdk.client.model.TeamUpdatedEvent;
import jakarta.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This EventListener subscribes to the events feeds in an infinite loop and processes the events.
 */
public class DataMeshManagerEventListener {

  private static final Logger log = LoggerFactory.getLogger(DataMeshManagerEventListener.class);

  private final String agentId;
  private final DataMeshManagerEventHandler eventHandler;
  private final DataMeshManagerClient client;
  private final DataMeshManagerStateRepository stateRepository;

  private final ObjectMapper objectMapper;
  private final DataMeshManagerAgentRegistration agentRegistration;

  private boolean stopped = false;
  private Duration pollInterval = Duration.ofSeconds(5);

  public DataMeshManagerEventListener(String agentId, DataMeshManagerClient client, DataMeshManagerEventHandler eventHandler,
      DataMeshManagerStateRepository stateRepository) {
    this.agentId = Objects.requireNonNull(agentId, "agentId must not be null");
    this.eventHandler = Objects.requireNonNull(eventHandler, "eventHandler must not be null");
    this.client = Objects.requireNonNull(client, "client must not be null");
    this.stateRepository = Objects.requireNonNull(stateRepository, "stateRepository must not be null");
    this.agentRegistration = new DataMeshManagerAgentRegistration(client, agentId, "event-listener");

    this.objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    this.agentRegistration.register();
  }

  /**
   * Starts the event listener to poll for events from the DataMeshManager in an infinite loop.
   */
  public void start() {
    log.info("{}: Start polling for events", agentId);

    // TODO error handling for agentRegistration

    var lastEventId = getLastEventId();
    while (!this.stopped) {
      try {

        List<datameshmanager.sdk.client.model.CloudEvent> events = fetchEvents(lastEventId);

        for (var event : events) {
          processEvent(event);
          lastEventId = Objects.requireNonNull(event.getId()).toString();
          saveLastEventId(lastEventId);
        }

        if (events.isEmpty()) {
          log.info("Got empty response, now wait for {} to make the next call", pollInterval);
          Thread.sleep(pollInterval.toMillis());
          continue;
        }

      } catch (InterruptedException e) {
        break;
      } catch (Exception e) {
        log.error("Failed to fetch events, now wait for 30 seconds to make the next call", e);
        try {
          Thread.sleep(Duration.ofSeconds(30).toMillis());
        } catch (InterruptedException ex) {
          break;
        }
      }
    }

    log.info("Stopped polling for events");
  }

  @Nullable
  private String getLastEventId() {
    return (String) stateRepository.getState().get("lastEventId");
  }

  private void saveLastEventId(String lastEventId) {
    stateRepository.saveState(Map.of("lastEventId", lastEventId));
  }


  public List<datameshmanager.sdk.client.model.CloudEvent> fetchEvents(String lastEventId) throws InterruptedException {
    log.info("Fetching events with lastEventId={}", lastEventId);
    List<CloudEvent> response = null;
    try {
      var events = client.getEventsApi().pollEvents(lastEventId, false);
      log.debug("Fetched {} events", events.size());
      return events;
    } catch (ApiException e) {
      log.error("Failed to fetch events", e);
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    if (this.stopped) {
      log.info("Polling for events already stopped");
      return;
    }
    this.stopped = true;
    log.info("Stopping polling for events");
  }

  public Duration getPollInterval() {
    return pollInterval;
  }

  public void setPollInterval(Duration pollInterval) {
    this.pollInterval = pollInterval;
  }

  private void processEvent(datameshmanager.sdk.client.model.CloudEvent event) {
    log.info("Processing event {} of type {}", event.getId(), event.getType());
    this.eventHandler.onEvent(event);
    switch (Objects.requireNonNull(event.getType())) {
      case "com.datamesh-manager.events.DataProductCreatedEvent" ->
          this.eventHandler.onDataProductCreatedEvent(convertPayload(event, DataProductCreatedEvent.class));
      case "com.datamesh-manager.events.DataProductUpdatedEvent" ->
          this.eventHandler.onDataProductUpdatedEvent(convertPayload(event, DataProductUpdatedEvent.class));
      case "com.datamesh-manager.events.DataProductDeletedEvent" ->
          this.eventHandler.onDataProductDeletedEvent(convertPayload(event, DataProductDeletedEvent.class));
      case "com.datamesh-manager.events.OutputPortCreatedEvent" ->
          this.eventHandler.onOutputPortCreatedEvent(convertPayload(event, OutputPortCreatedEvent.class));
      case "com.datamesh-manager.events.OutputPortUpdatedEvent" ->
          this.eventHandler.onOutputPortUpdatedEvent(convertPayload(event, OutputPortUpdatedEvent.class));
      case "com.datamesh-manager.events.OutputPortDeletedEvent" ->
          this.eventHandler.onOutputPortDeletedEvent(convertPayload(event, OutputPortDeletedEvent.class));
      case "com.datamesh-manager.events.DataContractCreatedEvent" ->
          this.eventHandler.onDataContractCreatedEvent(convertPayload(event, DataContractCreatedEvent.class));
      case "com.datamesh-manager.events.DataContractUpdatedEvent" ->
          this.eventHandler.onDataContractUpdatedEvent(convertPayload(event, DataContractUpdatedEvent.class));
      case "com.datamesh-manager.events.DataContractDeletedEvent" ->
          this.eventHandler.onDataContractDeletedEvent(convertPayload(event, DataContractDeletedEvent.class));
      case "com.datamesh-manager.events.AccessCreatedEvent" ->
          this.eventHandler.onAccessCreatedEvent(convertPayload(event, AccessCreatedEvent.class));
      case "com.datamesh-manager.events.AccessUpdatedEvent" ->
          this.eventHandler.onAccessUpdatedEvent(convertPayload(event, AccessUpdatedEvent.class));
      case "com.datamesh-manager.events.AccessDeletedEvent" ->
          this.eventHandler.onAccessDeletedEvent(convertPayload(event, AccessDeletedEvent.class));
      case "com.datamesh-manager.events.AccessRequestedEvent" ->
          this.eventHandler.onAccessRequestedEvent(convertPayload(event, AccessRequestedEvent.class));
      case "com.datamesh-manager.events.AccessApprovedEvent" ->
          this.eventHandler.onAccessApprovedEvent(convertPayload(event, AccessApprovedEvent.class));
      case "com.datamesh-manager.events.AccessRejectedEvent" ->
          this.eventHandler.onAccessRejectedEvent(convertPayload(event, AccessRejectedEvent.class));
      case "com.datamesh-manager.events.AccessActivatedEvent" ->
          this.eventHandler.onAccessActivatedEvent(convertPayload(event, AccessActivatedEvent.class));
      case "com.datamesh-manager.events.AccessDeactivatedEvent" ->
          this.eventHandler.onAccessDeactivatedEvent(convertPayload(event, AccessDeactivatedEvent.class));
      case "com.datamesh-manager.events.SourceSystemCreatedEvent" ->
          this.eventHandler.onSourceSystemCreatedEvent(convertPayload(event, SourceSystemCreatedEvent.class));
      case "com.datamesh-manager.events.SourceSystemUpdatedEvent" ->
          this.eventHandler.onSourceSystemUpdatedEvent(convertPayload(event, SourceSystemUpdatedEvent.class));
      case "com.datamesh-manager.events.SourceSystemDeletedEvent" ->
          this.eventHandler.onSourceSystemDeletedEvent(convertPayload(event, SourceSystemDeletedEvent.class));
      case "com.datamesh-manager.events.TeamCreatedEvent" ->
          this.eventHandler.onTeamCreatedEvent(convertPayload(event, TeamCreatedEvent.class));
      case "com.datamesh-manager.events.TeamUpdatedEvent" ->
          this.eventHandler.onTeamUpdatedEvent(convertPayload(event, TeamUpdatedEvent.class));
      case "com.datamesh-manager.events.TeamDeletedEvent" ->
          this.eventHandler.onTeamDeletedEvent(convertPayload(event, TeamDeletedEvent.class));
      case "com.datamesh-manager.events.DefinitionCreatedEvent" ->
          this.eventHandler.onDefinitionCreatedEvent(convertPayload(event, DefinitionCreatedEvent.class));
      case "com.datamesh-manager.events.DefinitionUpdatedEvent" ->
          this.eventHandler.onDefinitionUpdatedEvent(convertPayload(event, DefinitionUpdatedEvent.class));
      case "com.datamesh-manager.events.DefinitionDeletedEvent" ->
          this.eventHandler.onDefinitionDeletedEvent(convertPayload(event, DefinitionDeletedEvent.class));
      case "com.datamesh-manager.events.TagCreatedEvent" ->
          this.eventHandler.onTagCreatedEvent(convertPayload(event, TagCreatedEvent.class));
      case "com.datamesh-manager.events.TagUpdatedEvent" ->
          this.eventHandler.onTagUpdatedEvent(convertPayload(event, TagUpdatedEvent.class));
      case "com.datamesh-manager.events.TagDeletedEvent" ->
          this.eventHandler.onTagDeletedEvent(convertPayload(event, TagDeletedEvent.class));
      case "com.datamesh-manager.events.AssetCreatedEvent" ->
          this.eventHandler.onAssetCreatedEvent(convertPayload(event, AssetCreatedEvent.class));
      case "com.datamesh-manager.events.AssetUpdatedEvent" ->
          this.eventHandler.onAssetUpdatedEvent(convertPayload(event, AssetUpdatedEvent.class));
      case "com.datamesh-manager.events.AssetDeletedEvent" ->
          this.eventHandler.onAssetDeletedEvent(convertPayload(event, AssetDeletedEvent.class));
      case "com.datamesh-manager.events.TestResultsCreatedEvent" -> this.eventHandler.onTestResultsCreatedEvent(
          convertPayload(event, datameshmanager.sdk.client.model.TestResultsCreatedEvent.class));
      case "com.datamesh-manager.events.TestResultsDeletedEvent" -> this.eventHandler.onTestResultsDeletedEvent(
          convertPayload(event, datameshmanager.sdk.client.model.TestResultsDeletedEvent.class));
      default -> log.warn("Unknown event type: {}", event.getType());

    }

  }

  private <T> T convertPayload(datameshmanager.sdk.client.model.CloudEvent event, Class<T> payloadType) {
    CloudEventData data = event.getData();
    return objectMapper.convertValue(data, payloadType);
  }


}
