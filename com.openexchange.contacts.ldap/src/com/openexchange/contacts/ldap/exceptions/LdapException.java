package com.openexchange.contacts.ldap.exceptions;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;


public class LdapException extends OXException {

    /**
     * Error codes for permission exceptions.
     * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
     */
    public enum Code {
        /**
         * LDAP contacts cannot be deleted
         */
        DELETE_NOT_POSSIBLE("LDAP contacts cannot be deleted", Category.PERMISSION, 1);
    
        /**
         * Message of the exception.
         */
        private final String message;
    
        /**
         * Category of the exception.
         */
        private final Category category;
    
        /**
         * Detail number of the exception.
         */
        private final int number;
    
        /**
         * Default constructor.
         * @param message message.
         * @param category category.
         * @param detailNumber detail number.
         */
        private Code(final String message, final Category category,
            final int detailNumber) {
            this.message = message;
            this.category = category;
            this.number = detailNumber;
        }
    
        public Category getCategory() {
            return category;
        }
    
        public String getMessage() {
            return message;
        }
    
        public int getNumber() {
            return number;
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 3828591312217664226L;

    public LdapException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }
    
    /**
     * Initializes a new exception using the information provided by the code.
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public LdapException(final Code code, final Throwable cause,
        final Object... messageArgs) {
        super(EnumComponent.PERMISSION, code.category, code.number,
            null == code.message ? cause.getMessage() : code.message, cause);
        setMessageArgs(messageArgs);
    }
    
    /**
     * Constructor with all parameters.
     * @param component Component.
     * @param category Category.
     * @param number detail number.
     * @param message message of the exception.
     * @param cause the cause.
     * @param messageArgs arguments for the exception message.
     */
    public LdapException(final EnumComponent component, final Category category,
        final int number, final String message, final Throwable cause, final Object... messageArgs) {
        super(component, category, number, message, cause);
        super.setMessageArgs(messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the cause.
     * 
     * @param cause
     *            the cause of the exception.
     */
    public LdapException(final AbstractOXException cause) {
        super(cause);
    }

}
