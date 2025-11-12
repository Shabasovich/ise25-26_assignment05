package de.seuhd.campuscoffee.data.impl;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.seuhd.campuscoffee.data.client.OsmFeignClient;
import de.seuhd.campuscoffee.data.client.OsmResponse;
import de.seuhd.campuscoffee.domain.exceptions.OsmNodeMissingFieldsException;
import de.seuhd.campuscoffee.domain.exceptions.OsmNodeNotFoundException;
import de.seuhd.campuscoffee.domain.model.OsmAmenity;
import de.seuhd.campuscoffee.domain.model.OsmNode;
import de.seuhd.campuscoffee.domain.ports.OsmDataService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * OSM data service that fetches node data from the OpenStreetMap API.
 */
@Service
@Slf4j
@RequiredArgsConstructor
class OsmDataServiceImpl implements OsmDataService {
    private final OsmFeignClient osmFeignClient;

    @Override
    public @NonNull OsmNode fetchNode(@NonNull Long nodeId) throws OsmNodeNotFoundException {
        try {
            log.debug("Fetching OSM node {}...", nodeId);
            String xmlResponse = osmFeignClient.fetchNode(nodeId);

            if (xmlResponse == null || xmlResponse.isEmpty()) {
                log.error("Empty response from OSM API for node {}", nodeId);
                throw new OsmNodeNotFoundException(nodeId);
            }

            OsmNode node = parseOsmXml(xmlResponse, nodeId);

            log.debug("Successfully fetched and parsed OSM node {}", nodeId);
            return node;

        } catch (FeignException.NotFound e) {
            log.warn("OSM node {} not found", nodeId);
            throw new OsmNodeNotFoundException(nodeId);
        } catch (FeignException e) {
            log.error("HTTP error fetching OSM node {}: {} - {}",
                    nodeId, e.status(), e.getMessage());
            throw new OsmNodeNotFoundException(nodeId);
        } catch (OsmNodeMissingFieldsException e) {
            // re-throw missing fields exception as-is
            throw e;
        } catch (Exception e) {
            log.error("Error fetching OSM node {}", nodeId, e);
            throw new OsmNodeNotFoundException(nodeId);
        }
    }

    /**
     * Parses the OSM XML response and extracts node data.
     *
     * @param xmlResponse the XML response from OSM API
     * @param nodeId the node ID for error reporting
     * @return parsed OsmNode object
     * @throws Exception if XML parsing fails or required fields are missing
     */
    private OsmNode parseOsmXml(String xmlResponse, Long nodeId) throws Exception {
        // parse XML using Jackson (deserializer ensures node element and id are present)
        XmlMapper xmlMapper = new XmlMapper();
        OsmResponse osmResponse = xmlMapper.readValue(xmlResponse, OsmResponse.class);
        Map<String, String> tags = osmResponse.getTags();

        // extract required fields
        String name = getRequiredTag(tags, "name", nodeId);
        String city = getRequiredTag(tags, "addr:city", nodeId);
        String street = getRequiredTag(tags, "addr:street", nodeId);
        String houseNumber = getRequiredTag(tags, "addr:housenumber", nodeId);
        String postcode = getRequiredTag(tags, "addr:postcode", nodeId);
        String amenityStr = getRequiredTag(tags, "amenity", nodeId);
        OsmAmenity amenity = OsmAmenity.fromOsmValue(amenityStr)
                .orElseThrow(() -> {
                    log.warn("OSM node {} has unsupported amenity type: {}", nodeId, amenityStr);
                    return new OsmNodeMissingFieldsException(nodeId, "amenity");
                });

        // extract optional fields
        Optional<String> nameDe = Optional.ofNullable(tags.get("name:de"));
        Optional<String> nameEn = Optional.ofNullable(tags.get("name:en"));
        Optional<String> description = Optional.ofNullable(tags.get("description"));

        // build and return the OsmNode
        return OsmNode.builder()
                .nodeId(nodeId)
                .name(nameEn.or(() -> nameDe).orElse(name)) // prioritize nameEn, then nameDe, then fall back to name
                .amenity(amenity)
                .city(city)
                .street(street)
                .houseNumber(houseNumber)
                .postcode(postcode)
                .description(description.orElse("n/a"))
                .build();
    }

    /**
     * Retrieves a required tag from the tags map.
     *
     * @param tags   the map of OSM tags
     * @param key    the tag key to retrieve
     * @param nodeId the node ID for error reporting
     * @return the tag value
     * @throws OsmNodeMissingFieldsException if the tag is missing
     */
    private String getRequiredTag(Map<String, String> tags, String key, Long nodeId) {
        return Optional.ofNullable(tags.get(key))
                .orElseThrow(() -> {
                    log.warn("OSM node {} is missing required field: '{}'. Available tags: {}",
                            nodeId, key, tags.keySet());
                    return new OsmNodeMissingFieldsException(nodeId, key);
                });
    }
}
