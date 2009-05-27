package com.openexchange.spamhandler.spamassassin.exceptions;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.mail.MailException;


public class SpamhandlerSpamassassinException extends MailException {

    public static enum Code {
        /**
         * Spamd returned wrong exit code "%s"
         */
        WRONG_SPAMD_EXIT("Spamd returned wrong exit code \"%s\"", Category.CODE_ERROR, 3000),

        /**
         * Internal error: Wrong arguments are given to the tell command: "%s"
         */
        WRONG_TELL_CMD_ARGS("Internal error: Wrong arguments are given to the tell command: \"%s\"", Category.CODE_ERROR, 3001),

        /**
         * Error during communication with spamd: "%s"
         */
        COMMUNICATION_ERROR("Error during communication with spamd: \"%s\"", Category.CODE_ERROR, 3002),

        /**
         * Can't handle spam because MailService isn't available
         */
        MAILSERVICE_MISSING("Can't handle spam because MailService isn't available", Category.CODE_ERROR, 3003),
        
        /**
         * Error while getting spamd provider from service: "%s"
         */
        ERROR_GETTING_SPAMD_PROVIDER("Error while getting spamd provider from service: \"%s\"", Category.CODE_ERROR, 3004);


        
        
        private final Category category;

        private final int detailNumber;

        private final String message;

        private Code(final String message, final Category category, final int detailNumber) {
            this.message = message;
            this.detailNumber = detailNumber;
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }

        public String getMessage() {
            return message;
        }

        public int getNumber() {
            return detailNumber;
        }
    }
    
    private static final transient Object[] EMPTY_ARGS = new Object[0];
    
    private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(SpamhandlerSpamassassinException.class);
    
    /**
     * For serialization
     */
    private static final long serialVersionUID = 8765339558941873978L;

    public SpamhandlerSpamassassinException(AbstractOXException cause) {
        super(cause);
    }

    public SpamhandlerSpamassassinException(final Code code) {
        this(code, EMPTY_ARGS);
    }
    
    public SpamhandlerSpamassassinException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    public SpamhandlerSpamassassinException(final Code code, final Throwable cause, final Object... messageArgs) {
        super(EnumComponent.MAIL, code.category, code.detailNumber, code.message, cause);
        super.setMessageArgs(messageArgs);
    }

    public SpamhandlerSpamassassinException(final Component component, final Category category, final int detailNumber, final String message, final Throwable cause) {
        super(component, category, detailNumber, message, cause);
    }

}
