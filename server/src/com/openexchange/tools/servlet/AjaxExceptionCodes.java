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
    UnknownAction("Unknown AJAX action: %s.", Category.CATEGORY_ERROR, 1),
    /**
     * Missing the following request parameter: %s
     */
    MISSING_PARAMETER("Missing the following request parameter: %s", Category.CATEGORY_ERROR, 2),
    /**
     * Missing upload image.
     */
    NoUploadImage("Missing upload image.", Category.CATEGORY_ERROR, 3),
    /**
     * Invalid parameter: %s
     */
    InvalidParameter("Invalid parameter: %s", Category.CATEGORY_ERROR, 4),
    /**
     * I/O error while writing to Writer object: %s
     */
    IOError("I/O error while writing to Writer object: %s", Category.CATEGORY_ERROR, 5),
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
    JSONError("JSON error: %s", Category.CATEGORY_ERROR, 9),
    /**
     * Invalid parameter "%1$s": %2$s
     */
    InvalidParameterValue("Invalid parameter \"%1$s\": %2$s", Category.CATEGORY_ERROR, 10),
    /**
     * Unexpected error: %1$s
     */
    UnexpectedError("Unexpected error: %1$s", Category.CATEGORY_ERROR, 11),
    /**
     * A parameter conflict occurred.
     */
    ParameterConflict("A parameter conflict occurred.", Category.CATEGORY_ERROR, 12),
    /**
     * Parameter "%1$s" conflicts with parameter "%2$s".
     */
    EitherParameterConflict("Parameter \"%1$s\" conflicts with parameter \"%2$s\".", Category.CATEGORY_ERROR, 13),
    /**
     * Action "%1$s" on request path "%2$s" is not permitted via a non-secure connection.
     */
    NonSecureDenied("Action \"%1$s\" on request path \"%2$s\" is not permitted via a non-secure connection.", Category.CATEGORY_ERROR, 14),
    /**
     * The action "%1$s" is disabled due to server configuration
     */
    DisabledAction("The action \"%1$s\" is disabled due to server configuration", Category.CATEGORY_PERMISSION_DENIED, 15),
    /**
     * No permission for module: %s.
     */
    NoPermissionForModule("No permission for module: %1$s.", Category.CATEGORY_PERMISSION_DENIED, 16),
    /**
     * Object has been changed in the meantime.
     */
    Conflict("Object has been changed in the meantime.", Category.CATEGORY_CONFLICT, 17),
    
    ;
    
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

    public String getPrefix() {
        return "SVL";
    }

    public Category getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public int getNumber() {
        return number;
    }

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