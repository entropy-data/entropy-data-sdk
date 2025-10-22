package entropydata.sdk;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import entropydata.sdk.client.ApiException;
import entropydata.sdk.client.model.AccessActivatedEvent;
import entropydata.sdk.client.model.AccessApprovedEvent;
import entropydata.sdk.client.model.AccessCreatedEvent;
import entropydata.sdk.client.model.AccessDeactivatedEvent;
import entropydata.sdk.client.model.AccessDeletedEvent;
import entropydata.sdk.client.model.AccessRejectedEvent;
import entropydata.sdk.client.model.AccessRequestedEvent;
import entropydata.sdk.client.model.AccessUpdatedEvent;
import entropydata.sdk.client.model.AssetCreatedEvent;
import entropydata.sdk.client.model.AssetDeletedEvent;
import entropydata.sdk.client.model.AssetUpdatedEvent;
import entropydata.sdk.client.model.CloudEvent;
import entropydata.sdk.client.model.DataContractCreatedEvent;
import entropydata.sdk.client.model.DataContractDeletedEvent;
import entropydata.sdk.client.model.DataContractUpdatedEvent;
import entropydata.sdk.client.model.DataProductCreatedEvent;
import entropydata.sdk.client.model.DataProductDeletedEvent;
import entropydata.sdk.client.model.DataProductUpdatedEvent;
import entropydata.sdk.client.model.DefinitionCreatedEvent;
import entropydata.sdk.client.model.DefinitionDeletedEvent;
import entropydata.sdk.client.model.DefinitionUpdatedEvent;
import entropydata.sdk.client.model.OutputPortCreatedEvent;
import entropydata.sdk.client.model.OutputPortDeletedEvent;
import entropydata.sdk.client.model.OutputPortUpdatedEvent;
import entropydata.sdk.client.model.SourceSystemCreatedEvent;
import entropydata.sdk.client.model.SourceSystemDeletedEvent;
import entropydata.sdk.client.model.SourceSystemUpdatedEvent;
import entropydata.sdk.client.model.TagCreatedEvent;
import entropydata.sdk.client.model.TagDeletedEvent;
import entropydata.sdk.client.model.TagUpdatedEvent;
import entropydata.sdk.client.model.TeamCreatedEvent;
import entropydata.sdk.client.model.TeamDeletedEvent;
import entropydata.sdk.client.model.TeamUpdatedEvent;
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
public class EntropyDataEventListener {

  private static final Logger log = LoggerFactory.getLogger(EntropyDataEventListener.class);

  private final String connectorId;
  private final EntropyDataEventHandler eventHandler;
  private final EntropyDataClient client;
  private final EntropyDataStateRepository stateRepository;

  private final ObjectMapper objectMapper;
  private final EntropyDataConnectorRegistration connectorRegistration;

  private boolean stopped = false;
  private Duration pollInterval = Duration.ofSeconds(5);

  public EntropyDataEventListener(String connectorId, String type, EntropyDataClient client, EntropyDataEventHandler eventHandler,
      EntropyDataStateRepository stateRepository) {
    this.connectorId = Objects.requireNonNull(connectorId, "connectorId must not be null");
    this.eventHandler = Objects.requireNonNull(eventHandler, "eventHandler must not be null");
    this.client = Objects.requireNonNull(client, "client must not be null");
    this.stateRepository = Objects.requireNonNull(stateRepository, "stateRepository must not be null");
    this.connectorRegistration = new EntropyDataConnectorRegistration(client, connectorId, type);

    this.objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    this.connectorRegistration.register();
  }

