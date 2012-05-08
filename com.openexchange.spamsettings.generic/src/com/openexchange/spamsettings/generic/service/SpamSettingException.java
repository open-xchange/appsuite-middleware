
package com.openexchange.spamsettings.generic.service;

import com.openexchange.exceptions.ErrorMessage;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * {@link SpamSettingException} - A spam setting exception
 * 
 * dennis.sieben@open-xchange.com
 */
public class SpamSettingException extends AbstractOXException {

    private static final long serialVersionUID = -3554129943396182447L;

    private static final String STR_COMPONENT = "SPAM_SETTING";

    /**
     * The {@link Component} for file storage exception.
     */
    public static final Component COMPONENT = new Component() {

        private static final long serialVersionUID = 7647683196764758763L;

        public String getAbbreviation() {
            return STR_COMPONENT;
        }
    };

    /**
     * Initializes a new {@link SpamSettingException}.
     * 
     * @param cause The cause
     */
    public SpamSettingException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Initializes a new {@link SpamSettingException}.
     * 
     * @param message The message
     * @param cause The cause
     */
    public SpamSettingException(final String message, final AbstractOXException cause) {
        super(COMPONENT, message, cause);
    }

    /**
     * Initializes a new {@link SpamSettingException}.
     * 
     * @param category The category
     * @param detailNumber The detail number
     * @param message The message
     * @param cause The cause
     */
    public SpamSettingException(final Category category, final int detailNumber, final String message, final Throwable cause) {
        super(COMPONENT, category, detailNumber, message, cause);
    }

    /**
     * Initializes a new {@link SpamSettingException}.
     * 
     * @param message The message
     * @param cause The cause
     */
    public SpamSettingException(final ErrorMessage message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes a new {@link SpamSettingException}.
     * 
     * @param message The message
     * @param cause The cause
     * @param messageArguments The message arguments
     */
    public SpamSettingException(final ErrorMessage message, final Throwable cause, final Object... messageArguments) {
        super(message, cause);
        setMessageArgs(messageArguments);
    }

    /**
     * Initializes a new {@link SpamSettingException}.
     * 
     * @param component The component
     * @param message The message
     * @param cause The cause
     */
    protected SpamSettingException(final Component component, final String message, final AbstractOXException cause) {
        super(component, message, cause);
    }

    /**
     * Initializes a new {@link SpamSettingException}.
     * 
     * @param component The component
     * @param category The category
     * @param detailNumber The detail number
     * @param message The message
     * @param cause The cause
     */
    protected SpamSettingException(final Component component, final Category category, final int detailNumber, final String message, final Throwable cause) {
        super(component, category, detailNumber, message, cause);
    }

}
