
package com.openexchange.groupware.contexts.impl;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for context exceptions.
 */
public enum ContextExceptionCodes implements OXExceptionCode {
    /**
     * Mailadmin for a context is missing.
     */
    NO_MAILADMIN(ContextExceptionMessage.NO_MAILADMIN_MSG, Category.CATEGORY_CONFIGURATION, 1),
    /**
     * Cannot find context %d.
     */
    NOT_FOUND(ContextExceptionMessage.NOT_FOUND_MSG, Category.CATEGORY_CONFIGURATION, 2),
    /**
     * No connection to database.
     */
    NO_CONNECTION(ContextExceptionMessage.NO_CONNECTION_MSG, Category.CATEGORY_SERVICE_DOWN, 5),
    /**
     * SQL problem: %1$s.
     */
    SQL_ERROR(ContextExceptionMessage.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 6),
    /**
     * Updating database ... Try again later.
     */
    UPDATE(ContextExceptionMessage.UPDATE_MSG, Category.CATEGORY_TRY_AGAIN, 7),
    /**
     * Problem initializing the cache.
     */
    CACHE_INIT(ContextExceptionMessage.CACHE_INIT_MSG, Category.CATEGORY_CONFIGURATION, 8),
    /**
     * Cannot remove object %s from cache.
     */
    CACHE_REMOVE(ContextExceptionMessage.CACHE_REMOVE_MSG, Category.CATEGORY_ERROR, 9),
    /**
     * Cannot find context "%s".
     */
    NO_MAPPING(ContextExceptionMessage.NO_MAPPING_MSG, Category.CATEGORY_ERROR, 10);

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
     *
     * @param message message.
     * @param category category.
     * @param number detail number.
     */
    private ContextExceptionCodes(final String message, final Category category, final int number) {
        this.message = message;
        this.category = category;
        this.number = number;
    }

    @Override
    public String getPrefix() {
        return "CTX";
    }

    /**
     * @return the message
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * @return the category
     */
    @Override
    public Category getCategory() {
        return category;
    }

    /**
     * @return the number
     */
    @Override
    public int getNumber() {
        return number;
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
