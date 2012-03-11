package com.openexchange.caching;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;

/**
 * The error code enumeration for cache.
 */
public enum CacheExceptionCodes {

    /**
     * A cache error occurred: %1$s
     */
    CACHE_ERROR("A cache error occurred: %1$s", Category.CATEGORY_ERROR, 1),
    /**
     * Missing cache configuration file at location: %1$s
     */
    MISSING_CACHE_CONFIG_FILE("Missing cache configuration file at location: %1$s", Category.CATEGORY_CONFIGURATION, 2),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", Category.CATEGORY_ERROR, 3),
    /**
     * Missing configuration property: %1$s
     */
    MISSING_CONFIGURATION_PROPERTY("Missing configuration property: %1$s", Category.CATEGORY_CONFIGURATION, 4),
    /**
     * The default element attributes could not be retrieved.
     */
    FAILED_ATTRIBUTE_RETRIEVAL("The default element attributes could not be retrieved.", Category.CATEGORY_ERROR, 5),
    /**
     * A put into the cache failed.
     */
    FAILED_PUT("Put into cache failed.", Category.CATEGORY_ERROR, 6),
    /**
     * Safe put into cache failed. An object bound to given key already exists.
     */
    FAILED_SAFE_PUT("Safe put into cache failed. An object bound to given key already exists.", Category.CATEGORY_ERROR, 7),
    /**
     * Remove on cache failed
     */
    FAILED_REMOVE("Remove on cache failed", Category.CATEGORY_ERROR, 8),
    /**
     * The default element attributes could not be assigned.
     */
    FAILED_ATTRIBUTE_ASSIGNMENT("The default element attributes could not be assigned.", Category.CATEGORY_ERROR, 9),
    /**
     * No cache found for region name: %1$s
     */
    MISSING_CACHE_REGION("No cache found for region name: %1$s", Category.CATEGORY_CONFIGURATION, 10),
    /**
     * Missing default auxiliary defined by property: jcs.default=<aux-name>
     */
    MISSING_DEFAULT_AUX("Missing default auxiliary defined by property: jcs.default=<aux-name>", Category.CATEGORY_CONFIGURATION, 11),
    /**
     * Invalid cache region name \"%1$s\".
     */
    INVALID_CACHE_REGION_NAME("Invalid cache region name \"%1$s\".", Category.CATEGORY_ERROR, 12);

    private final String message;

    private final int detailNumber;

    private final Category category;

    private CacheExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
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

    private static final String PREFIX = "CAC";

    /**
     * Creates an {@link OXException} instance using this error code.
     *
     * @param cause The initial cause for {@link OXException}
     * @param logArguments The arguments for log message.
     * @return The newly created {@link OXException} instance.
     */
    public OXException create(final Throwable cause, final Object... logArguments) {
        return new OXException(detailNumber, OXExceptionStrings.MESSAGE, cause).setPrefix(PREFIX).addCategory(category).setLogMessage(
            message,
            logArguments);
    }
}
