Entropy Data SDK
======================

The Entropy Data SDK is a Java library that provides a set of APIs to interact with  [Entropy Data](https://entropy-data.com).

Using the SDK, you can build Java applications to automate data platform operations, such as:

- Synchronize data products and data assets from the data platform to Entropy Data
- Synchronize datacontract.yaml in Git repositories with Entropy Data
- Automate permissions in the data platform when an access request has been approved
- Notify downstream consumers when data contract tests have failed
- Publish data product costs and usage data to Entropy Data

This SDK is designed as a foundation for building data platform integrations that run as long-running connectors on customer's data platform, e.g., as containers running in a Kubernetes cluster or any other container-runtime. 

It interacts with the Entropy Data APIs to send metadata and to subscribe to events to trigger actions in the data platform or with other services.


Existing Connectors
---

We provide some connectors for commonly used platforms that use this SDK and that can be used out-of-the-box or as a template for custom integrations:

| Platform              | Connector                                                                                                  | Synchronize Assets | Access Management | Remarks                     |
|-----------------------|------------------------------------------------------------------------------------------------------------|--------------------|-------------------|-----------------------------|
| Databricks            | [datamesh-manager-connector-databricks](https://github.com/datamesh-manager/datamesh-manager-connector-databricks) | ✅                  | ✅                 | Uses Unity Catalog APIs     |
| Snowflake             | [datamesh-manager-connector-snowflake](https://github.com/datamesh-manager/datamesh-manager-connector-snowflake)   | ✅                  | ✅                 | Uses the Snowflake REST API | 
| Google Cloud Platform | [datamesh-manager-connector-gcp](https://github.com/datamesh-manager/datamesh-manager-connector-gcp)               | ✅                  | ✅                 | Uses BigQuery APIs          |
| Hive                 | [datamesh-manager-connector-hive](https://github.com/datamesh-manager/datamesh-manager-connector-hive)                                                                                                           |   ✅                  |                   | Uses JDBC (supports Hive, Impala, or Cloudera)                 |
| MariaDB                   | [datamesh-manager-connector-mariadb](https://github.com/datamesh-manager/datamesh-manager-connector-mariadb)                                                                                                           | ✅                   |                   | Uses JDBC                 |
| AWS                   |                                                                                                            |                    |                   | Coming soon                 |
| Azure                 |                                                                                                            |                    |                   | Coming soon                 |
| datahub               |                                                                                                            |                    |                   | Coming soon                 |
| Collibra              |                                                                                                            |                    |                   | Coming soon                 |
| Iceberg Catalog              |                                                                                                            |                    |                   | Coming soon                 |


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
  <groupId>com.entropy-data</groupId>
  <artifactId>entropy-data-sdk</artifactId>
  <version>RELEASE</version>
</dependency>
```

Replace the `RELEASE` with the latest version of the SDK.

### Instantiate an EntropyDataClient

To work with the API, you need an [API key](https://docs.entropy-data.com/quickstart).
Then you can instantiate an `EntropyDataClient`:

```java
var client = new EntropyDataClient(
    "https://api.entropy-data.com",
    "ed_live_..."
);
```

This client has all methods to interact with the [Entropy Data API](https://api.entropy-data.com/swagger/index.html).

### Implement an AssetsProvider (optional)

To synchronize assets (such as tables, views, files, topics, ...) from your data platform with Entropy Data, implement the `EntropyDataAssetsProvider` interface:

```java

public class MyAssetsProvider implements EntropyDataAssetsProvider {
  @Override
  public void fetchAssets(AssetCallback assetCallback) {
    // query your data platform for assets
    // convert them to entropydata.sdk.client.model.Asset objects
    // and call assetCallback.onAssetUpdated(asset) for each new or updated asset
  }
}
```

With this implementation, you can start an `EntropyDataAssetsSynchronizer`:

```java
var connectorid = "my-unique-assets-synchronization-connector-id";
var assetsProvider = new MyAssetsProvider();
var assetsSynchronizer = new EntropyDataAssetsSynchronizer(connectorid, client, assetsSupplier);
assetsSynchronizer.start(); // This will start a long-running connector that calls the fetchAssets method periodically
```

### Implement an EventListener (optional)

To trigger actions in your data platform when events happen in Entropy Data, you can implement the `EntropyDataEventListener` interface:

```java
public class MyEventHandler implements EntropyDataEventHandler {

  @Override
  public void onAccessActivatedEvent(AccessActivatedEvent event) {
    // TODO grant permissions in your data platform
    // use the EntropyDataClient to retrieve the current access resource and data product and consumer resource for details
  }

  @Override
  public void onAccessDeactivatedEvent(AccessDeactivatedEvent event) {
    // TODO revoke permissions in your data platform
  }
}
```

You can listen to any event from Entropy Data. The SDK provides a method for each event type.

With this implementation, you can start an `EntropyDataEventListener`:

```java
var connectorid = "my-unique-event-listener-connector-id";
var eventHandler = new MyEventHandler();
var stateRepository = ... // see below
var eventListener = new EntropyDataEventListener(connectorid, client, eventHandler, stateRepository);
eventListener.start(); // This will start a long-running connector that listens to events from Entropy Data
```

If you have multiple connectors in an application, make sure to start the `start()` methods in separate threads.

### State Repository

The `EntropyDataEventListener` requires an `EntropyDataStateRepository` to store the `lastEventId` that has been processed.
Also, you can use the state repository in other connectors, if you need to store information what has been processed or what is the current state of your connector.
You can implement this interface to store the state in a database, a file, or any other storage:

```java
public interface EntropyDataStateRepository {
  Map<String, Object> getState();
  void saveState(Map<String, Object> state);
}
```

For your convenience, you can use the `EntropyDataStateRepositoryRemote` to store the state directly in Entropy Data:

```java
var connectorId = "my-unique-event-listener-connector-id";
var stateRepository = new EntropyDataStateRepositoryRemote(connectorId, client);
```

and for testing there is also an `EntropyDataStateRepositoryInMemory`.



Contributing
---
Contributions are welcome! Please open an issue or a pull request.

License
---
[MIT License](LICENSE)
