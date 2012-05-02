package com.openexchange.groupware.downgrade;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error code enumeration for a failed delete event.
 */
public enum DowngradeFailedExceptionCode implements OXExceptionCode {

    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR(DowngradeFailedExceptionMessage.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 1),
    /**
     * An error occurred: %1$s
     */
    ERROR(DowngradeFailedExceptionMessage.ERROR_MSG, Category.CATEGORY_ERROR, 2);

    private final String message;

    private final Category category;

    private final int detailNumber;

    private DowngradeFailedExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "DOW";
    }

    @Override
    public final Category getCategory() {
        return category;
    }

    @Override
    public final int getNumber() {
        return detailNumber;
    }

    @Override
    public final String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
