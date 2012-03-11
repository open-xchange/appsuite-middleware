package com.openexchange.push.udp;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

public enum PushUDPExceptionCode implements OXExceptionCode {
    /**
     * Push UDP Exception.
     */
    PUSH_UDP_EXCEPTION("Push UDP Exception.", 1, CATEGORY_ERROR),
    /**
     * Missing Push UDP configuration.
     */
    MISSING_CONFIG("Missing Push UDP configuration.", 2, CATEGORY_CONFIGURATION),
    /**
     * User ID is not a number: %1$s.
     */
    USER_ID_NAN("User ID is not a number: %1$s.", 3, CATEGORY_ERROR),
    /**
     * Context ID is not a number: %1$s.
     */
    CONTEXT_ID_NAN("Context ID is not a number: %1$s.", 4, CATEGORY_ERROR),
    /**
     * Magic bytes are not a number: %1$s.
     */
    MAGIC_NAN("Magic bytes are not a number: %1$s.", 5, CATEGORY_ERROR),
    /**
     * Invalid Magic bytes: %1$s.
     */
    INVALID_MAGIC("Invalid Magic bytes: %1$s.", 6, CATEGORY_ERROR),
    /**
     * Folder ID is not a number: %1$s.
     */
    FOLDER_ID_NAN("Folder ID is not a number: %1$s.", 7, CATEGORY_ERROR),
    /**
     * Module is not a number: %1$s.
     */
    MODULE_NAN("Module is not a number: %1$s.", 8, CATEGORY_ERROR),
    /**
     * Port is not a number: %1$s.
     */
    PORT_NAN("Port is not a number: %1$s.", 9, CATEGORY_ERROR),
    /**
     * Request type is not a number: %1$s.
     */
    TYPE_NAN("Request type is not a number: %1$s.", 10, CATEGORY_ERROR),
    /**
     * Length is not a number: %1$s.
     */
    LENGTH_NAN("Length is not a number: %1$s.", 11, CATEGORY_ERROR),
    /**
     * Invalid user IDs: %1$s.
     */
    INVALID_USER_IDS("Invalid user IDs: %1$s.", 12, CATEGORY_ERROR),
    /**
     * Unknown request type: %1$s.
     */
    INVALID_TYPE("Unknown request type: %1$s.", 13, CATEGORY_ERROR),
    /**
     * Missing payload in datagram package.
     */
    MISSING_PAYLOAD("Missing payload in datagram package.", 14, CATEGORY_ERROR),
    /**
     * Missing payload in datagram package.
     */
    NO_CHANNEL("No UDP channel is configured. Check for failed channel opens on server startup.", 15, CATEGORY_ERROR);


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
    private PushUDPExceptionCode(final String message, final int detailNumber, final Category category) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "PUSHUDP";
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
