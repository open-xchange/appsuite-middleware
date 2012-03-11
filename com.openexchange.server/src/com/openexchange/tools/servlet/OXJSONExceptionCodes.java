package com.openexchange.tools.servlet;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for JSON exceptions.
 */
public enum OXJSONExceptionCodes implements OXExceptionCode {
    /**
     * Exception while writing JSON.
     */
    JSON_WRITE_ERROR("Exception while writing JSON.", Category.CATEGORY_ERROR, 1),
    /**
     * Exception while parsing JSON: "%s".
     */
    JSON_READ_ERROR("Exception while parsing JSON: \"%s\".", Category.CATEGORY_ERROR, 2),
    /**
     * Invalid cookie.
     * */
    INVALID_COOKIE("Invalid cookie.", Category.CATEGORY_TRY_AGAIN, 3),
    /**
     * Exception while building JSON.
     */
    JSON_BUILD_ERROR("Exception while building JSON.", Category.CATEGORY_ERROR, 4),
    /**
     * Value "%1$s" of attribute %s contains non digit characters. */
    CONTAINS_NON_DIGITS("Value \"%1$s\" of attribute %2$s contains non digit characters.", Category.CATEGORY_USER_INPUT, 5),
    /**
     * Too many digits within field %1$s.
     */
    TOO_BIG_NUMBER("Too many digits within field %1$s.", Category.CATEGORY_USER_INPUT, 6),
    /**
     * Unable to parse value "%1$s" within field %2$s as a number.
     */
    NUMBER_PARSING("Unable to parse value \"%1$s\" within field %2$s as a number.", Category.CATEGORY_ERROR, 7),
    /**
     * Invalid value \"%2$s\" in JSON attribute \"%1$s\".
     */
    INVALID_VALUE("Invalid value \"%2$s\" in JSON attribute \"%1$s\".", Category.CATEGORY_USER_INPUT, 8),
    /**
     * Missing field "%1$s" in JSON data.
     */
    MISSING_FIELD("Missing field \"%1$s\" in JSON data.", Category.CATEGORY_ERROR, 9);

    private static final String PREFIX = "SVL";
    
    private final String message;
    private final Category category;
    private final int number;

    private OXJSONExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        number = detailNumber;
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
    public int getNumber() {
        return number;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
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
