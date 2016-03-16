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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
