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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.DefaultIcon;
import com.openexchange.oauth.provider.DefaultScopes;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientData;
import com.openexchange.oauth.provider.client.ClientManagement;
import com.openexchange.oauth.provider.rmi.RemoteClientManagement;
import com.openexchange.oauth2.utils.OAuthTestUtils;

/**
 * {@link EndpointTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class EndpointTest {

    protected static final String AUTHORIZATION_ENDPOINT = "/ajax/" + OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS;

    protected static final String TOKEN_ENDPOINT = "/ajax/" + OAuthProviderConstants.ACCESS_TOKEN_SERVLET_ALIAS;

    protected static final String REVOKE_ENDPOINT = "/ajax/" + OAuthProviderConstants.REVOKE_SERVLET_ALIAS;

    protected static String hostname;
    protected static String login;
    protected static String password;

    protected DefaultHttpClient client;

    protected Client oauthClient;

    @BeforeClass
    public static void beforeClass() throws OXException {
        AJAXConfig.init();
        hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        login = AJAXConfig.getProperty(User.User1.getLogin()) + "@" + AJAXConfig.getProperty(AJAXConfig.Property.CONTEXTNAME);
        password = AJAXConfig.getProperty(User.User1.getPassword());
    }

    @Before
    public void before() throws Exception {
        // prepare http client
        client = new DefaultHttpClient(new PoolingClientConnectionManager());
        HttpParams params = client.getParams();
        int minute = 1 * 60 * 1000;
        HttpConnectionParams.setConnectionTimeout(params, minute);
        HttpConnectionParams.setSoTimeout(params, minute);
        HttpClientParams.setRedirecting(params, false);

        SSLSocketFactory ssf = new SSLSocketFactory(new TrustSelfSignedStrategy(), new AllowAllHostnameVerifier());
        client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, ssf));

        // register client application
        ClientData clientData = prepareClient("Test App " + System.currentTimeMillis());
        RemoteClientManagement clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
        oauthClient = clientManagement.registerClient(ClientManagement.DEFAULT_GID, clientData, AbstractOAuthTest.getMasterAdminCredentials());
    }

    @After
    public void after() throws Exception {
        RemoteClientManagement clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
        clientManagement.unregisterClient(oauthClient.getId(), AbstractOAuthTest.getMasterAdminCredentials());
    }

    protected void expectSecureRedirect(HttpUriRequest request, HttpResponse response) {
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, response.getStatusLine().getStatusCode());
        Header location = response.getFirstHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        assertEquals("https://" + request.getURI().toString().substring(7), location.getValue());
    }

    protected HttpResponse executeAndConsume(HttpUriRequest request) throws ClientProtocolException, IOException {
        HttpResponse response = client.execute(request);
        EntityUtils.consumeQuietly(response.getEntity());
        return response;
    }

    protected String getScopes() {
        return oauthClient.getDefaultScope().scopeString();
    }

    protected String getClientId() {
        return oauthClient.getId();
    }

    protected String getClientSecret() {
        return oauthClient.getSecret();
    }

    protected String getRedirectURI() {
        return oauthClient.getRedirectURIs().get(0);
    }

    protected String getSecondRedirectURI() {
        return oauthClient.getRedirectURIs().get(1);
    }

    protected static ClientData prepareClient(String name) {
        DefaultIcon icon = new DefaultIcon();
        icon.setData(IconBytes.DATA);
        icon.setMimeType("image/jpg");

        Set<String> redirectURIs = new HashSet<>();
        redirectURIs.add("http://localhost");
        redirectURIs.add("http://localhost:8080");

        ClientData clientData = new ClientData();
        clientData.setName(name);
        clientData.setDescription(name);
        clientData.setIcon(icon);
        clientData.setContactAddress("webmaster@example.com");
        clientData.setWebsite("http://www.example.com");
        clientData.setDefaultScope(new DefaultScopes("read_contacts"));
        clientData.setRedirectURIs(redirectURIs);
        return clientData;
    }

    protected static void assertNoAccess(OAuthClient client) throws Exception {
        boolean error = false;
        try {
            client.assertAccess();
        } catch (AssertionError e) {
            error = true;
        }

        assertTrue("API access was possible although it should not", error);
    }

    protected URIBuilder prepareAuthenticationRequest(String redirectLocation) throws URISyntaxException {
        return prepareAuthenticationRequest(redirectLocation, false, null);
    }

    protected URIBuilder prepareAuthenticationRequest(String redirectLocation, boolean omitParam, String param) throws URISyntaxException {
        Map<String, String> redirectParams = OAuthTestUtils.extractRedirectParamsFromFragment(redirectLocation);
        redirectParams.put("user_login", login);
        redirectParams.put("user_password", password);
        redirectParams.put("access_denied", "false");

        URIBuilder uriBuilder = new URIBuilder().setScheme("https").setHost(hostname).setPath("/ajax/o/oauth2/authorization");

        for (Entry<String, String> entry : redirectParams.entrySet()) {
            if (entry.getKey().equals(param)) {
                if (!omitParam) {
                    uriBuilder.addParameter(entry.getKey(), "invalid");
                }
            } else {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        return uriBuilder;
    }

    protected URI prepareAuthorizationRequest(String csrfState) throws URISyntaxException {
        URIBuilder getLoginFormBuilder = new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath("/ajax/o/oauth2/authorization")
            .setParameter("response_type", "code")
            .setParameter("client_id", getClientId())
            .setParameter("redirect_uri", getRedirectURI())
            .setParameter("state", csrfState);
        if (getScopes() != null) {
            getLoginFormBuilder.setParameter("scope", new DefaultScopes(getScopes()).scopeString());
        }
        return getLoginFormBuilder.build();
    }
}
