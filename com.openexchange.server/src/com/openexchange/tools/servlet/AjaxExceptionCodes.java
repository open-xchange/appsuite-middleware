/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

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
    UNKNOWN_ACTION(AjaxExceptionMessages.UnknownAction_MSG, Category.CATEGORY_ERROR, 1),
    /**
     * Missing the following request parameter: %s
     */
    MISSING_PARAMETER(AjaxExceptionMessages.MISSING_PARAMETER_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * Missing upload image.
     */
    NO_UPLOAD_IMAGE(AjaxExceptionMessages.NoUploadImage_MSG, Category.CATEGORY_ERROR, 3),
    /**
     * Invalid parameter: %s
     */
    IMVALID_PARAMETER(AjaxExceptionMessages.InvalidParameter_MSG, Category.CATEGORY_ERROR, 4),
    /**
     * I/O error while writing to Writer object: %s
     */
    IO_ERROR(AjaxExceptionMessages.IOError_MSG, Category.CATEGORY_ERROR, 5),
    /**
     * Missing AJAX request handler for module %s
     */
    MISSING_REQUEST_HANDLER(AjaxExceptionMessages.MISSING_REQUEST_HANDLER_MSG, Category.CATEGORY_ERROR, 6),
    /**
     * Unknown module: %s.
     */
    UNKNOWN_MODULE(AjaxExceptionMessages.UNKNOWN_MODULE_MSG, Category.CATEGORY_ERROR, 7),
    /**
     * A harmful attachment was detected.
     */
    HARMFUL_ATTACHMENT(AjaxExceptionMessages.HARMFUL_ATTACHMENT_MSG, Category.CATEGORY_ERROR, 8),
    /**
     * JSON error: %s
     */
    JSON_ERROR(AjaxExceptionMessages.JSONError_MSG, Category.CATEGORY_ERROR, 9),
    /**
     * Invalid parameter "%1$s": %2$s
     */
    INVALID_PARAMETER_VALUE(AjaxExceptionMessages.InvalidParameterValue_MSG, Category.CATEGORY_ERROR, 10),
    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR(AjaxExceptionMessages.UnexpectedError_MSG, Category.CATEGORY_ERROR, 11),
    /**
     * A parameter conflict occurred.
     */
    PARAMETER_CONFLICT(AjaxExceptionMessages.ParameterConflict_MSG, Category.CATEGORY_ERROR, 12),
    /**
     * Parameter "%1$s" conflicts with parameter "%2$s".
     */
    EITHER_PARAMETER_CONFLICT(AjaxExceptionMessages.EitherParameterConflict_MSG, Category.CATEGORY_ERROR, 13),
    /**
     * Action "%1$s" on request path "%2$s" is not permitted via a non-secure connection.
     */
    NON_SECURE_DENIED(AjaxExceptionMessages.NonSecureDenied_MSG, Category.CATEGORY_ERROR, 14),
    /**
     * The action "%1$s" is disabled due to server configuration
     */
    DISABLED_ACTION(AjaxExceptionMessages.DisabledAction_MSG, Category.CATEGORY_PERMISSION_DENIED, 15),
    /**
     * No permission for module: %s.
     */
    NO_PERMISSION_FOR_MODULE(AjaxExceptionMessages.NO_PERMISSION_FOR_MODULE, Category.CATEGORY_PERMISSION_DENIED, 16),
    /**
     * Object has been changed in the meantime.
     */
    CONFLICT(AjaxExceptionMessages.CONFLICT, Category.CATEGORY_CONFLICT, 17),
    /**
     * Unexpected result. Expected "%1$s", but is "%2$s".
     */
    UNEXPECTED_RESULT(AjaxExceptionMessages.UNEXPECTED_RESULT, Category.CATEGORY_ERROR, 18),
    /**
     * Too many concurrent requests. Please try again later.
     */
    TOO_MANY_REQUESTS(AjaxExceptionMessages.TOO_MANY_REQUESTS, Category.CATEGORY_TRY_AGAIN, 19),
    /**
     * Bad request. The server is unable to handle the request.
     */
    BAD_REQUEST(AjaxExceptionMessages.BAD_REQUEST, Category.CATEGORY_ERROR, 20),
    /**
     * Unknown AJAX action %1$s in module %2$s.
     */
    UNKNOWN_ACTION_IN_MODULE(AjaxExceptionMessages.UnknownActionInModule_MSG, Category.CATEGORY_ERROR, 21),
    /**
     * The file \"%1$s\" (\"%2$s\") can't be imported as image. Only image types (JPG, GIF, BMP or PNG) are supported.
     */
    NO_IMAGE_FILE(AjaxExceptionMessages.NO_IMAGE_FILE_MSG, Category.CATEGORY_USER_INPUT, 22),
    /**
     * Missing request body.
     */
    MISSING_REQUEST_BODY(AjaxExceptionMessages.MISSING_REQUEST_BODY_MSG, Category.CATEGORY_ERROR, 23),
    /**
     * An HTTP error occurred. Status %1$s. Message %2$s.
     * <p>
     * Throws an HTTP error. Specify status code and status message (optional).
     */
    HTTP_ERROR(AjaxExceptionMessages.HTTP_ERROR_MSG, Category.CATEGORY_ERROR, 24),
    /**
     * Unsupported format: %1$s
     */
    UNSUPPORTED_FORMAT(AjaxExceptionMessages.UNSUPPORTED_FORMAT_MSG, Category.CATEGORY_ERROR, 25),
    /**
     * Missing cookie: %s
     */
    MISSING_COOKIE(AjaxExceptionMessages.MISSING_COOKIE_MSG, Category.CATEGORY_ERROR, 2), // Yapp, the same error code
    /**
     * Cookie "%1$s" cannot be found because requests do not have any cookies.
     */
    MISSING_COOKIES(AjaxExceptionMessages.MISSING_COOKIES_MSG, Category.CATEGORY_ERROR, 2), // Yapp, the same error code

    ;

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
