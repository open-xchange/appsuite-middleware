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

package com.openexchange.oidc.impl;

import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.openexchange.exception.OXException;
import com.openexchange.nimbusds.oauth2.sdk.http.send.HTTPSender;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.http.outbound.OIDCHttpClientConfig;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.session.Session;
import com.openexchange.session.oauth.OAuthTokens;
import com.openexchange.session.oauth.TokenRefreshResponse;
import com.openexchange.session.oauth.TokenRefreshResponse.ErrorType;
import com.openexchange.session.oauth.TokenRefresher;

public class OIDCTokenRefresher implements TokenRefresher {

    private static final Logger LOG = LoggerFactory.getLogger(OIDCTokenRefresher.class);

    private final OIDCBackend backend;
    private final Session session;

    /**
     * Initializes a new {@link OIDCTokenRefresher}.
     *
     * @param backend The ODIC back-end to use
     * @param session The session for which tokens are supposed to be refreshed
     */
    public OIDCTokenRefresher(OIDCBackend backend, Session session) {
        super();
        this.backend = backend;
        this.session = session;
    }

    @Override
    public TokenRefreshResponse execute(OAuthTokens currentTokens) throws OXException {
        if (!currentTokens.hasRefreshToken()) {
            LOG.debug("Cannot refresh OAuth tokens from session '{}' since no refresh token available", session.getSessionID());
            return TokenRefreshResponse.MISSING_REFRESH_TOKEN;
        }

        Object debugInfoForRefreshToken = OAuthTokens.getDebugInfoForRefreshToken(currentTokens);
        LOG.debug("Trying to refresh OAuth tokens from session '{}' using refresh token '{}'", session.getSessionID(), debugInfoForRefreshToken);

        RefreshToken refreshToken = new RefreshToken(currentTokens.getRefreshToken());
        AuthorizationGrant authorizationGrant = new RefreshTokenGrant(refreshToken);

        URI tokenEndpoint = OIDCTools.getURIFromPath(backend.getBackendConfig().getOpTokenEndpoint());
        TokenRequest request = new TokenRequest(tokenEndpoint,
                                                backend.getClientAuthentication(),
                                                authorizationGrant);

        LOG.debug("Sending refresh token request for session '{}' using refresh token '{}'", session.getSessionID(), debugInfoForRefreshToken);
        try {
            HTTPRequest httpRequest = backend.getHttpRequest(request.toHTTPRequest());
            TokenResponse response = TokenResponse.parse(HTTPSender.send(httpRequest, () -> {
                HttpClientService httpClientService = Services.getOptionalService(HttpClientService.class);
                if (httpClientService == null) {
                    throw new IllegalStateException("Missing service " + HttpClientService.class.getName());
                }
                return httpClientService.getHttpClient(OIDCHttpClientConfig.getClientIdOidc());
            }));
            return validateResponse(response, debugInfoForRefreshToken);
        } catch (Exception e) {
            LOG.info("Unable to refresh access token for user {} in context {}. Session '{}' will be invalidated.",
                I(session.getUserId()), I(session.getContextId()), session.getSessionID());
            TokenRefreshResponse.Error error = new TokenRefreshResponse.Error(ErrorType.TEMPORARY, "refresh_failed", e.getMessage());
            return new TokenRefreshResponse(error);
        }
    }

    private TokenRefreshResponse validateResponse(TokenResponse response, Object debugInfoForRefreshToken) {
        if (!response.indicatesSuccess()) {
            ErrorObject error = ((TokenErrorResponse) response).getErrorObject();
            TokenRefreshResponse.Error rError;
            if (OAuth2Error.INVALID_GRANT.equals(error)) {
                LOG.debug("Invalid refresh token from session '{}': {}", session.getSessionID(), debugInfoForRefreshToken);
                rError = new TokenRefreshResponse.Error(ErrorType.INVALID_REFRESH_TOKEN, error.getCode(), error.getDescription());
            } else {
                LOG.debug("Got token error response for refresh request for session '{}' using refresh token '{}'", session.getSessionID(), debugInfoForRefreshToken);
                rError = new TokenRefreshResponse.Error(ErrorType.TEMPORARY, error.getCode(), error.getDescription());
            }

            return new TokenRefreshResponse(rError);
        }

        AccessTokenResponse tokenResponse = (AccessTokenResponse) response;
        LOG.debug("Got successful token response for refresh request for session '{}' using refresh token '{}'", session.getSessionID(), debugInfoForRefreshToken);
        return new TokenRefreshResponse(OIDCTools.convertNimbusTokens(tokenResponse.getTokens()));
    }

}