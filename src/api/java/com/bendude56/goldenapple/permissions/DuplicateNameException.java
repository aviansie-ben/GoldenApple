package com.bendude56.goldenapple.permissions;

public class DuplicateNameException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public DuplicateNameException() {
        super();
    }
    
    public DuplicateNameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public DuplicateNameException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DuplicateNameException(String message) {
        super(message);
    }
    
    public DuplicateNameException(Throwable cause) {
        super(cause);
    }
}
