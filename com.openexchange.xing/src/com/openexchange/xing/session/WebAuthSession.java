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

package com.openexchange.xing.session;

import com.openexchange.xing.XingAPI;

/**
 * Keeps track of a logged in user contains configuration options for the {@link XingAPI}. This type of {@link Session} uses the web OAuth
 * flow to authenticate users:
 * <ol>
 * <li>A request token + secret and redirect URL are retrieved using {@link WebAuthSession#getAuthInfo()} or
 * {@link WebAuthSession#getAuthInfo(String)}.</li>
 * <li>You store the request token + secret, and redirect the user to the redirect URL where they will authenticate with XING and grant your
 * app permission to access their account.</li>
 * <li>XING will redirect back to your site if it was provided a URL to do so (otherwise, you have to ask the user when he/she is done).</li>
 * <li>The user's access token + secret are set on this session when you call
 * {@link WebAuthSession#retrieveWebAccessToken(RequestTokenPair)} with the previously-saved request token + secret. You have a limited
 * amount of time to make this call or the request token will expire.</li>
 * </ol>
 */
public class WebAuthSession extends AbstractSession {

    /**
     * Creates a new web auth session with the given app key pair and access type. The session will not be linked because it has no access
     * token or secret.
     */
    public WebAuthSession(AppKeyPair appKeyPair) {
        super(appKeyPair);
    }

    /**
     * Creates a new web auth session with the given app key pair and access type. The session will be linked to the account corresponding
     * to the given access token pair.
     */
    public WebAuthSession(AppKeyPair appKeyPair, AccessTokenPair accessTokenPair) {
        super(appKeyPair, accessTokenPair);
    }
    
    /**
     * Initializes a new {@link WebAuthSession} with the specified {@link ConsumerPair}. The session will be used to create a Xing
     * profile, based on the OX account (upsell).
     * 
     * @param appKeyPair the appKeyPair
     * @param consumerPair the ConsumerPair
     */
    public WebAuthSession(AppKeyPair appKeyPair, ConsumerPair consumerPair) {
        super(appKeyPair, consumerPair);
    }
    

}
