package entropydata.sdk;

import com.fasterxml.jackson.databind.node.ObjectNode;
import entropydata.sdk.client.ApiException;
import entropydata.sdk.client.api.AssetsApi;
import entropydata.sdk.client.model.Asset;
import entropydata.sdk.client.model.AssetInfo;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes an asset of a data platform to Entropy Data.
 *
 * <p>The asset usually exists under the id the provider derives from the platform. If it does not, it may still exist under
 * another id: one taken over in an earlier run, found by its {@code info.sourceId}, or a placeholder someone created before
 * the platform object existed, whose id the provider cannot know. A placeholder has the same {@code info.source} and
 * {@code info.qualifiedName} and no {@code info.sourceId} yet. Such an asset is updated instead of creating a duplicate.
 *
 * <p>Owner, tags, and links are curated in Entropy Data. A provider that leaves tags or links empty keeps the ones there (the
 * generated model starts them empty, so empty and left out look the same), and an owner set in Entropy Data wins over the
 * provider's, so the provider's owner only fills an empty one.
 */
class EntropyDataAssetWriter {

  private static final Logger log = LoggerFactory.getLogger(EntropyDataAssetWriter.class);

  private final AssetsApi assetsApi;

  /**
   * Entropy Data ids by provider id, for assets taken over under another id and for relationship targets already
   * resolved, so a parent shared by many children is looked up once.
   */
  private final Map<String, String> entropyDataIds = new ConcurrentHashMap<>();

  /**
   * An Entropy Data version without the lookup filters ignores them and answers with the first page of all assets. An answer
   * holding an asset that does not match what was asked for gives that away, so lookups stop for the rest of the process
   * instead of fetching up to a page of assets for every new one. An empty organization cannot give it away, so no probe is
   * made up front.
   */
  private volatile boolean lookupSupported = true;

  EntropyDataAssetWriter(AssetsApi assetsApi) {
    this.assetsApi = assetsApi;
  }

  void save(Asset asset) {
    var providerId = asset.getId();
    var existing = findExisting(asset).orElse(null);

    if (existing != null && !Objects.equals(providerId, existing.getId())) {
      log.info("Asset {} exists as asset {}, so update that one", providerId, existing.getId());
      asset.setId(existing.getId());
      var info = infoOf(asset);
      if (isBlank(info.getSourceId())) {
        info.setSourceId(providerId);
      }
      entropyDataIds.put(providerId, existing.getId());
    }

    resolveRelationships(asset);

    if (existing != null) {
      keepCuratedFields(asset, existing);
      if (isUnchanged(asset, existing)) {
        log.info("Asset {} already exists and unchanged", asset.getId());
        return;
      }
    }

    log.info("Saving asset {}", asset.getId());
    assetsApi.addAsset(asset.getId(), asset);
  }

  void delete(String providerId) {
    var entropyDataId = entropyDataIds.get(providerId);
    if (entropyDataId == null) {
      // A deletion carries the provider's id only, so an asset taken over before a restart is found by its source id alone.
      var matches = lookUp(null, null, providerId);
      entropyDataId = matches.size() == 1 ? matches.get(0).getId() : providerId;
    }
    log.info("Deleting asset {}", entropyDataId);
    assetsApi.deleteAsset(entropyDataId);
    entropyDataIds.remove(providerId);
  }

  private String entropyDataIdOf(String providerId) {
    return entropyDataIds.getOrDefault(providerId, providerId);
  }

  private Optional<Asset> findExisting(Asset asset) {
    var byId = findById(entropyDataIdOf(asset.getId()));
    if (byId.isPresent()) {
      return byId;
    }
    entropyDataIds.remove(asset.getId());

    var info = asset.getInfo();
    if (info == null || isBlank(info.getSource())) {
      return Optional.empty();
    }

    // Taking an asset over stores the provider's id as its source id when the provider sends none.
    var sourceId = isBlank(info.getSourceId()) ? asset.getId() : info.getSourceId();
    var matches = findBySourceId(info.getSource(), sourceId);
    if (matches.size() > 1) {
      log.warn("Asset {}: {} assets have source {} and source id {}, so none of them is updated", asset.getId(), matches.size(),
          info.getSource(), sourceId);
      return Optional.empty();
    }
    if (matches.size() == 1) {
      return findById(matches.get(0).getId());
    }

    if (!isBlank(info.getQualifiedName())) {
      var placeholders = findPlaceholders(info.getSource(), info.getQualifiedName());
      if (placeholders.size() > 1) {
        log.warn("Asset {}: {} assets without source id have source {} and qualified name {}, so none of them is updated",
            asset.getId(), placeholders.size(), info.getSource(), info.getQualifiedName());
        return Optional.empty();
      }
      if (placeholders.size() == 1) {
        return findById(placeholders.get(0).getId());
      }
    }

    return Optional.empty();
  }

