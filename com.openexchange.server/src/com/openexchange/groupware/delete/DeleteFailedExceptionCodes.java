package com.openexchange.groupware.delete;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error code for failed delete events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum DeleteFailedExceptionCodes implements OXExceptionCode {
    /**
     * Unknown delete event type: %1$d
     */
    UNKNOWN_TYPE(DeleteFailedExceptionMessage.UNKNOWN_TYPE_MSG, Category.CATEGORY_ERROR, 1),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR(DeleteFailedExceptionMessage.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * An error occurred: %1$s
     */
    ERROR(DeleteFailedExceptionMessage.ERROR_MSG, Category.CATEGORY_ERROR, 3);

    private final String message;

    private final Category category;

    private final int detailNumber;

    private DeleteFailedExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
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
    public String getPrefix() {
        return "DEL";
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
