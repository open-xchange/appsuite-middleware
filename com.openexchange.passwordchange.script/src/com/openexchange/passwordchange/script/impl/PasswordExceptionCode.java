
package com.openexchange.passwordchange.script.impl;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link PasswordExceptionCode}
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum PasswordExceptionCode implements OXExceptionCode {
    /**
     * Cannot change password for any reason.
     */
    PASSWORD_FAILED(PasswordExceptionMessage.PASSWORD_FAILED_MSG, CATEGORY_PERMISSION_DENIED, 1),
    /**
     * New password too short.
     */
    PASSWORD_SHORT(PasswordExceptionMessage.PASSWORD_SHORT_MSG, CATEGORY_USER_INPUT, 2),
    /**
     * New password too weak.
     */
    PASSWORD_WEAK(PasswordExceptionMessage.PASSWORD_WEAK_MSG, CATEGORY_USER_INPUT, 3),
    /**
     * User not found.
     */
    PASSWORD_NOUSER(PasswordExceptionMessage.PASSWORD_NOUSER_MSG, CATEGORY_CONFIGURATION, 4),
    /**
     * User not found.
     */
    LDAP_ERROR(PasswordExceptionMessage.LDAP_ERROR_MSG, CATEGORY_CONFIGURATION, 5),
    /**
     * A database connection cannot be obtained.
     */
    NO_CONNECTION(PasswordExceptionMessage.NO_CONNECTION_MSG, CATEGORY_SERVICE_DOWN, 6),

    /**
     * No permission to modify resources in context %1$s
     */
    PERMISSION(PasswordExceptionMessage.PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 7);

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
    private final int detailNumber;

    /**
     * Default constructor.
     * 
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private PasswordExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "PSW";
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
