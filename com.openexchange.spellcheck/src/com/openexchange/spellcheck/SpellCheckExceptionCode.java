package com.openexchange.spellcheck;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

public enum SpellCheckExceptionCode implements OXExceptionCode {

    /**
     * Spell check property '%1$s' not specified in configuration.
     */
    MISSING_PROPERTY("Spell check property '%1$s' not specified in configuration.", CATEGORY_CONFIGURATION, 1),
    /**
     * Spell check directory '%1$s' not found or is not a directory
     */
    MISSING_DIR("Spell check directory '%1$s' not found or is not a directory", CATEGORY_CONFIGURATION, 2),
    /**
     * Only one phonetic file is allowed per locale
     */
    ONLY_ONE_PHON_FILE("Only one phonetic file is allowed per locale", CATEGORY_CONFIGURATION, 3),
    /**
     * At least one word list file per locale
     */
    AT_LEAST_ONE_WL_FILE("At least one word list file per locale", CATEGORY_CONFIGURATION, 4),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 5),
    /**
     * No locale directory found
     */
    NO_LOCALE_FOUND("No locale directory found", CATEGORY_CONFIGURATION, 6),
    /**
     * No dictionary available for locale %1$s
     */
    MISSING_LOCALE_DIC("No dictionary available for locale %1$s", CATEGORY_CONFIGURATION, 7),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR("A SQL error occurred: %1$s", CATEGORY_ERROR, 8),
    /**
     * Invalid format of user dictionary: %1$s
     */
    INVALID_FORMAT("Invalid format of user dictionary: %1$s", CATEGORY_ERROR, 9),
    /**
     * Spell check servlet cannot be registered: %1$s
     */
    SERVLET_REGISTRATION_FAILED("Spell check servlet cannot be registered: %1$s", CATEGORY_ERROR, 10),
    /**
     * Missing parameter %1$s
     */
    MISSING_PARAM("Missing parameter %1$s", CATEGORY_ERROR, 11),
    /**
     * Unsupported value parameter %1$s: %2$s
     */
    UNSUPPORTED_PARAM("Unsupported value parameter %1$s: %2$s", CATEGORY_ERROR, 12),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", CATEGORY_ERROR, 13),
    /**
     * Invalid locale string: %1$s
     */
    INVALID_LOCALE_STR("Invalid locale string: %1$s", CATEGORY_ERROR, 14);

    private final String message;

    private final int detailNumber;

    private final Category category;

    private SpellCheckExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
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
    public String getPrefix() {
        return "SPELLCHECK";
    }

    @Override
    public boolean equals(final OXException e) {
        return getPrefix().equals(e.getPrefix()) && e.getCode() == getNumber();
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