  /**
   * Starts the event listener to poll for events from the DataMeshManager in an infinite loop.
   */
  public void start() {
    log.info("{}: Start polling for events", connectorId);

    // TODO error handling for connectorRegistration

    var lastEventId = getLastEventId();
    while (!this.stopped) {
      try {

        List<entropydata.sdk.client.model.CloudEvent> events = fetchEvents(lastEventId);

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


  public List<entropydata.sdk.client.model.CloudEvent> fetchEvents(String lastEventId) throws InterruptedException {
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

  private void processEvent(entropydata.sdk.client.model.CloudEvent event) {
    log.info("Processing event {} of type {}", event.getId(), event.getType());
    this.eventHandler.onEvent(event);
    switch (Objects.requireNonNull(event.getType())) {
      case "com.entropy-data.events.DataProductCreatedEvent" ->
          this.eventHandler.onDataProductCreatedEvent(convertPayload(event, DataProductCreatedEvent.class));
      case "com.entropy-data.events.DataProductUpdatedEvent" ->
          this.eventHandler.onDataProductUpdatedEvent(convertPayload(event, DataProductUpdatedEvent.class));
      case "com.entropy-data.events.DataProductDeletedEvent" ->
          this.eventHandler.onDataProductDeletedEvent(convertPayload(event, DataProductDeletedEvent.class));
      case "com.entropy-data.events.OutputPortCreatedEvent" ->
          this.eventHandler.onOutputPortCreatedEvent(convertPayload(event, OutputPortCreatedEvent.class));
      case "com.entropy-data.events.OutputPortUpdatedEvent" ->
          this.eventHandler.onOutputPortUpdatedEvent(convertPayload(event, OutputPortUpdatedEvent.class));
      case "com.entropy-data.events.OutputPortDeletedEvent" ->
          this.eventHandler.onOutputPortDeletedEvent(convertPayload(event, OutputPortDeletedEvent.class));
      case "com.entropy-data.events.DataContractCreatedEvent" ->
          this.eventHandler.onDataContractCreatedEvent(convertPayload(event, DataContractCreatedEvent.class));
      case "com.entropy-data.events.DataContractUpdatedEvent" ->
          this.eventHandler.onDataContractUpdatedEvent(convertPayload(event, DataContractUpdatedEvent.class));
      case "com.entropy-data.events.DataContractDeletedEvent" ->
          this.eventHandler.onDataContractDeletedEvent(convertPayload(event, DataContractDeletedEvent.class));
      case "com.entropy-data.events.AccessCreatedEvent" ->
          this.eventHandler.onAccessCreatedEvent(convertPayload(event, AccessCreatedEvent.class));
      case "com.entropy-data.events.AccessUpdatedEvent" ->
          this.eventHandler.onAccessUpdatedEvent(convertPayload(event, AccessUpdatedEvent.class));
      case "com.entropy-data.events.AccessDeletedEvent" ->
          this.eventHandler.onAccessDeletedEvent(convertPayload(event, AccessDeletedEvent.class));
      case "com.entropy-data.events.AccessRequestedEvent" ->
          this.eventHandler.onAccessRequestedEvent(convertPayload(event, AccessRequestedEvent.class));
      case "com.entropy-data.events.AccessApprovedEvent" ->
          this.eventHandler.onAccessApprovedEvent(convertPayload(event, AccessApprovedEvent.class));
      case "com.entropy-data.events.AccessRejectedEvent" ->
          this.eventHandler.onAccessRejectedEvent(convertPayload(event, AccessRejectedEvent.class));
      case "com.entropy-data.events.AccessActivatedEvent" ->
          this.eventHandler.onAccessActivatedEvent(convertPayload(event, AccessActivatedEvent.class));
      case "com.entropy-data.events.AccessDeactivatedEvent" ->
          this.eventHandler.onAccessDeactivatedEvent(convertPayload(event, AccessDeactivatedEvent.class));
      case "com.entropy-data.events.SourceSystemCreatedEvent" ->
          this.eventHandler.onSourceSystemCreatedEvent(convertPayload(event, SourceSystemCreatedEvent.class));
      case "com.entropy-data.events.SourceSystemUpdatedEvent" ->
          this.eventHandler.onSourceSystemUpdatedEvent(convertPayload(event, SourceSystemUpdatedEvent.class));
      case "com.entropy-data.events.SourceSystemDeletedEvent" ->
          this.eventHandler.onSourceSystemDeletedEvent(convertPayload(event, SourceSystemDeletedEvent.class));
      case "com.entropy-data.events.TeamCreatedEvent" ->
          this.eventHandler.onTeamCreatedEvent(convertPayload(event, TeamCreatedEvent.class));
      case "com.entropy-data.events.TeamUpdatedEvent" ->
          this.eventHandler.onTeamUpdatedEvent(convertPayload(event, TeamUpdatedEvent.class));
      case "com.entropy-data.events.TeamDeletedEvent" ->
          this.eventHandler.onTeamDeletedEvent(convertPayload(event, TeamDeletedEvent.class));
      case "com.entropy-data.events.DefinitionCreatedEvent" ->
          this.eventHandler.onDefinitionCreatedEvent(convertPayload(event, DefinitionCreatedEvent.class));
      case "com.entropy-data.events.DefinitionUpdatedEvent" ->
          this.eventHandler.onDefinitionUpdatedEvent(convertPayload(event, DefinitionUpdatedEvent.class));
      case "com.entropy-data.events.DefinitionDeletedEvent" ->
          this.eventHandler.onDefinitionDeletedEvent(convertPayload(event, DefinitionDeletedEvent.class));
      case "com.entropy-data.events.TagCreatedEvent" ->
          this.eventHandler.onTagCreatedEvent(convertPayload(event, TagCreatedEvent.class));
      case "com.entropy-data.events.TagUpdatedEvent" ->
          this.eventHandler.onTagUpdatedEvent(convertPayload(event, TagUpdatedEvent.class));
      case "com.entropy-data.events.TagDeletedEvent" ->
          this.eventHandler.onTagDeletedEvent(convertPayload(event, TagDeletedEvent.class));
      case "com.entropy-data.events.AssetCreatedEvent" ->
          this.eventHandler.onAssetCreatedEvent(convertPayload(event, AssetCreatedEvent.class));
      case "com.entropy-data.events.AssetUpdatedEvent" ->
          this.eventHandler.onAssetUpdatedEvent(convertPayload(event, AssetUpdatedEvent.class));
      case "com.entropy-data.events.AssetDeletedEvent" ->
          this.eventHandler.onAssetDeletedEvent(convertPayload(event, AssetDeletedEvent.class));
      case "com.entropy-data.events.TestResultsCreatedEvent" -> this.eventHandler.onTestResultsCreatedEvent(
          convertPayload(event, entropydata.sdk.client.model.TestResultsCreatedEvent.class));
      case "com.entropy-data.events.TestResultsDeletedEvent" -> this.eventHandler.onTestResultsDeletedEvent(
          convertPayload(event, entropydata.sdk.client.model.TestResultsDeletedEvent.class));
      case "com.entropy-data.events.DataUsageAgreementCreatedEvent" -> log.debug("Ignore deprecated event");
      case "com.entropy-data.events.DataUsageAgreementUpdatedEvent" -> log.debug("Ignore deprecated event");
      case "com.entropy-data.events.DataUsageAgreementDeletedEvent" -> log.debug("Ignore deprecated event");
      case "com.entropy-data.events.DataUsageAgreementRequestedEvent" -> log.debug("Ignore deprecated event");
      case "com.entropy-data.events.DataUsageAgreementApprovedEvent" -> log.debug("Ignore deprecated event");
      case "com.entropy-data.events.DataUsageAgreementRejectedEvent" -> log.debug("Ignore deprecated event");
      case "com.entropy-data.events.DataUsageAgreementActivatedEvent" -> log.debug("Ignore deprecated event");
      case "com.entropy-data.events.DataUsageAgreementDeactivatedEvent" -> log.debug("Ignore deprecated event");
      default -> log.warn("Unknown event type: {}", event.getType());

    }

  }

  private <T> T convertPayload(entropydata.sdk.client.model.CloudEvent event, Class<T> payloadType) {
    var data = event.getData();
    return objectMapper.convertValue(data, payloadType);
  }


}
