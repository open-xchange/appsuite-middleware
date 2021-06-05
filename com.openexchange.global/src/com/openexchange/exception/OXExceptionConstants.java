/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.exception;


/**
 * {@link OXExceptionConstants} - Provides access to constants for <a href="http://www.open-xchange.com">Open-Xchange</a> exceptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface OXExceptionConstants {

    /**
     * The default error code: <code>9999</code>.
     */
    public static final int CODE_DEFAULT = 9999;

    /**
     * The empty message.
     */
    public static final String EMPTY_MSG = "[Not available]";

    /**
     * The empty message arguments.
     */
    public static final Object[] MESSAGE_ARGS_EMPTY = new Object[0];

    /*-
     * ------------------------------- Constants for categories -----------------------------------
     */

    /**
     * The category for an error.
     */
    public static final Category CATEGORY_ERROR = Category.CATEGORY_ERROR;

    /**
     * The default category for an invalid user input.
     */
    public static final Category CATEGORY_USER_INPUT = Category.CATEGORY_USER_INPUT;

    /**
     * The default category for a configuration issue (e.g. missing required property).
     */
    public static final Category CATEGORY_CONFIGURATION = Category.CATEGORY_CONFIGURATION;

    /**
     * The default category for a permission-denied issue using debug log level.
     */
    public static final Category CATEGORY_PERMISSION_DENIED = Category.CATEGORY_PERMISSION_DENIED;

    /**
     * The default category for a try-again issue using debug log level.
     */
    public static final Category CATEGORY_TRY_AGAIN = Category.CATEGORY_TRY_AGAIN;

    /**
     * The default category for a connectivity issue (e.g. broken/lost TCP connection) using debug log level.
     */
    public static final Category CATEGORY_CONNECTIVITY = Category.CATEGORY_CONNECTIVITY;

    /**
     * The default category for a missing service or system (e.g. database) using debug log level.
     */
    public static final Category CATEGORY_SERVICE_DOWN = Category.CATEGORY_SERVICE_DOWN;

    /**
     * The default category for truncated data using error log level.
     */
    public static final Category CATEGORY_TRUNCATED = Category.CATEGORY_TRUNCATED;

    /**
     * The default category for conflicting data using debug log level.
     */
    public static final Category CATEGORY_CONFLICT = Category.CATEGORY_CONFLICT;

    /**
     * The default category for if a 3rd party system reported capacity restrictions (e.g. quota).
     */
    public static final Category CATEGORY_CAPACITY = Category.CATEGORY_CAPACITY;

    /**
     * The default category for a warning displayed to user.
     */
    public static final Category CATEGORY_WARNING = Category.CATEGORY_WARNING;

    /*-
     * ------------------------------- Constants for prefixes -----------------------------------
     */

    /**
     * The general, all-purpose error code prefix with no certain affiliation.
     */
    public static final String PREFIX_GENERAL = "OX";

    /*-
     * ------------------------------- Constants for property names -------------------------------
     */

    /**
     * The property name for session identifier.
     */
    public static final String PROPERTY_SESSION = "com.openexchange.exception.sessionId";

    /**
     * The property name for client identifier.
     */
    public static final String PROPERTY_CLIENT = "com.openexchange.exception.client";

    /**
     * The property name for authentication identifier.
     */
    public static final String PROPERTY_AUTH_ID = "com.openexchange.exception.authId";

    /**
     * The property name for login string.
     */
    public static final String PROPERTY_LOGIN = "com.openexchange.exception.login";

    /**
     * The property name for user identifier.
     */
    public static final String PROPERTY_USER = "com.openexchange.exception.user";

    /**
     * The property name for context identifier.
     */
    public static final String PROPERTY_CONTEXT = "com.openexchange.exception.context";

    /**
     * The property name for request (without body).
     */
    public static final String PROPERTY_REQUEST = "com.openexchange.exception.request";

    /**
     * The property name for request body.
     */
    public static final String PROPERTY_REQUEST_BODY = "com.openexchange.exception.requestBody";

    /**
     * The property name for response (without body).
     */
    public static final String PROPERTY_RESPONSE = "com.openexchange.exception.response";

    /**
     * The property name for user's locale (instance of <tt>java.lang.String</tt>; e.g. <tt>"en_US"</tt>).
     */
    public static final String PROPERTY_LOCALE = "com.openexchange.exception.locale";

}
