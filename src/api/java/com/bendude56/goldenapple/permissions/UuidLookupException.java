package com.bendude56.goldenapple.permissions;

/**
 * Thrown when a user cannot be added to the database because their UUID cannot
 * be retrieved from the Mojang servers. This may occur either if the user does
 * not exist or if the Mojang authentication servers cannot be contacted.
 * 
 * @author ben_dude56
 */
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
