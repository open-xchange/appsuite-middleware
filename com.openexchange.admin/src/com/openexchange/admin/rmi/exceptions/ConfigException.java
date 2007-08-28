package com.openexchange.admin.rmi.exceptions;

public class ConfigException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -1939972861295634777L;

    public ConfigException() {
            super("Context already exists");
    }

    public ConfigException(String message) {
            super(message);
            
    }

    public ConfigException(Throwable cause) {
            super(cause);
            
    }

    public ConfigException(String message, Throwable cause) {
            super(message, cause);          
    }

}
