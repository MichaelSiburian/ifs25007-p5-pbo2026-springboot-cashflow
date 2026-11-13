package org.delcom.ifs25007springbootcashflow.domain.exception;

/** Dilempar saat validasi input bisnis gagal. */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
