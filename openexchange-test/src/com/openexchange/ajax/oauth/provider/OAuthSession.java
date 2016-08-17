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
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.oauth.provider.protocol.Grant;
import com.openexchange.ajax.oauth.provider.protocol.OAuthParams;
import com.openexchange.ajax.oauth.provider.protocol.Protocol;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;


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
    public OAuthSession(User user, String clientId, String clientSecret, String redirectURI, Scope scope) {
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

    private void obtainAccess(User user, HttpClient client) throws Exception {
        String hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        String login = AJAXConfig.getProperty(user.getLogin()) + "@" + AJAXConfig.getProperty(AJAXConfig.Property.CONTEXTNAME);
        String password = AJAXConfig.getProperty(user.getPassword());
        String state = UUIDs.getUnformattedStringFromRandom();

        OAuthParams params = new OAuthParams()
            .setHostname(hostname)
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
