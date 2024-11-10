package datameshmanager.sdk;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datameshmanager.client")
public record DataMeshManagerClientProperties(
    String id,
    String host,
    String apikey) {

}
