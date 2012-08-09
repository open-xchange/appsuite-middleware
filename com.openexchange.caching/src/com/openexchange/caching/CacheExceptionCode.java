

package com.openexchange.caching;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * The error code enumeration for cache.
 */
public enum CacheExceptionCode implements OXExceptionCode {

    /**
     * A cache error occurred: %1$s
     */
    CACHE_ERROR(CacheExceptionMessage.CACHE_ERROR, Category.CATEGORY_ERROR, 1),
    /**
     * Missing cache configuration file at location: %1$s
     */
    MISSING_CACHE_CONFIG_FILE(CacheExceptionMessage.MISSING_CACHE_CONFIG_FILE, Category.CATEGORY_CONFIGURATION, 2),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(CacheExceptionMessage.IO_ERROR, Category.CATEGORY_ERROR, 3),
    /**
     * Missing configuration property: %1$s
     */
    MISSING_CONFIGURATION_PROPERTY(CacheExceptionMessage.MISSING_CONFIGURATION_PROPERTY, Category.CATEGORY_CONFIGURATION, 4),
    /**
     * The default element attributes could not be retrieved.
     */
    FAILED_ATTRIBUTE_RETRIEVAL(CacheExceptionMessage.FAILED_ATTRIBUTE_RETRIEVAL, Category.CATEGORY_ERROR, 5),
    /**
     * 'Put' into cache failed.
     */
    FAILED_PUT(CacheExceptionMessage.FAILED_PUT, Category.CATEGORY_ERROR, 6),
    /**
     * 'Save put' into cache failed. An object bound to given key already exists.
     */
    FAILED_SAFE_PUT(CacheExceptionMessage.FAILED_SAFE_PUT, Category.CATEGORY_ERROR, 7),
    /**
     * Remove on cache failed
     */
    FAILED_REMOVE(CacheExceptionMessage.FAILED_REMOVE, Category.CATEGORY_ERROR, 8),
    /**
     * The default element attributes could not be assigned.
     */
    FAILED_ATTRIBUTE_ASSIGNMENT(CacheExceptionMessage.FAILED_ATTRIBUTE_ASSIGNMENT, Category.CATEGORY_ERROR, 9),
    /**
     * No cache found for region name: %1$s
     */
    MISSING_CACHE_REGION(CacheExceptionMessage.MISSING_CACHE_REGION, Category.CATEGORY_CONFIGURATION, 10),
    /**
     * Missing default auxiliary defined by property: jcs.default=<aux-name>
     */
    MISSING_DEFAULT_AUX(CacheExceptionMessage.MISSING_DEFAULT_AUX, Category.CATEGORY_CONFIGURATION, 11),
    /**
     * Invalid cache region name \"%1$s\".
     */
    INVALID_CACHE_REGION_NAME(CacheExceptionMessage.INVALID_CACHE_REGION_NAME, Category.CATEGORY_ERROR, 12),
    /**
     * Method not supported.
     */
    UNSUPPORTED_OPERATION(CacheExceptionMessage.UNSUPPORTED_OPERATION, Category.CATEGORY_ERROR, 13)
    ;

    private final String message;

    private final int detailNumber;

    private final Category category;

    private CacheExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return "CAC";
    }

    @Override
    public int getNumber() {
        return detailNumber;
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
