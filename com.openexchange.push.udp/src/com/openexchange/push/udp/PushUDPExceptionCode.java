
package com.openexchange.push.udp;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

public enum PushUDPExceptionCode implements OXExceptionCode {
    /**
     * Push UDP Exception.
     */
    PUSH_UDP_EXCEPTION(PushUDPExceptionMessage.PUSH_UDP_EXCEPTION_MSG, 1, CATEGORY_ERROR),
    /**
     * Missing Push UDP configuration.
     */
    MISSING_CONFIG(PushUDPExceptionMessage.MISSING_CONFIG_MSG, 2, CATEGORY_CONFIGURATION),
    /**
     * User ID is not a number: %1$s.
     */
    USER_ID_NAN(PushUDPExceptionMessage.USER_ID_NAN_MSG, 3, CATEGORY_ERROR),
    /**
     * Context ID is not a number: %1$s.
     */
    CONTEXT_ID_NAN(PushUDPExceptionMessage.CONTEXT_ID_NAN_MSG, 4, CATEGORY_ERROR),
    /**
     * Magic bytes are not a number: %1$s.
     */
    MAGIC_NAN(PushUDPExceptionMessage.MAGIC_NAN_MSG, 5, CATEGORY_ERROR),
    /**
     * Invalid Magic bytes: %1$s.
     */
    INVALID_MAGIC(PushUDPExceptionMessage.INVALID_MAGIC_MSG, 6, CATEGORY_ERROR),
    /**
     * Folder ID is not a number: %1$s.
     */
    FOLDER_ID_NAN(PushUDPExceptionMessage.FOLDER_ID_NAN_MSG, 7, CATEGORY_ERROR),
    /**
     * Module is not a number: %1$s.
     */
    MODULE_NAN(PushUDPExceptionMessage.MODULE_NAN_MSG, 8, CATEGORY_ERROR),
    /**
     * Port is not a number: %1$s.
     */
    PORT_NAN(PushUDPExceptionMessage.PORT_NAN_MSG, 9, CATEGORY_ERROR),
    /**
     * Request type is not a number: %1$s.
     */
    TYPE_NAN(PushUDPExceptionMessage.TYPE_NAN_MSG, 10, CATEGORY_ERROR),
    /**
     * Length is not a number: %1$s.
     */
    LENGTH_NAN(PushUDPExceptionMessage.LENGTH_NAN_MSG, 11, CATEGORY_ERROR),
    /**
     * Invalid user IDs: %1$s.
     */
    INVALID_USER_IDS(PushUDPExceptionMessage.INVALID_USER_IDS_MSG, 12, CATEGORY_ERROR),
    /**
     * Unknown request type: %1$s.
     */
    INVALID_TYPE(PushUDPExceptionMessage.INVALID_TYPE_MSG, 13, CATEGORY_ERROR),
    /**
     * Missing payload in datagram package.
     */
    MISSING_PAYLOAD(PushUDPExceptionMessage.MISSING_PAYLOAD_MSG, 14, CATEGORY_ERROR),
    /**
     * No UDP channel is configured.
     */
    NO_CHANNEL(PushUDPExceptionMessage.NO_CHANNEL_MSG, 15, CATEGORY_ERROR);

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
