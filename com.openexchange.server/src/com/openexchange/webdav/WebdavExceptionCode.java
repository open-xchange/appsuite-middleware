package com.openexchange.webdav;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.groupware.EnumComponent;

public enum WebdavExceptionCode implements OXExceptionCode {

    /**
     * Invalid value in element &quot;%1$s&quot;: %2$s.
     */
    INVALID_VALUE("Invalid value in element \"%1$s\": %2$s.", CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred.
     */
    IO_ERROR("An I/O error occurred.", CATEGORY_ERROR, 2),
    /**
     * Missing field %1$s.
     */
    MISSING_FIELD("Missing field %1$s.", CATEGORY_ERROR, 3),
    /**
     * Missing header field %1$s.
     */
    MISSING_HEADER_FIELD("Missing header field %1$s.", CATEGORY_ERROR, 4),
    /**
     * Invalid action %1$s.
     */
    INVALID_ACTION("Invalid action %1$s.", CATEGORY_ERROR, 5),
    /**
     * %1$s is not a number.
     */
    NOT_A_NUMBER("%1$s is not a number.", CATEGORY_ERROR, 6),
    /**
     * No principal found: %1$s.
     */
    NO_PRINCIPAL("No principal found: %1$s.", CATEGORY_ERROR, 7),
    /**
     * Empty passwords are not allowed.
     */
    EMPTY_PASSWORD("Empty passwords are not allowed.", CATEGORY_USER_INPUT, 8),
    /**
     * Unsupported authorization mechanism in "Authorization" header: %1$s.
     */
    UNSUPPORTED_AUTH_MECH("Unsupported authorization mechanism in \"Authorization\" header: %1$s.", CATEGORY_ERROR, 9),
    /**
     * Resolving user name "%1$s" failed.
     */
    RESOLVING_USER_NAME_FAILED("Resolving user name \"%1$s\" failed.", CATEGORY_ERROR, 10),
    /**
     * Authentication failed for user name: %1$s
     */
    AUTH_FAILED("Authentication failed for user name: %1$s", CATEGORY_ERROR, 11),
    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR("Unexpected error: %1$s", CATEGORY_ERROR, 11);


    private final String message;

    private final int detailNumber;

    private final Category category;

    private WebdavExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return EnumComponent.WEBDAV.getAbbreviation();
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
