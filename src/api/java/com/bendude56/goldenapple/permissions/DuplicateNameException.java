package com.bendude56.goldenapple.permissions;

/**
 * Thrown when a user tries to connect and their name cannot be changed in the
 * database because a previous user has the same username as them in the
 * database. Typically, this indicates that the first user has changed their
 * username, then the second user changed their username to the previous
 * username of the first user.
 * 
 * @author ben_dude56
 */
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
