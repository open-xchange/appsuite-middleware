package com.openexchange.imap.entity2acl;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

public enum Entity2ACLExceptionCode implements OXExceptionCode {

    /**
     * Implementing class could not be found
     */
    CLASS_NOT_FOUND(Entity2ACLExceptionMessage.CLASS_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred while creating the socket connection to IMAP server (%1$s): %2$s
     */
    CREATING_SOCKET_FAILED(Entity2ACLExceptionMessage.CREATING_SOCKET_FAILED_MSG, Category.CATEGORY_SERVICE_DOWN, 2),
    /**
     * Instantiating the class failed.
     */
    INSTANTIATION_FAILED(Entity2ACLExceptionMessage.INSTANTIATION_FAILED_MSG, Category.CATEGORY_ERROR, 3),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(Entity2ACLExceptionMessage.IO_ERROR_MSG, Category.CATEGORY_SERVICE_DOWN, 4),
    /**
     * Missing property %1$s in system.properties.
     */
    MISSING_SETTING(Entity2ACLExceptionMessage.MISSING_SETTING_MSG, Category.CATEGORY_CONFIGURATION, 5),
    /**
     * Unknown IMAP server: %1$s
     */
    UNKNOWN_IMAP_SERVER(Entity2ACLExceptionMessage.UNKNOWN_IMAP_SERVER_MSG, Category.CATEGORY_ERROR, 6),
    /**
     * Missing IMAP server arguments to resolve IMAP login to a user
     */
    MISSING_ARG(Entity2ACLExceptionMessage.MISSING_ARG_MSG, Category.CATEGORY_ERROR, 7),
    /**
     * IMAP login %1$s could not be resolved to a user
     */
    RESOLVE_USER_FAILED(Entity2ACLExceptionMessage.RESOLVE_USER_FAILED_MSG, Category.CATEGORY_ERROR, 8),
    /**
     * User %1$s from context %2$s is not known on IMAP server "%3$s".
     */
    UNKNOWN_USER(Entity2ACLExceptionMessage.UNKNOWN_USER_MSG, Category.CATEGORY_ERROR, 9);

    /**
     * Category of the exception.
     */
    final Category category;

    /**
     * Message of the exception.
     */
    final String message;

    /**
     * Detail number of the exception.
     */
    final int number;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private Entity2ACLExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        number = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "ACL";
    }

    /**
     * @return the category.
     */
    @Override
    public Category getCategory() {
        return category;
    }

    /**
     * @return the message.
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * @return the number.
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
