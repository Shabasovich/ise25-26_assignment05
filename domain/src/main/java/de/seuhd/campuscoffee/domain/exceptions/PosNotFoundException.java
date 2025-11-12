package de.seuhd.campuscoffee.domain.exceptions;

/**
 * Exception thrown when a POS entity is not found in the database.
 */
public class PosNotFoundException extends RuntimeException {
    public PosNotFoundException(Long posId) {
        super("POS with ID " + posId + " does not exist.");
    }
    public PosNotFoundException(String name) {
        super("POS with name '" + name + "' does not exist.");
    }
}
