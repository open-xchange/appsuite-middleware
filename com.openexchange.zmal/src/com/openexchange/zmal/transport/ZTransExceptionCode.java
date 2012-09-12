
package com.openexchange.zmal.transport;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.zmal.ZmalProvider;

/**
 * The SMTP error codes.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum ZTransExceptionCode implements OXExceptionCode {

    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(MailExceptionCode.IO_ERROR),
    /**
     * Unsupported charset-encoding: %1$s
     */
    ENCODING_ERROR(MailExceptionCode.ENCODING_ERROR),
    /**
     * The message part with sequence ID %1$s could not be found in message %2$s in folder %3$s
     */
    PART_NOT_FOUND(ZTransExceptionMessage.PART_NOT_FOUND_MSG, CATEGORY_ERROR, 3003),
    /**
     * Html-2-Text conversion failed: %1$s
     */
    HTML2TEXT_CONVERTER_ERROR(ZTransExceptionMessage.HTML2TEXT_CONVERTER_ERROR_MSG, CATEGORY_ERROR, 3004),
    /**
     * An internal error occurred: %1$s
     */
    INTERNAL_ERROR(ZTransExceptionMessage.INTERNAL_ERROR_MSG, CATEGORY_ERROR, 3005),
    /**
     * No recipient(s) has been defined for new message
     */
    MISSING_RECIPIENTS(ZTransExceptionMessage.MISSING_RECIPIENTS_MSG, CATEGORY_USER_INPUT, 3006),
    /**
     * Message has been successfully sent, but a copy could not be placed in your sent folder
     */
    COPY_TO_SENT_FOLDER_FAILED(MailExceptionCode.COPY_TO_SENT_FOLDER_FAILED),
    /**
     * Receipt acknowledgment cannot be sent: missing header %1$s in message %2$s
     */
    MISSING_NOTIFICATION_HEADER(ZTransExceptionMessage.MISSING_NOTIFICATION_HEADER_MSG, CATEGORY_ERROR, 3008),
    /**
     * No send address could be found in user configuration
     */
    NO_SEND_ADDRESS_FOUND(ZTransExceptionMessage.NO_SEND_ADDRESS_FOUND_MSG, CATEGORY_ERROR, 3009),
    /**
     * No content available in mail part
     */
    NO_CONTENT(ZTransExceptionMessage.NO_CONTENT_MSG, CATEGORY_ERROR, 3010),
    /**
     * Message has been successfully sent, but a copy could not be placed in your sent folder due to exceeded quota.
     */
    COPY_TO_SENT_FOLDER_FAILED_QUOTA(MailExceptionCode.COPY_TO_SENT_FOLDER_FAILED_QUOTA),
    /**
     * No storage access because mail connection is not connected
     */
    NOT_CONNECTED(ZTransExceptionMessage.NOT_CONNECTED_MSG, CATEGORY_ERROR, 3012),
    /**
     * Unable to parse SMTP server URI "%1$s".
     */
    URI_PARSE_FAILED(ZTransExceptionMessage.URI_PARSE_FAILED_MSG, CATEGORY_CONFIGURATION, 3013),
    /**
     * The following recipient is not allowed: %1$s. Please remove associated address and try again.
     */
    RECIPIENT_NOT_ALLOWED(ZTransExceptionMessage.RECIPIENT_NOT_ALLOWED, CATEGORY_USER_INPUT, 3014),
    ;

    private final String message;

    private final int detailNumber;

    private final Category category;

    private final String prefix;

    private ZTransExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        prefix = ZmalProvider.PROTOCOL_ZMAL.getName();
    }

    private ZTransExceptionCode(final MailExceptionCode code) {
        message = code.getMessage();
        detailNumber = code.getNumber();
        category = code.getCategory();
        prefix = code.getPrefix();
    }

    @Override
    public String getPrefix() {
        return prefix;
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
