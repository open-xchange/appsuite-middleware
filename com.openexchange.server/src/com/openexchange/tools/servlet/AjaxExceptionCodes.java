package com.openexchange.tools.servlet;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for AJAX-related servlet exceptions.
 */
public enum AjaxExceptionCodes implements OXExceptionCode {
    /**
     * Unknown AJAX action: %s.
     */
    UNKNOWN_ACTION("Unknown AJAX action: %s.", Category.CATEGORY_ERROR, 1),
    /**
     * Missing the following request parameter: %s
     */
    MISSING_PARAMETER("Missing the following request parameter: %s", Category.CATEGORY_ERROR, 2),
    /**
     * Missing upload image.
     */
    NO_UPLOAD_IMAGE("Missing upload image.", Category.CATEGORY_ERROR, 3),
    /**
     * Invalid parameter: %s
     */
    IMVALID_PARAMETER("Invalid parameter: %s", Category.CATEGORY_ERROR, 4),
    /**
     * I/O error while writing to Writer object: %s
     */
    IO_ERROR("I/O error while writing to Writer object: %s", Category.CATEGORY_ERROR, 5),
    /**
     * Missing AJAX request handler for module %s
     */
    MISSING_REQUEST_HANDLER("Missing AJAX request handler for module %s", Category.CATEGORY_ERROR, 6),
    /**
     * Unknown module: %s.
     */
    UNKNOWN_MODULE("Unknown module: %s.", Category.CATEGORY_ERROR, 7),
    /**
     * A harmful attachment was detected.
     */
    HARMFUL_ATTACHMENT("A harmful attachment was detected.", Category.CATEGORY_ERROR, 8),
    /**
     * JSON error: %s
     */
    JSON_ERROR("JSON error: %s", Category.CATEGORY_ERROR, 9),
    /**
     * Invalid parameter "%1$s": %2$s
     */
    INVALID_PARAMETER_VALUE("Invalid parameter \"%1$s\": %2$s", Category.CATEGORY_ERROR, 10),
    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR("Unexpected error: %1$s", Category.CATEGORY_ERROR, 11),
    /**
     * A parameter conflict occurred.
     */
    PARAMETER_CONFLICT("A parameter conflict occurred.", Category.CATEGORY_ERROR, 12),
    /**
     * Parameter "%1$s" conflicts with parameter "%2$s".
     */
    EITHER_PARAMETER_CONFLICT("Parameter \"%1$s\" conflicts with parameter \"%2$s\".", Category.CATEGORY_ERROR, 13),
    /**
     * Action "%1$s" on request path "%2$s" is not permitted via a non-secure connection.
     */
    NON_SECURE_DENIED("Action \"%1$s\" on request path \"%2$s\" is not permitted via a non-secure connection.", Category.CATEGORY_ERROR, 14),
    /**
     * The action "%1$s" is disabled due to server configuration
     */
    DISABLED_ACTION("The action \"%1$s\" is disabled due to server configuration", Category.CATEGORY_PERMISSION_DENIED, 15),
    /**
     * No permission for module: %s.
     */
    NO_PERMISSION_FOR_MODULE("No permission for module: %1$s.", Category.CATEGORY_PERMISSION_DENIED, 16),
    /**
     * Object has been changed in the meantime.
     */
    CONFLICT("Object has been changed in the meantime.", Category.CATEGORY_CONFLICT, 17),
    /**
     * Unexpected result. Expected "%1$s", but is "%2$s".
     */
    UNEXPECTED_RESULT("Unexpected result. Expected \"%1$s\", but is \"%2$s\".", Category.CATEGORY_ERROR, 18),
    /**
     * Too many concurrent requests. Please try again later.
     */
    TOO_MANY_REQUESTS("Too many concurrent requests. Please try again later.", Category.CATEGORY_TRY_AGAIN, 19),
    /**
     * Bad request. The server is unable to handle the request.
     */
    BAD_REQUEST("Bad request. The server is unable to handle the request.", Category.CATEGORY_ERROR, 20);

    public static final String PREFIX = "SVL";

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

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private AjaxExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        number = detailNumber;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
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
