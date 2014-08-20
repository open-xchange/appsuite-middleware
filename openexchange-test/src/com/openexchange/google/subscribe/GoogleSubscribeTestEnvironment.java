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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.google.subscribe;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.oauth.actions.InitOAuthAccountRequest;
import com.openexchange.ajax.oauth.actions.InitOAuthAccountResponse;
import com.openexchange.configuration.GoogleConfig;
import com.openexchange.configuration.GoogleConfig.Property;
import com.openexchange.exception.OXException;

/**
 * {@link GoogleSubscribeTestEnvironment}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GoogleSubscribeTestEnvironment {

    private static final GoogleSubscribeTestEnvironment INSTANCE = new GoogleSubscribeTestEnvironment();

    private static final String SERVICE_ID = "com.openexchange.oauth.google";

    private static final String ACCOUNT_NAME = "My Google account";

    private AJAXClient ajaxClient;

    private GoogleOAuthClient oauthClient;

    /**
     * Get the instance of the environment
     * 
     * @return the instance
     */
    public static final GoogleSubscribeTestEnvironment getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------- //

    /**
     * Initialize the test environment
     * 
     * @throws OXException
     */
    public void init() {
        try {
            GoogleConfig.init();
            initAJAXClient();
            initGoogleOAuthClient();
            initGoogleOAuthAccount();
            createGoogleSubscription();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clean up
     * 
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void cleanup() throws OXException, IOException, JSONException {
        logout();
    }

    /**
     * Initialize the client
     * 
     * @throws OXException
     * @throws JSONException
     * @throws IOException
     */
    private void initAJAXClient() throws OXException, IOException, JSONException {
        ajaxClient = new AJAXClient(User.User1);
    }

    /**
     * Initialize the google oauth client and perform a login
     * 
     * @throws Exception
     */
    private void initGoogleOAuthClient() throws Exception {
        oauthClient = new GoogleOAuthClient();
        oauthClient.login(GoogleConfig.getProperty(Property.EMAIL), GoogleConfig.getProperty(Property.PASSWORD));
    }

    /**
     * Initialize the google oauth account
     * 
     * @throws Exception
     */
    private void initGoogleOAuthAccount() throws Exception {
        final InitOAuthAccountRequest req = new InitOAuthAccountRequest(SERVICE_ID, ACCOUNT_NAME, true);
        final InitOAuthAccountResponse response = ajaxClient.execute(req);
        final Object data = response.getData();
        if (data instanceof JSONObject) {
            final JSONObject j = (JSONObject) data;
            final String redirectURI = fetchRedirectURI(j.getString("authUrl"));
        } else {
            throw new Exception("Invalid response body: " + data);
        }
    }

    private String fetchRedirectURI(final String authURL) throws Exception {
        String redirectURI = null;
        final String[] split = authURL.split("\\?");
        if (split.length != 2) {
            throw new Exception("Invalid authURL");
        }
        final String[] params = split[1].split("&");
        for (String p : params) {
            String[] sp = p.split("=");
            if (sp[0].equals("redirect_uri")) {
                redirectURI = sp[1];
                break;
            }
        }
        return redirectURI;
    }

    /**
     * Create a google subscription for the current user
     */
    private void createGoogleSubscription() {

    }

    /**
     * Logout the client
     * 
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    private void logout() throws OXException, IOException, JSONException {
        if (ajaxClient != null) {
            ajaxClient.logout();
        }
    }

}
