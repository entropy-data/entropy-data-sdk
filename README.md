Data Mesh Manager SDK
======================

The Data Mesh Manager SDK is a Java library that provides a set of APIs to interact with  [Data Mesh Manager](https://datamesh-manager.com) and [Data Contract Manager](https://datacontract-manager.com).

Using the SDK, you can build Java applications to automate data platform operations, such as:

- Synchronize data products and data assets from the data platform to the Data Mesh Manager
- Synchronize datacontract.yaml in Git repositories with Data Contract Manager
- Automate permissions in the data platform when an access request has been approved
- Notify downstream consumers when data contract tests have failed
- Publish data product costs and usage data to Data Mesh Manager

This SDK is designed as a foundation for building data platform integrations that run as long-running connectors on customer's data platform, e.g., as containers running in a Kubernetes cluster or any other container-runtime. 

It interacts with the Data Mesh Manager APIs to send metadata and to subscribe to events to trigger actions in the data platform or with other services.


Existing Connectors
---

We provide some connectors for commonly-used platforms that that use this SDK and that can be used out-of-the-box or as a template for custom integrations:

| Platform              | Connector                                                                                                  | Synchronize Assets | Access Management | Remarks                     |
|-----------------------|------------------------------------------------------------------------------------------------------------|--------------------|-------------------|-----------------------------|
| Databricks            | [datamesh-manager-connector-databricks](https://github.com/datamesh-manager/datamesh-manager-connector-databricks) | ✅                  | ✅                 | Uses Unity Catalog APIs     |
| Snowflake             | [datamesh-manager-connector-snowflake](https://github.com/datamesh-manager/datamesh-manager-connector-snowflake)   | ✅                  | ✅                 | Uses the Snowflake REST API | 
| AWS                   |                                                                                                            |                    |                   | Coming soon                 |
| Google Cloud Platform | [datamesh-manager-connector-gcp](https://github.com/datamesh-manager/datamesh-manager-connector-gcp)               | ✅                  | ✅                 | Uses BigQuery APIs          |
| Azure                 |                                                                                                            |                    |                   | Coming soon                 |
| datahub               |                                                                                                            |                    |                   | Coming soon                 |
| Collibra              |                                                                                                            |                    |                   | Coming soon                 |

If you are interested in further integration, please [contact us](https://entropy-data.atlassian.net/servicedesk/customer/portals).


Getting Started
---

Follow this guide to build your own custom integration.

### Requirements

- Java 17 or later

### Dependency

Add this dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>com.datamesh-manager</groupId>
  <artifactId>datamesh-manager-sdk</artifactId>
  <version>RELEASE</version>
</dependency>
```

Replace the `RELEASE` with the latest version of the SDK.

### Instantiate a DataMeshManagerClient

To work with the API, you need an [API key](https://docs.datamesh-manager.com/quickstart).
Then you can instantiate a `DataMeshManagerClient`:

```java
var client = new DataMeshManagerClient(
    "https://api.datamesh-manager.com",
    "dmm_live_..."
);
```

This client has all methods to interact with the [Data Mesh Manager API](https://api.datamesh-manager.com/swagger/index.html).

### Implement an AssetsProvider (optional)

To synchronize assets (such as tables, views, files, topics, ...) from your data platform with Data Mesh Manager, implement the `DataMeshManagerAssetsProvider` interface:

```java

public class MyAssetsProvider implements DataMeshManagerAssetsProvider {
  @Override
  public void fetchAssets(AssetCallback assetCallback) {
    // query your data platform for assets
    // convert them to datameshmanager.sdk.client.model.Asset objects
    // and call assetCallback.onAssetUpdated(asset) for each new or updated asset
  }
}
```

With this implementation, you can start an `DataMeshManagerAssetsSynchronizer`:

```java
var connectorid = "my-unique-assets-synchronization-connector-id";
var assetsProvider = new MyAssetsProvider();
var assetsSynchronizer = new DataMeshManagerAssetsSynchronizer(connectorid, client, assetsSupplier);
assetsSynchronizer.start(); // This will start a long-running connector that calls the fetchAssets method periodically
```

### Implement an EventListener (optional)

To trigger actions in your data platform when events happen in Data Mesh Manager, you can implement the `DataMeshManagerEventListener` interface:

```java
public class MyEventHandler implements DataMeshManagerEventHandler {

  @Override
  public void onAccessActivatedEvent(AccessActivatedEvent event) {
    // TODO grant permissions in your data platform
    // use the DataMeshManagerClient to retrieve the current access resource and data product and consumer resource for details
  }

  @Override
  public void onAccessDeactivatedEvent(AccessDeactivatedEvent event) {
    // TODO revoke permissions in your data platform
  }
}
```

You can listen to any event from Data Mesh Manager. The SDK provides a method for each event type.

With this implementation, you can start an `DataMeshManagerEventListener`:

```java
var connectorid = "my-unique-event-listener-connector-id";
var eventHandler = new MyEventHandler();
var stateRepository = ... // see below
var eventListener = new DataMeshManagerEventListener(connectorid, client, eventHandler, stateRepository);
eventListener.start(); // This will start a long-running connector that listens to events from Data Mesh Manager
```

If you have multiple connectors in an application, make sure to start the `start()` methods in separate threads.

### State Repository

The `DataMeshManagerEventListener` requires a `DataMeshManagerStateRepository` to store the `lastEventId` that has been processed.
Also, you can use the state repository in other connectors, if you need to store information what has been processed or what is the current state of your connector.
You can implement this interface to store the state in a database, a file, or any other storage:

```java
public interface DataMeshManagerStateRepository {
  Map<String, Object> getState();
  void saveState(Map<String, Object> state);
}
```

For your convenience, you can use the `DataMeshManagerStateRepositoryRemote` to store the state directly in the Data Mesh Manager:

```java
var connectorId = "my-unique-event-listener-connector-id";
var stateRepository = new DataMeshManagerStateRepositoryRemote(connectorId, client);
```

and for testing there is also a `DataMeshManagerStateRepositoryInMemory`.



Contributing
---
Contributions are welcome! Please open an issue or a pull request.

License
---
[MIT License](LICENSE)
