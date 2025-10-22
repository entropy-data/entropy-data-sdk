package entropydata.sdk;

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
import entropydata.sdk.client.model.TestResultsCreatedEvent;
import entropydata.sdk.client.model.TestResultsDeletedEvent;


public interface EntropyDataEventHandler {

  /**
   * This callback function is called for every event.
   */
  default void onEvent(CloudEvent cloudEvent) {
  }

  default void onDataProductCreatedEvent(DataProductCreatedEvent event) {
  }

  default void onDataProductUpdatedEvent(DataProductUpdatedEvent event) {
  }

  default void onDataProductDeletedEvent(DataProductDeletedEvent event) {
  }

  default void onOutputPortCreatedEvent(OutputPortCreatedEvent event) {
  }

  default void onOutputPortUpdatedEvent(OutputPortUpdatedEvent event) {
  }

  default void onOutputPortDeletedEvent(OutputPortDeletedEvent event) {
  }

  default void onDataContractCreatedEvent(DataContractCreatedEvent event) {
  }

  default void onDataContractUpdatedEvent(DataContractUpdatedEvent event) {
  }

  default void onDataContractDeletedEvent(DataContractDeletedEvent event) {
  }

  default void onAccessCreatedEvent(AccessCreatedEvent event) {
  }

  default void onAccessUpdatedEvent(AccessUpdatedEvent event) {
  }

  default void onAccessDeletedEvent(AccessDeletedEvent event) {
  }

  default void onAccessRequestedEvent(AccessRequestedEvent event) {
  }

  default void onAccessApprovedEvent(AccessApprovedEvent event) {
  }

  default void onAccessRejectedEvent(AccessRejectedEvent event) {
  }

  default void onAccessActivatedEvent(AccessActivatedEvent event) {
  }

  default void onAccessDeactivatedEvent(AccessDeactivatedEvent event) {
  }

  default void onSourceSystemCreatedEvent(SourceSystemCreatedEvent event) {
  }

  default void onSourceSystemUpdatedEvent(SourceSystemUpdatedEvent event) {
  }

  default void onSourceSystemDeletedEvent(SourceSystemDeletedEvent event) {
  }

  default void onTeamCreatedEvent(TeamCreatedEvent event) {
  }

  default void onTeamUpdatedEvent(TeamUpdatedEvent event) {
  }

  default void onTeamDeletedEvent(TeamDeletedEvent event) {
  }

  default void onDefinitionCreatedEvent(DefinitionCreatedEvent event) {
  }

  default void onDefinitionUpdatedEvent(DefinitionUpdatedEvent event) {
  }

  default void onDefinitionDeletedEvent(DefinitionDeletedEvent event) {
  }

  default void onTagCreatedEvent(TagCreatedEvent event) {
  }

  default void onTagUpdatedEvent(TagUpdatedEvent event) {
  }

  default void onTagDeletedEvent(TagDeletedEvent event) {
  }

  default void onAssetCreatedEvent(AssetCreatedEvent event) {
  }

  default void onAssetUpdatedEvent(AssetUpdatedEvent event) {
  }

  default void onAssetDeletedEvent(AssetDeletedEvent event) {
  }

  default void onTestResultsCreatedEvent(TestResultsCreatedEvent event) {
  }

  default void onTestResultsDeletedEvent(TestResultsDeletedEvent event) {
  }

}
