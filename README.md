Data Mesh Manager SDK
======================

The Data Mesh Manager SDK is a Java library that provides a set of APIs to interact with  [Data Mesh Manager](https://datamesh-manager.com) and [Data Contract Manager](https://datacontract-manager.com).

Using the SDK, you can build Java applications to automate data platform operations, such as:

- Synchronize data products and data assets from the data platform to the Data Mesh Manager
- Automate permissions in the data platform when an access request has been approved
- Notify downstream consumers when data contract tests have failed
- Publish data product costs and usage data to Data Mesh Manager

Concept
-------

This SDK is designed as a foundation for building data platform integrations that run as agents on customer's data platform, e.g., as containers running in a Kubernetes cluster. 

It interacts with the Data Mesh Manager APIs to send metadata and to subscribe to events to trigger actions in the data platform or with other services.

Getting Started
---

### Requirements

- Java 17 or later
- Spring Boot 3.3 or later (recommended)

### Spring Boot

We recommend to use Spring Boot to implement your integration application, as Spring Boot provides production-ready features, such as configuration management, logging, and observability.

Use [Spring Initializr](https://start.spring.io/) to create a new Spring Boot project, add these dependencies

- Spring Web
- Spring Boot Actuator

and add the following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>com.datamesh-manager</groupId>
  <artifactId>datamesh-manager-sdk</artifactId>
  <version>LATEST</version>
</dependency>
```

Replace the `LATEST` with the latest version of the SDK.
