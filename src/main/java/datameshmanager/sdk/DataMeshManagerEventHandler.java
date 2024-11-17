package datameshmanager.sdk;

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
import datameshmanager.sdk.client.model.DataContractCreatedEvent;
import datameshmanager.sdk.client.model.DataContractDeletedEvent;
import datameshmanager.sdk.client.model.DataContractUpdatedEvent;
import datameshmanager.sdk.client.model.DataProductCreatedEvent;
import datameshmanager.sdk.client.model.DataProductDeletedEvent;
import datameshmanager.sdk.client.model.DataProductUpdatedEvent;
import datameshmanager.sdk.client.model.DataUsageAgreementActivatedEvent;
import datameshmanager.sdk.client.model.DataUsageAgreementApprovedEvent;
import datameshmanager.sdk.client.model.DataUsageAgreementCreatedEvent;
import datameshmanager.sdk.client.model.DataUsageAgreementDeactivatedEvent;
import datameshmanager.sdk.client.model.DataUsageAgreementDeletedEvent;
import datameshmanager.sdk.client.model.DataUsageAgreementRejectedEvent;
import datameshmanager.sdk.client.model.DataUsageAgreementRequestedEvent;
import datameshmanager.sdk.client.model.DataUsageAgreementUpdatedEvent;
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
import datameshmanager.sdk.client.model.TestResultsCreatedEvent;
import datameshmanager.sdk.client.model.TestResultsDeletedEvent;


public interface DataMeshManagerEventHandler {

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

  default void onDataUsageAgreementCreatedEvent(DataUsageAgreementCreatedEvent event) {
  }

  default void onDataUsageAgreementUpdatedEvent(DataUsageAgreementUpdatedEvent event) {
  }

  default void onDataUsageAgreementDeletedEvent(DataUsageAgreementDeletedEvent event) {
  }

  default void onDataUsageAgreementRequestedEvent(DataUsageAgreementRequestedEvent event) {
  }

  default void onDataUsageAgreementApprovedEvent(DataUsageAgreementApprovedEvent event) {
  }

  default void onDataUsageAgreementRejectedEvent(DataUsageAgreementRejectedEvent event) {
  }

  default void onDataUsageAgreementActivatedEvent(DataUsageAgreementActivatedEvent event) {
  }

  default void onDataUsageAgreementDeactivatedEvent(DataUsageAgreementDeactivatedEvent event) {
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
