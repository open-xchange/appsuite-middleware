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

package com.openexchange.oauth.api;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import com.openexchange.java.Strings;
import com.openexchange.oauth.AccessTokenSecretExtractor20;

/**
 * {@link MicrosoftGraphApi}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftGraphApi extends DefaultApi20 {

    private static final String STATIC_PARAMS = "response_type=code&response_mode=query";

    private static final String LOGIN_URL = "https://login.microsoftonline.com";

    private static final String COMMON_TENANT = "common";
    private static final String TOKEN_ENDPOINT = "oauth2/v2.0/token";
    private static final String AUTHORIZE_ENDPOINT = "oauth2/v2.0/authorize";

    // Authorise end-point variations
    private static final String BASE_AUTHORIZE_URL = LOGIN_URL + "/" + COMMON_TENANT + "/" + AUTHORIZE_ENDPOINT + "?" + STATIC_PARAMS;
    private static final String AUTHORIZE_URL = BASE_AUTHORIZE_URL + "&client_id=%s&redirect_uri=%s";
    private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s";

    // Token end-point
    private static final String TOKEN_URL = LOGIN_URL + "/" + COMMON_TENANT + "/" + TOKEN_ENDPOINT;

    /**
     * Initializes a new {@link MicrosoftGraphApi}.
     */
    public MicrosoftGraphApi() {
        super();
    }

    @Override
    public String getAccessTokenEndpoint() {
        return TOKEN_URL;
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return config.hasScope() ? String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()), OAuthEncoder.encode(config.getScope())) : String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new AccessTokenSecretExtractor20();
    }

    @Override
    public OAuthService createService(OAuthConfig config) {
        return new MicrosoftGraphService(this, config);
    }

    public static class MicrosoftGraphService extends OAuth20ServiceImpl {

        private final DefaultApi20 api;
        private final OAuthConfig config;

        /**
         * Initialises a new {@link MicrosoftGraphService}.
         * 
         * @param api
         * @param config
         */
        public MicrosoftGraphService(DefaultApi20 api, OAuthConfig config) {
            super(api, config);
            this.api = api;
            this.config = config;
        }

        @Override
        public Token getAccessToken(Token requestToken, Verifier verifier) {
            OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
            switch (api.getAccessTokenVerb()) {
                case POST:
                    request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
                    // API Secret is optional
                    if (config.getApiSecret() != null && config.getApiSecret().length() > 0) {
                        request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
                    }
                    if (requestToken == null) {
                        if (verifier == null || Strings.isEmpty(verifier.getValue())) {
                            throw new IllegalArgumentException("The verifier must neither be 'null' nor empty! To retrieve an 'authorization_code' an OAuth 'code' must be obtained first. Check your OAuth workflow!");
                        }
                        request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
                        request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                        request.addBodyParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.GRANT_TYPE_AUTHORIZATION_CODE);
                    } else {
                        request.addBodyParameter(OAuthConstants.REFRESH_TOKEN, requestToken.getSecret());
                        request.addBodyParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.GRANT_TYPE_REFRESH_TOKEN);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("The method '" + api.getAccessTokenVerb() + "' is invalid for this request. The OAuth workflow for Microsoft Graph API requires a POST method.");
            }
            Response response = request.send();
            return api.getAccessTokenExtractor().extract(response.getBody());
        }

        @Override
        public void signRequest(Token accessToken, OAuthRequest request) {
            request.addHeader("Authorization", "Bearer " + accessToken.getToken());
            request.addHeader("Accept", "application/json");
        }

        /**
         * Checks possible expiration for specified access token.
         *
         * @param accessToken The access token to validate
         * @return <code>true</code> if expired; otherwise <code>false</code> if valid
         */
        public boolean isExpired(String accessToken) {
            OAuthRequest request = new OAuthRequest(Verb.GET, "https://graph.microsoft.com/v1.0/me");
            signRequest(new Token(accessToken, ""), request);

            Response response = request.send();
            if (response.getCode() == 401 || response.getCode() == 400) {
                return true;
            }

            return false;
        }
    }
}
