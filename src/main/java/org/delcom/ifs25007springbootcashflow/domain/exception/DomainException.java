package org.delcom.ifs25007springbootcashflow.domain.exception;

/** Exception dasar (checked) untuk seluruh kegagalan bisnis di domain. */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }
}
