package datameshmanager.sdk;

import datameshmanager.sdk.client.ApiClient;
import datameshmanager.sdk.client.api.AccessApi;
import datameshmanager.sdk.client.api.AssetsApi;
import datameshmanager.sdk.client.api.ConnectorsApi;
import datameshmanager.sdk.client.api.CostsApi;
import datameshmanager.sdk.client.api.DataContractsApi;
import datameshmanager.sdk.client.api.DataProductsApi;
import datameshmanager.sdk.client.api.DefinitionsApi;
import datameshmanager.sdk.client.api.EventsApi;
import datameshmanager.sdk.client.api.SourceSystemsApi;
import datameshmanager.sdk.client.api.TagsApi;
import datameshmanager.sdk.client.api.TeamsApi;
import datameshmanager.sdk.client.api.TestResultsApi;

public class DataMeshManagerClient {

  private final ApiClient apiClient;

  private final AccessApi accessApi;
  private final AssetsApi assetsApi;
  private final CostsApi costsApi;
  private final DataContractsApi dataContractsApi;
  private final DataProductsApi dataProductsApi;
  private final DefinitionsApi definitionsApi;
  private final EventsApi eventsApi;
  private final SourceSystemsApi sourceSystemsApi;
  private final TagsApi tagsApi;
  private final TeamsApi teamsApi;
  private final TestResultsApi testResultsApi;
  private final ConnectorsApi connectorsApi;

  public DataMeshManagerClient(String host, String apiKey) {
    var apiClient = new ApiClient();
    apiClient.setBasePath(host);
    apiClient.setApiKey(apiKey);
    this.apiClient = apiClient;

    this.accessApi = new AccessApi(apiClient);
    this.assetsApi = new AssetsApi(apiClient);
    this.costsApi = new CostsApi(apiClient);
    this.dataContractsApi = new DataContractsApi(apiClient);
    this.dataProductsApi = new DataProductsApi(apiClient);
    this.definitionsApi = new DefinitionsApi(apiClient);
    this.eventsApi = new EventsApi(apiClient);
    this.sourceSystemsApi = new SourceSystemsApi(apiClient);
    this.tagsApi = new TagsApi(apiClient);
    this.teamsApi = new TeamsApi(apiClient);
    this.testResultsApi = new TestResultsApi(apiClient);
    this.connectorsApi = new ConnectorsApi(apiClient);
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public AccessApi getAccessApi() {
    return accessApi;
  }

  public AssetsApi getAssetsApi() {
    return assetsApi;
  }

  public CostsApi getCostsApi() {
    return costsApi;
  }

  public DataContractsApi getDataContractsApi() {
    return dataContractsApi;
  }

  public DataProductsApi getDataProductsApi() {
    return dataProductsApi;
  }

  public DefinitionsApi getDefinitionsApi() {
    return definitionsApi;
  }

  public EventsApi getEventsApi() {
    return eventsApi;
  }

  public SourceSystemsApi getSourceSystemsApi() {
    return sourceSystemsApi;
  }

  public TagsApi getTagsApi() {
    return tagsApi;
  }

  public TeamsApi getTeamsApi() {
    return teamsApi;
  }

  public TestResultsApi getTestResultsApi() {
    return testResultsApi;
  }

  public ConnectorsApi getConnectorsApi() {
    return connectorsApi;
  }
}
