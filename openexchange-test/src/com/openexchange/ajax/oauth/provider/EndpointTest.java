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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractTestEnvironment;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.provider.impl.OAuthProviderConstants;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.oauth.provider.rmi.client.ClientDataDto;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.IconDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.AJAXConfig.Property;
import com.openexchange.test.common.test.pool.TestContext;
import com.openexchange.test.common.test.pool.TestContextPool;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * {@link EndpointTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@RunWith(ConcurrentTestRunner.class)
public abstract class EndpointTest extends AbstractTestEnvironment {

    public static final String AUTHORIZATION_ENDPOINT = "/ajax/" + OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS;

    public static final String TOKEN_ENDPOINT = "/ajax/" + OAuthProviderConstants.ACCESS_TOKEN_SERVLET_ALIAS;

    public static final String REVOKE_ENDPOINT = "/ajax/" + OAuthProviderConstants.REVOKE_SERVLET_ALIAS;

    protected CloseableHttpClient client;

    protected ClientDto oauthClient;

    protected String csrfState;

    protected TestUser testUser;

    protected final static String SCHEME =  "https";

    protected final static String HOSTNAME = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);

    protected final static int PORT = 443;

    protected TestContext testContext;

    protected AJAXClient noReplyClient;

    protected TestUser noReplyUser;

    @Before
    public void before() throws Exception {
        testContext = TestContextPool.acquireContext(this.getClass().getCanonicalName());
        testUser = testContext.acquireUser();
        //noReplyUser = testContext.getNoReplyUser();
        //noReplyClient = new AJAXClient(noReplyUser);
        //noReplyClient.execute(new ClearMailsRequest());
        // prepare http client
        // prepare new httpClient
        SSLContext sslcontext = new SSLContextBuilder().loadTrustMaterial(null, TrustSelfSignedStrategy.INSTANCE).build();
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslsf)
                .build();

        int minute = 1 * 60 * 1000;
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(minute)
            .setConnectionRequestTimeout(minute)
            .setSocketTimeout(minute)
            .build();

        client = HttpClients
            .custom()
            .disableRedirectHandling()
            .setDefaultRequestConfig(config)
            .setConnectionManager(new PoolingHttpClientConnectionManager(socketFactoryRegistry))
            .build();


          // register client application
          ClientDataDto clientData = prepareClient("Test App " + UUID.randomUUID().toString());
          RemoteClientManagement clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMIHOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
          oauthClient = clientManagement.registerClient(RemoteClientManagement.DEFAULT_GID, clientData, AbstractOAuthTest.getMasterAdminCredentials());

          csrfState = UUIDs.getUnformattedStringFromRandom();

    }

    @After
    public void after() throws Exception {
        try {
            if (client != null && client.getConnectionManager() != null) {
                client.close();
            }
            RemoteClientManagement clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMIHOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
            clientManagement.unregisterClient(oauthClient.getId(), AbstractOAuthTest.getMasterAdminCredentials());
        } finally {
            TestContextPool.backContext(testContext);
        }
    }

    protected void expectSecureRedirect(HttpUriRequest request, HttpResponse response) {
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, response.getStatusLine().getStatusCode());
        Header location = response.getFirstHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        assertEquals("https://" + request.getURI().toString().substring(7), location.getValue());
    }

    protected HttpResponse executeAndConsume(HttpRequestBase request) throws ClientProtocolException, IOException {
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            EntityUtils.consumeQuietly(entity);
        }
        return response;
    }

    protected Scope getScope() {
        return Scope.parseScope(oauthClient.getDefaultScope());
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

    protected static ClientDataDto prepareClient(String name) {
        IconDto icon = new IconDto();
        icon.setData(IconBytes.DATA);
        icon.setMimeType("image/jpg");

        List<String> redirectURIs = new ArrayList<>(2);
        redirectURIs.add("http://localhost");
        redirectURIs.add("http://localhost:8080");

        ClientDataDto clientData = new ClientDataDto();
        clientData.setName(name);
        clientData.setDescription(name);
        clientData.setIcon(icon);
        clientData.setContactAddress("webmaster@example.com");
        clientData.setWebsite("http://www.example.com");
        clientData.setDefaultScope("read_contacts");
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

}
