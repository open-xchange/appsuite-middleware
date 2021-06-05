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

package com.openexchange.ajax.oauth.provider;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.junit.Assert;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.oauth.provider.protocol.Grant;
import com.openexchange.ajax.oauth.provider.protocol.OAuthParams;
import com.openexchange.ajax.oauth.provider.protocol.Protocol;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * {@link OAuthSession}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthSession extends AJAXSession {

    private final String clientId;

    private final String clientSecret;

    private final String redirectURI;

    private final Scope scope;

    private Grant grant;

    /**
     * Initializes a new {@link OAuthSession}.
     */
    public OAuthSession(TestUser user, String clientId, String clientSecret, String redirectURI, Scope scope) {
        super(newWebConversation(), newOAuthHttpClient(), null);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectURI = redirectURI;
        this.scope = scope;
        try {
            AJAXConfig.init();
            obtainAccess(user, getHttpClient());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    public static DefaultHttpClient newOAuthHttpClient() {
        DefaultHttpClient client = newHttpClient();
        try {
            SSLSocketFactory ssf = new SSLSocketFactory(new TrustSelfSignedStrategy(), new AllowAllHostnameVerifier());
            client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, ssf));
        } catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            Assert.fail(e.getMessage());
        }

        HttpParams params = client.getParams();
        HttpClientParams.setRedirecting(params, false);
        HttpClientParams.setAuthenticating(params, false);
        return client;
    }

    private void obtainAccess(TestUser user, HttpClient client) throws Exception {
        String scheme = "https";
        String hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        int port = 443;
        String login = user.getLogin();
        String password = user.getPassword();
        String state = UUIDs.getUnformattedStringFromRandom();

        OAuthParams params = new OAuthParams()
            .setScheme(scheme)
            .setHostname(hostname)
            .setPort(port)
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRedirectURI(redirectURI)
            .setScope(scope.toString())
            .setState(state);
        grant = Protocol.obtainAccess(client, params, login, password);
    }

    /**
     * Gets the accessToken
     *
     * @return The accessToken
     */
    public String getAccessToken() {
        return grant.getAccessToken();
    }

    /**
     * Gets the refreshToken
     *
     * @return The refreshToken
     */
    public String getRefreshToken() {
        return grant.getRefreshToken();
    }

}
