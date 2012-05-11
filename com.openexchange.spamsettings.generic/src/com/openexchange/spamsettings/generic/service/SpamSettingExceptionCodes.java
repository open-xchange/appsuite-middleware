
package com.openexchange.spamsettings.generic.service;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link SpamSettingExceptionCodes} - Enumeration of all {@link SpamSettingException}s.
 * 
 * @author francisco.laguna@open-xchange.com
 */
public enum SpamSettingExceptionCodes implements OXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(SpamSettingExceptionMessages.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 1),
    COULD_NOT_COERCE_VALUE(SpamSettingExceptionMessages.COULD_NOT_COERCE_VALUE_MSG, CATEGORY_ERROR, 2),
    CAN_NOT_DEFINE_METADATA(SpamSettingExceptionMessages.CAN_NOT_DEFINE_METADATA_MSG, CATEGORY_ERROR, 3),
    CAN_NOT_SET_PROPERTY(SpamSettingExceptionMessages.CAN_NOT_SET_PROPERTY_MSG, CATEGORY_ERROR, 4),
    ;

    private final Category category;

    private final int detailNumber;

    private final String message;

    private SpamSettingExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    private static final String PREFIX = "SPAM_SETTING"; // aka "SSG"

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates an {@link OXException} instance using this error code.
     * 
     * @return The newly created {@link OXException} instance.
     */
    public OXException create() {
        return create(new Object[0]);
    }

    /**
     * Creates an {@link OXException} instance using this error code.
     * 
     * @param logArguments The arguments for log message.
     * @return The newly created {@link OXException} instance.
     */
    public OXException create(final Object... logArguments) {
        return create(null, logArguments);
    }

    /**
     * Creates an {@link OXException} instance using this error code.
     * 
     * @param cause The initial cause for {@link OXException}
     * @param arguments The arguments for message.
     * @return The newly created {@link OXException} instance.
     */
    public OXException create(final Throwable cause, final Object... arguments) {
        return OXExceptionFactory.getInstance().create(this, cause, arguments);
    }
}
