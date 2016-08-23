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

package com.openexchange.oauth.dropbox.v1;

import java.io.IOException;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.Session.AccessType;
import com.openexchange.oauth.dropbox.OAuthToken;
import com.dropbox.client2.session.WebAuthSession;

/**
 * {@link OAuth} tests the OAuth 1.0 authorisation workflow
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuth {

    /**
     * Simple OAuth 1.0 test
     */
    public static void main(String[] args) throws DropboxException, IOException {
        if (args.length == 0) {
            System.err.println("You must specify the API key and API secret");
            System.exit(-1);
        }
        String apiKey = args[0];
        String apiSecret = args[1];

        OAuth oauth = new OAuth(apiKey, apiSecret);
        oauth.authorise();
    }

    private String apiKey;
    private String apiSecret;

    /**
     * Initialises a new {@link OAuth}.
     * 
     * @param apiKey
     * @param apiSecret
     */
    public OAuth(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    /**
     * Perform OAuth 1.0
     * 
     * @throws DropboxException if a Dropbox error occurs
     * @throws IOException if an I/O error occurs
     */
    public OAuthToken authorise() throws DropboxException, IOException {
        // Use the API key and secret to get an authorisation URL
        DropboxAPI<WebAuthSession> dropboxAPI = getAPI(apiKey, apiSecret, AccessType.DROPBOX);
        String authURL = dropboxAPI.getSession().getAuthInfo().url;
        System.err.println("\n ** EXECUTE in browser and ACCEPT, then press any key to continue... ** \n\n" + authURL);

        // Waiting for user input
        System.in.read();

        // Once accepted, complete OAuth inititialisation by retrieving the user specific key/secret token pair
        AccessTokenPair tokenPair = dropboxAPI.getSession().getAccessTokenPair();
        RequestTokenPair tokens = new RequestTokenPair(tokenPair.key, tokenPair.secret);
        dropboxAPI.getSession().retrieveWebAccessToken(tokens);
        
        String tokenKey = dropboxAPI.getSession().getAccessTokenPair().key;
        String tokenSecret = dropboxAPI.getSession().getAccessTokenPair().secret;

        return new OAuthToken(tokenKey, tokenSecret);
    }

    /**
     * Create a {@link DropboxAPI} instance with the specified API key/secret and {@link AccessType}
     * 
     * @param key The API key
     * @param secret The API secret
     * @param accessType The {@link AccessType}
     * @return The {@link DropboxAPI}
     */
    public DropboxAPI<WebAuthSession> getAPI(String key, String secret, AccessType accessType) {
        AppKeyPair keyPair = new AppKeyPair(key, secret);
        WebAuthSession session = new TrustAllWebAuthSession(keyPair, accessType);
        return new DropboxAPI<WebAuthSession>(session);
    }
}
