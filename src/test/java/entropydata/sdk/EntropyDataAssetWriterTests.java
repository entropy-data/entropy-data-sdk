package entropydata.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import entropydata.sdk.client.ApiClient;
import entropydata.sdk.client.ApiException;
import entropydata.sdk.client.api.AssetsApi;
import entropydata.sdk.client.model.Asset;
import entropydata.sdk.client.model.AssetInfo;
import entropydata.sdk.client.model.AssetRelationshipsInner;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EntropyDataAssetWriterTests {

  private final InMemoryAssetsApi entropyData = new InMemoryAssetsApi();

  @Test
  void updatesAssetCreatedBeforeThePlatformObjectInsteadOfAddingADuplicate() {
    entropyData.store(new Asset().id("orders-placeholder")
        .info(new AssetInfo().source("unity").qualifiedName("Sales.Orders.Order_Lines").owner("team-sales"))
        .tags(List.of("pii")));

    new EntropyDataAssetWriter(entropyData).save(table("table-uuid", "sales.orders.order_lines"));

    assertThat(entropyData.puts).containsExactly("orders-placeholder");
    assertThat(entropyData.assets).doesNotContainKey("table-uuid");
    var saved = entropyData.assets.get("orders-placeholder");
    assertThat(saved.getInfo().getSourceId()).isEqualTo("table-uuid");
    assertThat(saved.getInfo().getDescription()).isEqualTo("from Databricks");
    assertThat(saved.getInfo().getOwner()).isEqualTo("team-sales");
    assertThat(saved.getTags()).containsExactly("pii");
  }

  @Test
  void findsTheTakenOverAssetBySourceIdAfterARestartAndARename() {
    entropyData.store(new Asset().id("orders-placeholder").info(new AssetInfo().source("unity").qualifiedName("sales.orders.order_lines")));
    new EntropyDataAssetWriter(entropyData).save(table("table-uuid", "sales.orders.order_lines"));

    new EntropyDataAssetWriter(entropyData).save(table("table-uuid", "sales.orders.lines"));

    assertThat(entropyData.puts).containsExactly("orders-placeholder", "orders-placeholder");
    assertThat(entropyData.assets.get("orders-placeholder").getInfo().getQualifiedName()).isEqualTo("sales.orders.lines");
  }

  @Test
  void leavesAnAssetOfAnotherPlatformObjectWithTheSameNameAlone() {
    entropyData.store(table("old-table-uuid", "sales.orders.order_lines"));

    new EntropyDataAssetWriter(entropyData).save(table("new-table-uuid", "sales.orders.order_lines"));

    assertThat(entropyData.puts).containsExactly("new-table-uuid");
    assertThat(entropyData.assets).containsKeys("old-table-uuid", "new-table-uuid");
  }

  @Test
  void addsANewAssetWhenSeveralPlaceholdersMatch() {
    entropyData.store(new Asset().id("first").info(new AssetInfo().source("unity").qualifiedName("sales.orders.order_lines")));
    entropyData.store(new Asset().id("second").info(new AssetInfo().source("unity").qualifiedName("SALES.ORDERS.ORDER_LINES")));

    new EntropyDataAssetWriter(entropyData).save(table("table-uuid", "sales.orders.order_lines"));

    assertThat(entropyData.puts).containsExactly("table-uuid");
  }

  @Test
  void keepsTheOwnerSetInEntropyData() {
    var curated = table("table-uuid", "sales.orders.order_lines");
    curated.getInfo().owner("team-sales");
    entropyData.store(curated);

    var fromPlatform = table("table-uuid", "sales.orders.order_lines");
    fromPlatform.getInfo().owner("team-default").description("changed");
    new EntropyDataAssetWriter(entropyData).save(fromPlatform);

    assertThat(entropyData.assets.get("table-uuid").getInfo().getOwner()).isEqualTo("team-sales");
  }

  @Test
  void fillsAnEmptyOwnerWithTheProvidersOwner() {
    entropyData.store(table("table-uuid", "sales.orders.order_lines"));

    var fromPlatform = table("table-uuid", "sales.orders.order_lines");
    fromPlatform.getInfo().owner("team-default");
    new EntropyDataAssetWriter(entropyData).save(fromPlatform);

    assertThat(entropyData.assets.get("table-uuid").getInfo().getOwner()).isEqualTo("team-default");
  }

  @Test
  void skipsAnAssetThatOnlyDiffersByOwnerAndAssignedTags() {
    var curated = table("table-uuid", "sales.orders.order_lines").assignedTags(List.of("governance/internal"));
    curated.getInfo().owner("team-sales");
    entropyData.store(curated);

    new EntropyDataAssetWriter(entropyData).save(table("table-uuid", "sales.orders.order_lines"));

    assertThat(entropyData.puts).isEmpty();
  }

  @Test
  void pointsChildrenAtTheTakenOverParent() {
    entropyData.store(new Asset().id("orders-schema").info(new AssetInfo().source("unity").qualifiedName("sales.orders")));
    new EntropyDataAssetWriter(entropyData).save(new Asset().id("schema-uuid")
        .info(new AssetInfo().source("unity").sourceId("schema-uuid").qualifiedName("sales.orders")));

    new EntropyDataAssetWriter(entropyData).save(table("table-uuid", "sales.orders.order_lines")
        .relationships(List.of(new AssetRelationshipsInner().relationshipType("parent").assetId("schema-uuid"))));

    assertThat(entropyData.assets.get("table-uuid").getRelationships().get(0).getAssetId()).isEqualTo("orders-schema");
  }

  @Test
  void doesNotMatchWhenEntropyDataIgnoresTheLookupFilters() {
    entropyData.ignoresFilters = true;
    entropyData.store(new Asset().id("orders-placeholder").info(new AssetInfo().source("unity").qualifiedName("sales.orders.order_lines")));

    var writer = new EntropyDataAssetWriter(entropyData);
    writer.save(table("table-uuid", "sales.orders.order_lines"));
    writer.save(table("other-table-uuid", "sales.orders.customers"));

    assertThat(entropyData.puts).containsExactly("table-uuid", "other-table-uuid");
    assertThat(entropyData.listCalls).isEqualTo(1);
  }

  @Test
  void stopsLookingUpOnceEntropyDataTurnsOutToIgnoreTheFiltersInAnOrganizationThatStartedEmpty() {
    entropyData.ignoresFilters = true;

    var writer = new EntropyDataAssetWriter(entropyData);
    writer.save(table("first-table-uuid", "sales.orders.order_lines"));
    writer.save(table("second-table-uuid", "sales.orders.customers"));
    writer.save(table("third-table-uuid", "sales.orders.invoices"));

    assertThat(entropyData.puts).containsExactly("first-table-uuid", "second-table-uuid", "third-table-uuid");
    // two empty answers for the first asset, then the second asset's first lookup returns the first asset unfiltered
    assertThat(entropyData.listCalls).isEqualTo(3);
  }

  private static Asset table(String id, String qualifiedName) {
    return new Asset().id(id)
        .info(new AssetInfo().source("unity").sourceId(id).qualifiedName(qualifiedName).type("unity_table").description("from Databricks"));
  }

  /** Behaves like the asset endpoints of Entropy Data, including the read-only assigned tags. */
  private static class InMemoryAssetsApi extends AssetsApi {

    private final ApiClient apiClient = new ApiClient();
    final Map<String, Asset> assets = new LinkedHashMap<>();
    final List<String> puts = new ArrayList<>();
    boolean ignoresFilters = false;
    int listCalls = 0;

    InMemoryAssetsApi() {
      super(new ApiClient());
    }

    void store(Asset asset) {
      assets.put(asset.getId(), copy(asset));
    }

    @Override
    public Asset getAsset(String id) {
      var asset = assets.get(id);
      if (asset == null) {
        throw new ApiException(404, "Not Found");
      }
      return copy(asset);
    }

    @Override
    public List<Asset> getAssets(Integer p, String source, String qualifiedName, String sourceId) {
      listCalls++;
      return assets.values().stream()
          .filter(asset -> ignoresFilters || source == null || source.equals(asset.getInfo().getSource()))
          .filter(asset -> ignoresFilters || qualifiedName == null || qualifiedName.equalsIgnoreCase(asset.getInfo().getQualifiedName()))
          .filter(asset -> ignoresFilters || sourceId == null || sourceId.equals(asset.getInfo().getSourceId()))
          .map(this::copy)
          .toList();
    }

    @Override
    public void addAsset(String id, Asset asset) {
      puts.add(id);
      var stored = copy(asset);
      var existing = assets.get(id);
      stored.setAssignedTags(existing == null ? null : existing.getAssignedTags());
      assets.put(id, stored);
    }

    private Asset copy(Asset asset) {
      return apiClient.getObjectMapper().convertValue(asset, Asset.class);
    }
  }
}
