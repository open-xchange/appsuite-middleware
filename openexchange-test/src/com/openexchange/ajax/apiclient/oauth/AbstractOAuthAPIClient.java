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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.apiclient.oauth;

import java.io.IOException;
import org.junit.Assert;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.ajax.framework.ProvisioningSetup;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * {@link AbstractOAuthAPIClient} is a subclass of the {@link AbstractAPIClientSession} which provides an oauth based api client
 *
 * This requires the following ajax properties to be configured: {@link AJAXConfig.Property#OAUTH_TOKEN_ENDPOINT}, {@link AJAXConfig.Property#OAUTH_CLIENT_ID} and {@link AJAXConfig.Property#OAUTH_CLIENT_PASSWORD}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.5
 */
public class AbstractOAuthAPIClient extends AbstractAPIClientSession {

    protected static final boolean USE_PREFIX = true;

    protected OAuthApiClient oauthclient;
    private volatile AccessTokenResponse accessTokenResponse;

    @Override
    public void setUp() throws Exception {
        ProvisioningSetup.init();
        testContext = TestContextPool.acquireContext(this.getClass().getCanonicalName());
        Assert.assertNotNull("Unable to retrieve a context!", testContext);
        testUser = testContext.acquireUser();
        testUser2 = testContext.acquireUser();
        oauthclient = generateOAuthClient();
    }

    /**
     * Gets the oauth based client
     *
     * @return The {@link ApiClient}
     */
    protected ApiClient getOAuthBasedClient() {
        return oauthclient;
    }

    /**
     * Generates an oauth client, which uses an provided oauth token to authenticate instead of login/password
     *
     * @return The {@link OAuthApiClient}
     * @throws OXException in case something went wrong
     */
    protected final OAuthApiClient generateOAuthClient() throws OXException {
        OAuthApiClient newClient;
        try {
            newClient = new OAuthApiClient(() -> "Bearer " + getToken());
            setBasePath(newClient);
            if (USE_PREFIX) {
                newClient.setBasePath(newClient.getBasePath() + "/oauth/modules");
            }
            newClient.setUserAgent("HTTP API Testing Agent");
        } catch (Exception e) {
            throw new OXException(e);
        }
        return newClient;
    }

    /**
     * Gets an access token from the configured token endpoint. Requires the "Resource Owner Password Credentials Grant" oauth flow.
     *
     * @return The access token
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     * @throws ApiException
     */
    private synchronized String getToken() throws JsonParseException, JsonMappingException, IOException, ApiException {
        if (accessTokenResponse != null) {
            return accessTokenResponse.getAccessToken();
        }

        OkHttpClient client = new OkHttpClient();
        // @formatter:off
        RequestBody formBody = new FormBody.Builder()
            .add("username", testUser.getUser())
            .add("password", testUser.getPassword())
            .add("grant_type", "password")
            .add("scope", "read_mails write_mails write_userconfig")
            .build();
        // @formatter:on

        String tokenEndpoint = AJAXConfig.getProperty(AJAXConfig.Property.OAUTH_TOKEN_ENDPOINT);
        String clientId = AJAXConfig.getProperty(AJAXConfig.Property.OAUTH_CLIENT_ID);
        String clientPW = AJAXConfig.getProperty(AJAXConfig.Property.OAUTH_CLIENT_PASSWORD);
        if (Strings.isEmpty(tokenEndpoint) || Strings.isEmpty(clientId) || Strings.isEmpty(clientPW)) {
            throw new ApiException("Invalid oauth configuration. Missing one of the following properties: " + AJAXConfig.Property.OAUTH_TOKEN_ENDPOINT + "," + AJAXConfig.Property.OAUTH_CLIENT_ID + "," + AJAXConfig.Property.OAUTH_CLIENT_PASSWORD);
        }

        // @formatter:off
        Request request = new Request.Builder()
                                     .url(tokenEndpoint)
                                     .addHeader("Authorization", Credentials.basic(clientId, clientPW))
                                     .post(formBody)
                                     .build();
        // @formatter:on

        Response response = client.newCall(request).execute();
        if (response.isSuccessful() == false) {
            throw new ApiException("Unable to get access token: " + response.body().string());
        }
        AccessTokenResponse resp = new ObjectMapper().readValue(response.body().string(), AccessTokenResponse.class);

        System.out.println("Received token: " + resp.getAccessToken());
        this.accessTokenResponse = resp;
        return resp.getAccessToken();
    }

}
