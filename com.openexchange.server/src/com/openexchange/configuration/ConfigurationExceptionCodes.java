package com.openexchange.configuration;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for the configuration exception.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum ConfigurationExceptionCodes implements OXExceptionCode {
    /** Filename for property file is not defined. */
    NO_FILENAME("Filename for property file is not defined.", Category.CATEGORY_CONFIGURATION, 1),
    /** File "%1$s" does not exist. */
    FILE_NOT_FOUND("File \"%1$s\" does not exist.", Category.CATEGORY_CONFIGURATION, 2),
    /** File "%1$s" is not readable. */
    NOT_READABLE("File \"%1$s\" is not readable.", Category.CATEGORY_CONFIGURATION, 3),
    /** Cannot read file "%1$s". */
    READ_ERROR("Cannot read file \"%1$s\".", Category.CATEGORY_CONFIGURATION, 4),
    /** Property "%1$s" is not defined. */
    PROPERTY_MISSING("Property \"%1$s\" is not defined.", Category.CATEGORY_CONFIGURATION, 5),
    /** Cannot load class "%1$s". */
    CLASS_NOT_FOUND("Cannot load class \"%1$s\".", Category.CATEGORY_CONFIGURATION, 6),
    /** Invalid configuration: %1$s */
    INVALID_CONFIGURATION("Invalid configuration: %1$s", Category.CATEGORY_CONFIGURATION, 7),
    /** Property %1$s is not an integer */
    PROPERTY_NOT_AN_INTEGER("Property %1$s is not an integer", Category.CATEGORY_CONFIGURATION, 8),
    /** An I/O error occurred: %1$s */
    IO_ERROR("An I/O error occurred: %1$s", Category.CATEGORY_CONFIGURATION, 9);

    private final String message;

    private final Category category;

    private final int detailNumber;

    private ConfigurationExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "CFG";
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