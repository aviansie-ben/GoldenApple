package com.bendude56.goldenapple.permissions;

public class UuidLookupException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public UuidLookupException() {
        super();
    }
    
    public UuidLookupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public UuidLookupException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UuidLookupException(String message) {
        super(message);
    }
    
    public UuidLookupException(Throwable cause) {
        super(cause);
    }
}
