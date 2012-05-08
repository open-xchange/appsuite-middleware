
package com.openexchange.spamsettings.generic.service;

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * {@link SpamSettingExceptionCodes} - Enumeration of all {@link SpamSettingException}s.
 * 
 * @author francisco.laguna@open-xchange.com
 */
public enum SpamSettingExceptionCodes implements OXErrorMessage {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(SpamSettingExceptionMessages.UNEXPECTED_ERROR_MSG, Category.CODE_ERROR, 1),
    COULD_NOT_COERCE_VALUE(SpamSettingExceptionMessages.COULD_NOT_COERCE_VALUE_MSG, Category.CODE_ERROR, 2),
    CAN_NOT_DEFINE_METADATA(SpamSettingExceptionMessages.CAN_NOT_DEFINE_METADATA_MSG, Category.CODE_ERROR, 3),
    CAN_NOT_SET_PROPERTY(SpamSettingExceptionMessages.CAN_NOT_SET_PROPERTY_MSG, Category.CODE_ERROR, 4),
    ;

    private final Category category;

    private final int detailNumber;

    private final String message;

    private SpamSettingExceptionCodes(final String message, final Category category, final int detailNumber) {
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

    public int getDetailNumber() {
        return detailNumber;
    }

    public String getHelp() {
        return null;
    }

    private static final Object[] EMPTY = new Object[0];

    /**
     * Creates a new file storage exception of this error type with no message arguments.
     * 
     * @return A new twitter exception
     */
    public SpamSettingException create() {
        return SpamSettingExceptionFactory.getInstance().create(this, EMPTY);
    }

    /**
     * Creates a new file storage exception of this error type with specified message arguments.
     * 
     * @param messageArgs The message arguments
     * @return A new twitter exception
     */
    public SpamSettingException create(final Object... messageArgs) {
        return SpamSettingExceptionFactory.getInstance().create(this, messageArgs);
    }

    /**
     * Creates a new file storage exception of this error type with specified cause and message arguments.
     * 
     * @param cause The cause
     * @param messageArgs The message arguments
     * @return A new twitter exception
     */
    public SpamSettingException create(final Throwable cause, final Object... messageArgs) {
        return SpamSettingExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
