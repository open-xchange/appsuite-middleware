package com.openexchange.tools.file.external;

import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for the file storage exception.
 *
 * @author Steffen Templin
 */
public enum QuotaFileStorageExceptionCodes implements OXExceptionCode {
    /** Couldn't reach the filestore */
    INSTANTIATIONERROR("Couldn't reach the filestore", Category.CATEGORY_SERVICE_DOWN, 21),
    /** Database Query could not be realized */
    SQLSTATEMENTERROR("Database Query could not be realized", Category.CATEGORY_ERROR, 23),
    /** The allowed Quota is reached. */
    STORE_FULL("The allowed Quota is reached.", Category.CATEGORY_USER_INPUT, 24),
    /** Quota seems to be inconsistent. Please use consistency tool on context %1$d. */
    QUOTA_UNDERRUN("Quota seems to be inconsistent. Please use consistency tool on context %1$d.", Category.CATEGORY_TRUNCATED, 25),
    /** Quota usage is missing for context %1$d. */
    NO_USAGE("Quota usage is missing for context %1$d.", Category.CATEGORY_ERROR, 26),
    /** Update of quota usage for context %1$d failed. */
    UPDATE_FAILED("Update of quota usage for context %1$d failed.", Category.CATEGORY_ERROR, 27);

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

    private final boolean display;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private QuotaFileStorageExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.number = detailNumber;
        display = category.getLogLevel().implies(LogLevel.DEBUG);
    }

    @Override
    public String getPrefix() {
        return "FLS";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return number;
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