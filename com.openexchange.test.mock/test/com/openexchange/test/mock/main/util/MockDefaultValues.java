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

package com.openexchange.test.mock.main.util;

import java.util.Locale;

/**
 * This class provides constants that should be used for all unit tests to have a correct and easy verification of the tests.<br>
 * <br>
 * Use the constants of this class instead of creating own variables for each test!
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class MockDefaultValues {

    /**
     * Default auth type
     */
    public static final String DEFAULT_AUTH_TYPE = "BASIC_AUTH";

    /**
     * Default any string
     */
    public static final String DEFAULT_ANY_STRING = "anyString";

    /**
     * Default client id
     */
    public static final String DEFAULT_CLIENT_ID = "theStringPropertyValue";

    /**
     * Default user agent
     */
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0";

    /**
     * Default auth id
     */
    public static final String DEFAULT_AUTH_ID = "26c0c2f28dec4beda128a042df46aa21";

    /**
     * Default client
     */
    public static final String DEFAULT_CLIENT = "open-xchange-appsuite";

    /**
     * Default jsessionid
     */
    public static final String DEFAULT_JSESSIONID = "5673333281539623060.OX0";

    /**
     * Default user id
     */
    public static final int DEFAULT_USER_ID = 307;

    /**
     * Default user login name
     */
    public static final String DEFAULT_USER_LOGIN_NAME = "martin.schneider";

    /**
     * Default user password
     */
    public static final String DEFAULT_USER_PASSWORD = "secret";

    /**
     * Default context id
     */
    public static final int DEFAULT_CONTEXT_ID = 424242669;

    /**
     * Default session id
     */
    public static final String DEFAULT_SESSION_ID = "8a07c5a2e4974a75ae70bd9a36198f03";

    /**
     * Default string property value
     */
    public static final String DEFAULT_STRING_PROPERTY_VALUE = "theStringPropertyValue";

    /**
     * Default property value
     */
    public static final int DEFAULT_INT_PROPERTY_VALUE = 100;

    /**
     * Default integer value
     */
    public static final int DEFAULT_INTEGER_VALUE = 999;

    /**
     * Default attribute value
     */
    public static final String DEFAULT_ATTRIBUTE_VALUE = "theAttributeValue";

    /**
     * Default property key
     */
    public static final String DEFAULT_PROPERTY_KEY = "thePropertyKey";

    /**
     * Default file name for yml files
     */
    public static final String DEFAULT_YML_FILENAME = "mockFileName.yml";

    /**
     * Default folder name
     */
    public static final String DEFAULT_FOLDERNAME = "theFolderName";

    /**
     * Default boolean as string
     */
    public static final String DEFAULT_FALSE_BOOLEAN_AS_STRING = "false";

    /**
     * Default character encoding
     */
    public static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";

    /**
     * Default content type
     */
    public static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";

    /**
     * Default locale
     */
    public static final Locale DEFAULT_LOCALE = Locale.US;

    /**
     * Default encoded url
     */
    public static final String DEFAULT_ENCODED_URL = "http://oxpedia.org/wiki/index.php?title=HTTP_API";

    /**
     * Default context path
     */
    public static final String DEFAULT_CONTEXT_AND_SERVLET_PATH = "/servlet";

    /**
     * Default host
     */
    public static final String DEFAULT_HOST = "127.0.0.1";

    /**
     * Default port
     */
    public static final int DEFAULT_PORT = 8080;

    /**
     * Default protocol
     */
    public static final String DEFAULT_PROTOCOL = "http";

    /**
     * Default method type post
     */
    public static final String DEFAULT_METHOD_TYPE_POST = "POST";

}
