package com.openexchange.groupware.downgrade;

import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Error code enumeration for a failed delete event.
 */
public enum DowngradeFailedExceptionCode implements OXExceptionCode {

    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR("A SQL error occurred: %1$s", Category.CATEGORY_ERROR, 1),
    /**
     * An error occurred: %1$s
     */
    ERROR("An error occurred: %1$s", Category.CATEGORY_ERROR, 2);

    private final String message;

    private final Category category;

    private final int detailNumber;

    private final boolean display;

    private DowngradeFailedExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
        display = category.getLogLevel().implies(LogLevel.DEBUG);
    }

    public String getPrefix() {
        return "DOW";
    }

    public final Category getCategory() {
        return category;
    }

    public final int getNumber() {
        return detailNumber;
    }

    public final String getMessage() {
        return message;
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return create(new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return create((Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        final OXException ret;
        if (display) {
            ret = new OXException(detailNumber, message, cause, args);
        } else {
            ret =
                new OXException(
                    detailNumber,
                    Category.EnumType.TRY_AGAIN.equals(category.getType()) ? OXExceptionStrings.MESSAGE_RETRY : OXExceptionStrings.MESSAGE,
                    new Object[0]).setLogMessage(message, args);
        }
        return ret.addCategory(category).setPrefix(getPrefix());
    }
}