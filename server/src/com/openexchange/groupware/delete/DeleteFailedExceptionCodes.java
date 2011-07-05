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
    UNKNOWN_TYPE("Unknown delete event type: %1$d", Category.CATEGORY_ERROR, 1),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR("A SQL error occurred: %1$s", Category.CATEGORY_ERROR, 2),
    /**
     * An error occurred: %1$s
     */
    ERROR("An error occurred: %1$s", Category.CATEGORY_ERROR, 3);

    private final String message;

    private final Category category;

    private final int detailNumber;

    private DeleteFailedExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
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
    
    public String getPrefix() {
        return "DEL";
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