  private void resolveRelationships(Asset asset) {
    if (asset.getRelationships() == null) {
      return;
    }
    var source = asset.getInfo() == null ? null : asset.getInfo().getSource();
    for (var relationship : asset.getRelationships()) {
      if (relationship.getAssetId() != null) {
        relationship.setAssetId(resolveRelatedAssetId(relationship.getAssetId(), source));
      }
    }
  }

  private String resolveRelatedAssetId(String providerId, String source) {
    var known = entropyDataIds.get(providerId);
    if (known != null) {
      return known;
    }
    if (findById(providerId).isPresent()) {
      entropyDataIds.put(providerId, providerId);
      return providerId;
    }
    if (!isBlank(source)) {
      var matches = findBySourceId(source, providerId);
      if (matches.size() == 1) {
        entropyDataIds.put(providerId, matches.get(0).getId());
        return matches.get(0).getId();
      }
    }
    return providerId;
  }

  private static void keepCuratedFields(Asset asset, Asset existing) {
    var existingOwner = existing.getInfo() == null ? null : existing.getInfo().getOwner();
    if (!isBlank(existingOwner)) {
      infoOf(asset).setOwner(existingOwner);
    }
    if (asset.getTags() == null || asset.getTags().isEmpty()) {
      asset.setTags(existing.getTags());
    }
    if (asset.getLinks() == null || asset.getLinks().isEmpty()) {
      asset.setLinks(existing.getLinks());
    }
  }

  /**
   * Assigned tags are read-only in the API, so a provider cannot change them. The assets are compared as JSON because the
   * generated model may have no setter for a read-only property.
   */
  private boolean isUnchanged(Asset asset, Asset existing) {
    var objectMapper = assetsApi.getApiClient().getObjectMapper();
    ObjectNode provided = objectMapper.valueToTree(asset);
    ObjectNode current = objectMapper.valueToTree(existing);
    provided.remove(Asset.JSON_PROPERTY_ASSIGNED_TAGS);
    current.remove(Asset.JSON_PROPERTY_ASSIGNED_TAGS);
    return provided.equals(current);
  }

  private Optional<Asset> findById(String id) {
    try {
      return Optional.ofNullable(assetsApi.getAsset(id));
    } catch (ApiException e) {
      if (e.getCode() == 404) {
        return Optional.empty();
      }
      throw e;
    }
  }

  private List<Asset> findBySourceId(String source, String sourceId) {
    return lookUp(source, null, sourceId);
  }

  private List<Asset> findPlaceholders(String source, String qualifiedName) {
    return lookUp(source, qualifiedName, null).stream()
        .filter(candidate -> isBlank(candidate.getInfo().getSourceId()))
        .toList();
  }

  private List<Asset> lookUp(String source, String qualifiedName, String sourceId) {
    if (!lookupSupported) {
      return List.of();
    }
    var answer = Optional.ofNullable(assetsApi.getAssets(null, source, qualifiedName, sourceId)).orElse(List.of());
    var matching = answer.stream()
        .filter(candidate -> candidate.getInfo() != null)
        .filter(candidate -> source == null || source.equals(candidate.getInfo().getSource()))
        .filter(candidate -> qualifiedName == null || qualifiedName.equalsIgnoreCase(candidate.getInfo().getQualifiedName()))
        .filter(candidate -> sourceId == null || sourceId.equals(candidate.getInfo().getSourceId()))
        .toList();
    if (matching.size() < answer.size()) {
      lookupSupported = false;
      log.warn("Entropy Data ignores the asset lookup filters, so assets created before their platform object existed "
          + "are not matched and get a duplicate. Update Entropy Data to match them.");
      // A page of all assets is no complete answer, so a single match on it could hide a second one elsewhere.
      return List.of();
    }
    return matching;
  }

  private static AssetInfo infoOf(Asset asset) {
    if (asset.getInfo() == null) {
      asset.setInfo(new AssetInfo());
    }
    return asset.getInfo();
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
