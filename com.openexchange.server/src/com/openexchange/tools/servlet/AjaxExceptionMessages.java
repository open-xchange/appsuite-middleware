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

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link AjaxExceptionMessages}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AjaxExceptionMessages implements LocalizableStrings {

    /**
     * Initializes a new {@link AjaxExceptionMessages}
     */
    private AjaxExceptionMessages() {
        super();
    }

    // Unknown AJAX action: %s.
    public static final String UnknownAction_MSG = "Unknown AJAX action: %s.";

    // Unknown AJAX action %1$s in module %2$s.
    public static final String UnknownActionInModule_MSG = "Unknown AJAX action %1$s in module %2$s.";

    // Missing the following request parameter: %s
    public static final String MISSING_PARAMETER_MSG = "Missing the following request parameter: %s";

    // Missing upload image.
    public static final String NoUploadImage_MSG = "Missing upload image.";

    // Invalid parameter: %s
    public static final String InvalidParameter_MSG = "Invalid parameter: %s";

    // I/O error while writing to Writer object: %s
    public static final String IOError_MSG = "I/O error while writing to Writer object: %s";

    // Missing AJAX request handler for module %s
    public static final String MISSING_REQUEST_HANDLER_MSG = "Missing AJAX request handler for module %s";

    // Unknown module: %s.
    public static final String UNKNOWN_MODULE_MSG = "Unknown module: %s.";

    // A harmful attachment was detected.
    public static final String HARMFUL_ATTACHMENT_MSG = "A harmful attachment was detected.";

    // JSON error: %s
    public static final String JSONError_MSG = "JSON error: %s";

    // Invalid parameter "%1$s": %2$s
    public static final String InvalidParameterValue_MSG = "Invalid parameter \"%1$s\": %2$s";

    // Unexpected error: %1$s
    public static final String UnexpectedError_MSG = "Unexpected error: %1$s";

    // A parameter conflict occurred.
    public static final String ParameterConflict_MSG = "A parameter conflict occurred.";

    // Parameter "%1$s" conflicts with parameter "%2$s".
    public static final String EitherParameterConflict_MSG = "Parameter \"%1$s\" conflicts with parameter \"%2$s\".";

    // Action "%1$s" on request path "%2$s" is not permitted via a non-secure connection.
    public static final String NonSecureDenied_MSG =
        "Action \"%1$s\" on request path \"%2$s\" is not permitted via a non-secure connection.";

    // The action "%1$s" is disabled due to server configuration
    public static final String DisabledAction_MSG = "The action \"%1$s\" is disabled due to server configuration";

    // No permission for module: %1$s.
    public static final String NO_PERMISSION_FOR_MODULE = "No permission for module: %1$s.";

    // Object has been changed in the meantime.
    public static final String CONFLICT = "Object has been changed in the meantime.";

    // Unexpected result. Expected "%1$s", but is "%2$s".
    public static final String UNEXPECTED_RESULT = "Unexpected result. Expected \"%1$s\", but is \"%2$s\".";

    // Too many concurrent requests. Please try again later.
    public static final String TOO_MANY_REQUESTS = "Too many concurrent requests. Please try again later.";

    // Bad request. The server is unable to handle the request.
    public static final String BAD_REQUEST = "Bad request. The server is unable to handle the request.";

    // The file \"%1$s\" (\"%2$s\") can't be imported as image. Only image types (JPG, GIF, BMP or PNG) are supported.
    public static final String NO_IMAGE_FILE_MSG = "The file \"%1$s\" (\"%2$s\") can't be imported as image. Only image types (JPG, GIF, BMP or PNG) are supported.";

    // Missing request body.
    public static final String MISSING_REQUEST_BODY_MSG = "Missing request body.";

    // An HTTP error occurred. Status %1$s. Message %2$s.
    public static final String HTTP_ERROR_MSG = "An HTTP error occurred. Status %1$s. Message %2$s.";

    // Unsupported format: %1$s
    public static final String UNSUPPORTED_FORMAT_MSG = "Unsupported format: %1$s";

    // Missing cookie: %1$s
    public static final String MISSING_COOKIE_MSG = "Missing cookie: %1$s";

    // Cookie "%1$s" cannot be found because requests does not have any cookies.
    public static final String MISSING_COOKIES_MSG = "Cookie \"%1$s\" cannot be found because requests does not have any cookies.";

}
