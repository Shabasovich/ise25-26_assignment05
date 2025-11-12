package de.seuhd.campuscoffee.domain.model;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

/**
 * Represents an OpenStreetMap node with relevant Point of Sale information.
 * This is the domain model for OSM data before it is converted to a POS object.
 *
 * @param nodeId The OpenStreetMap node ID.
 */
@Builder
public record OsmNode(@NonNull Long nodeId, @NonNull String city, @NonNull String houseNumber, @NonNull String postcode,
                      @NonNull String street, @NonNull OsmAmenity amenity, @NonNull String name,
                      @NonNull String description) {
}
