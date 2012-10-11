package com.openexchange.cache;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for the caching exception
 */
public enum OXCachingExceptionCode implements OXExceptionCode {
    /**
     * A put into the cache failed.
     */
    FAILED_PUT(OXCachingExceptionMessage.FAILED_PUT_MSG, CATEGORY_ERROR, 1),
    /**
     * The default element attributes could not be retrieved
     */
    FAILED_ATTRIBUTE_RETRIEVAL(OXCachingExceptionMessage.FAILED_ATTRIBUTE_RETRIEVAL_MSG, CATEGORY_ERROR, 2),
    /**
     * Remove on cache failed
     */
    FAILED_REMOVE(OXCachingExceptionMessage.FAILED_REMOVE_MSG, CATEGORY_ERROR, 3),
    /**
     * Cache %s could not be initialized due to following error: %s
     */
    FAILED_INIT(OXCachingExceptionMessage.FAILED_INIT_MSG, CATEGORY_ERROR, 4);

    /**
     * Message of the exception.
     */
    final String message;

    /**
     * Category of the exception.
     */
    final Category category;

    /**
     * Detail number of the exception.
     */
    final int detailNumber;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private OXCachingExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "CAC";
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Category getCategory() {
        return category;
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
