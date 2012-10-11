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

package com.openexchange.messaging.facebook;

import java.lang.reflect.Field;
import org.apache.commons.httpclient.HttpClient;
import com.gargoylesoftware.htmlunit.HttpWebConnection;

/**
 * {@link FacebookConstants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookConstants {

    /**
     * The configuration property name for facebook API key.
     */
    public static final String FACEBOOK_API_KEY = "apiKey";

    /**
     * The configuration property name for facebook secret key.
     */
    public static final String FACEBOOK_SECRET_KEY = "secretKey";

    /**
     * The default API key.
     */
    public static final String KEY_API = "d36ebc9e274a89e3bd0c239cea4acb48";

    /**
     * The default secret key.
     */
    public static final String KEY_SECRET = "903e8006dbad9204bb74c26eb3ca2310";

    /**
     * The type for update status.
     */
    public static final String TYPE_UPDATE_STATUS = "updateStatus";

    /**
     * The type for posting on a user's wall.
     */
    public static final String TYPE_POST = "post";

    /**
     * The HTTP client field which accesses "httpClient_" in class {@link HttpWebConnection}.
     */
    public static volatile Field HTTP_CLIENT_FIELD;

    /**
     * The connection manager field which accesses "httpConnectionManager" in class {@link HttpClient}.
     */
    public static volatile Field CONNECTION_MANAGER_FIELD;

    /**
     * The folder identifier for a user's wall posts.
     */
    public static final String FOLDER_WALL = "wall";

    /**
     * The constant for account.
     */
    public static final String FACEBOOK_OAUTH_ACCOUNT = "account";

    /**
     * Initializes facebook constants.
     *
     * @throws Exception
     */
    public static void init() throws Exception {
        Field hcf = null;
        Field cmf = null;
        try {
            hcf = HttpWebConnection.class.getDeclaredField("httpClient_");
            hcf.setAccessible(true);
            cmf = HttpClient.class.getDeclaredField("httpConnectionManager");
            cmf.setAccessible(true);
        } catch (final SecurityException e) {
            // Cannot occur
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FacebookConstants.class)).error(e.getMessage(), e);
        } catch (final NoSuchFieldException e) {
            // Cannot occur
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FacebookConstants.class)).error(e.getMessage(), e);
        }
        HTTP_CLIENT_FIELD = hcf;
        CONNECTION_MANAGER_FIELD = cmf;
    }

    /**
     * Drops facebook constants.
     */
    public static void drop() {
        HTTP_CLIENT_FIELD = null;
        CONNECTION_MANAGER_FIELD = null;
    }

    /**
     * Initializes a new {@link FacebookConstants}.
     */
    private FacebookConstants() {
        super();
    }

}
