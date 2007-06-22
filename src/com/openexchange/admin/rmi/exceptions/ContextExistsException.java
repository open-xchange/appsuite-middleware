
/**
 * 
 */
package com.openexchange.admin.rmi.exceptions;

import java.io.Serializable;

/**
 * @author choeger
 *
 */
public class ContextExistsException extends Exception implements Serializable{

	/**
         * For serialization
         */
        private static final long serialVersionUID = 1991615694615324164L;
    
        /**
	 * 
	 */
	public ContextExistsException() {
		super("Context already exists");
	}

	/**
	 * @param message
	 */
	public ContextExistsException(String message) {
		super(message);
		
	}

	/**
	 * @param cause
	 */
	public ContextExistsException(Throwable cause) {
		super(cause);
		
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ContextExistsException(String message, Throwable cause) {
		super(message, cause);		
	}

}
