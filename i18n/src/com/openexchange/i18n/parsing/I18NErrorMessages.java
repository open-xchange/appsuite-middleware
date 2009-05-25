
package com.openexchange.i18n.parsing;

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public enum I18NErrorMessages implements OXErrorMessage {

    UNEXPECTED_TOKEN(POParser.CLASS_ID * 100 + 1, I18NErrorStrings.UNEXPECTED_TOKEN, I18NErrorStrings.CHECK_FILE, AbstractOXException.Category.SETUP_ERROR),
    UNEXPECTED_TOKEN_CONSUME(POParser.CLASS_ID * 100 + 2, I18NErrorStrings.UNEXPECTED_TOKEN, I18NErrorStrings.CHECK_FILE, AbstractOXException.Category.SETUP_ERROR),
    EXPECTED_NUMBER(POParser.CLASS_ID * 100 + 3, I18NErrorStrings.EXPECTED_NUMBER, I18NErrorStrings.CHECK_FILE, AbstractOXException.Category.SETUP_ERROR),
    MALFORMED_TOKEN(POParser.CLASS_ID * 100 + 4, I18NErrorStrings.MALFORMED_TOKEN, I18NErrorStrings.CHECK_FILE, AbstractOXException.Category.SETUP_ERROR),
    IO_EXCEPTION(POParser.CLASS_ID * 100 + 5, I18NErrorStrings.IO_EXCEPTION, I18NErrorStrings.FILE_ACCESS, AbstractOXException.Category.SETUP_ERROR);

    public static I18NExceptions FACTORY = new I18NExceptions();

    private AbstractOXException.Category category;

    private String help;

    private String message;

    private int errorCode;

    I18NErrorMessages(final int errorCode, final String message, final String help, final AbstractOXException.Category category) {
        this.category = category;
        this.help = help;
        this.message = message;
        this.errorCode = errorCode;
    }

    public int getDetailNumber() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getHelp() {
        return help;
    }

    public AbstractOXException.Category getCategory() {
        return category;
    }

    public void throwException(final Throwable cause, final Object... args) throws I18NException {
        FACTORY.throwException(this, cause, args);
    }

    public void throwException(final Object... args) throws I18NException {
        throwException(null, args);
    }

}
