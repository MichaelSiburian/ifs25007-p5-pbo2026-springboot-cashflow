package org.delcom.ifs25007springbootcashflow.domain.exception;

/** Dilempar saat entitas dengan ID tertentu tidak ditemukan. */
public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " dengan ID " + id + " tidak ditemukan.");
    }
}
