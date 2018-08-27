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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.oauth.api;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.scribe.builder.api.DefaultApi20#getAccessTokenEndpoint()
     */
    @Override
    public String getAccessTokenEndpoint() {
        return TOKEN_URL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.scribe.builder.api.DefaultApi20#getAuthorizationUrl(org.scribe.model.OAuthConfig)
     */
    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return config.hasScope() ? String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()), OAuthEncoder.encode(config.getScope())) : String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.scribe.builder.api.DefaultApi20#getAccessTokenVerb()
     */
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.scribe.builder.api.DefaultApi20#getAccessTokenExtractor()
     */
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new JsonTokenExtractor();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.scribe.builder.api.DefaultApi20#createService(org.scribe.model.OAuthConfig)
     */
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
                        request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
                        request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                        request.addBodyParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.GRANT_TYPE_AUTHORIZATION_CODE);
                    } else {
                        request.addBodyParameter(OAuthConstants.REFRESH_TOKEN, requestToken.getSecret());
                        request.addBodyParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.GRANT_TYPE_REFRESH_TOKEN);
                    }
                    break;
                case GET:
                default:
                    request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
                    // API Secret is optional
                    if (config.getApiSecret() != null && config.getApiSecret().length() > 0) {
                        request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
                    }
                    request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue());
                    request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                    if (config.hasScope()) {
                        request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope());
                    }
            }
            Response response = request.send();
            return api.getAccessTokenExtractor().extract(response.getBody());
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.scribe.oauth.OAuth20ServiceImpl#signRequest(org.scribe.model.Token, org.scribe.model.OAuthRequest)
         */
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
