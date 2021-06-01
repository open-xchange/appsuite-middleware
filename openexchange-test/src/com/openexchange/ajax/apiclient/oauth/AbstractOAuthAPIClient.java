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

package com.openexchange.ajax.apiclient.oauth;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.test.common.configuration.AJAXConfig;
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
public abstract class AbstractOAuthAPIClient extends AbstractAPIClientSession {

    protected static final boolean USE_PREFIX = false;

    protected OAuthApiClient oauthclient;
    private volatile AccessTokenResponse accessTokenResponse;

    @Override
    public void setUp() throws Exception {
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
            .add("scope", getScopes())
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

    public abstract String getScopes();

}
