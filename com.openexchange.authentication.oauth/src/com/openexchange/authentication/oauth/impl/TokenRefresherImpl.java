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

package com.openexchange.authentication.oauth.impl;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
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
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.openexchange.authentication.oauth.http.OAuthAuthenticationHttpClientConfig;
import com.openexchange.exception.OXException;
import com.openexchange.nimbusds.oauth2.sdk.http.send.HTTPSender;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.oauth.OAuthTokens;
import com.openexchange.session.oauth.TokenRefreshResponse;
import com.openexchange.session.oauth.TokenRefreshResponse.ErrorType;
import com.openexchange.session.oauth.TokenRefresher;

/**
 * {@link TokenRefresherImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class TokenRefresherImpl extends OAuthRequestIssuer implements TokenRefresher {

    private static final Logger LOG = LoggerFactory.getLogger(TokenRefresherImpl.class);

    private final Session session;
    private final ServiceLookup services;

    public TokenRefresherImpl(Session session, OAuthAuthenticationConfig config, ServiceLookup services) {
        super(config);
        this.session = session;
        this.services = services;
    }

    @Override
    public TokenRefreshResponse execute(OAuthTokens currentTokens) throws OXException {
        if (!currentTokens.hasRefreshToken()) {
            return TokenRefreshResponse.MISSING_REFRESH_TOKEN;
        }

        //@formatter:off
        RefreshToken refreshToken = new RefreshToken(currentTokens.getRefreshToken());
        AuthorizationGrant authorizationGrant = new RefreshTokenGrant(refreshToken);
        TokenRequest request = new TokenRequest(config.getTokenEndpoint(),
                                                getClientAuthentication(),
                                                authorizationGrant);
        //@formatter:on
        LOG.debug("Sending refresh token request for session '{}'", session.getSessionID());

        try {
            TokenResponse response = TokenResponse.parse(HTTPSender.send(request.toHTTPRequest(), () -> {
                HttpClientService httpClientService = services.getOptionalService(HttpClientService.class);
                if (httpClientService == null) {
                    throw new IllegalStateException("Missing service " + HttpClientService.class.getName());
                }
                return httpClientService.getHttpClient(OAuthAuthenticationHttpClientConfig.getClientIdOAuthAuthentication());
            }));
            return validateResponse(response);
        } catch (com.nimbusds.oauth2.sdk.ParseException | IOException e) {
            LOG.info("Unable to refresh access token for user {} in context {}. Session will be invalidated.", I(session.getUserId()), I(session.getContextId()));
            LOG.trace("Refresh failed because of {}", e.getMessage(), e);
            TokenRefreshResponse.Error error = new TokenRefreshResponse.Error(ErrorType.TEMPORARY, "refresh_failed", e.getMessage());
            return new TokenRefreshResponse(error);
        }
    }

    private TokenRefreshResponse validateResponse(TokenResponse response) {
        if (!response.indicatesSuccess()) {
            TokenErrorResponse errorResponse = (TokenErrorResponse) response;
            LOG.debug("Got token error response to refresh request for session '{}'", session.getSessionID());

            ErrorObject error = errorResponse.getErrorObject();
            TokenRefreshResponse.Error rError;
            if (OAuth2Error.INVALID_GRANT.equals(error)) {
                rError = new TokenRefreshResponse.Error(ErrorType.INVALID_REFRESH_TOKEN, error.getCode(), error.getDescription());
            } else {
                rError = new TokenRefreshResponse.Error(ErrorType.TEMPORARY, error.getCode(), error.getDescription());
            }

            return new TokenRefreshResponse(rError);
        }

        AccessTokenResponse tokenResponse = (AccessTokenResponse) response;
        LOG.debug("Got success token response to refresh request for session '{}'", session.getSessionID());
        return new TokenRefreshResponse(convertNimbusTokens(tokenResponse.getTokens()));
    }

}
