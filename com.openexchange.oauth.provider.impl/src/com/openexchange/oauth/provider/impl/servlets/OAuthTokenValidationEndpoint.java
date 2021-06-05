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

package com.openexchange.oauth.provider.impl.servlets;

import static com.openexchange.tools.servlet.http.Tools.sendErrorResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.oauth.provider.authorizationserver.spi.AuthorizationException;
import com.openexchange.oauth.provider.authorizationserver.spi.OAuthAuthorizationService;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.impl.OAuthProviderConstants;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.http.Tools;


/**
 * {@link OAuthTokenValidationEndpoint} - End-point for validating a token.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public abstract class OAuthTokenValidationEndpoint extends OAuthEndpoint {

    private static final long serialVersionUID = 1337205004658187201L;

    private static final Logger LOG = LoggerFactory.getLogger(OAuthTokenValidationEndpoint.class);

    // ------------------------------------------------------------------------------------------------------------------

    protected final OAuthAuthorizationService oauthAuthService;

    protected OAuthTokenValidationEndpoint(OAuthAuthorizationService oauthAuthService, ClientManagement clientManagement, GrantManagement grantManagement, ServiceLookup services) {
        super(clientManagement, grantManagement, services);
        this.oauthAuthService = oauthAuthService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws IOException {
        try {
            Tools.disableCaching(resp);
            if (isNotSecureEndpoint(request, resp)) {
                return;
            }

            String accessToken = request.getParameter(OAuthProviderConstants.PARAM_ACCESS_TOKEN);
            if (accessToken == null) {
                failWithMissingParameter(resp, OAuthProviderConstants.PARAM_ACCESS_TOKEN);
                return;
            }

            Map<String, String> parameters;
            {
                Set<String> requiredParameters = requiredQueryParameters();
                int size;
                if (null == requiredParameters || (size = requiredParameters.size()) <= 0) {
                    parameters = Collections.emptyMap();
                } else {
                    parameters = new LinkedHashMap<>(size);
                    for (String name : requiredParameters) {
                        String value = request.getParameter(name);
                        if (null == value) {
                            failWithMissingParameter(resp, name);
                            return;
                        }
                        parameters.put(name, value);
                    }
                }
            }

            ValidationResponse response;
            try {
                response = oauthAuthService.validateAccessToken(accessToken);
            } catch (AuthorizationException e) {
                throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, "An error occurred while trying to validate an access token.");
            } catch (java.lang.IllegalArgumentException e) {
                LOG.debug("", e);
                handleMalformedToken(accessToken, request, resp);
                return;
            }

            switch (response.getTokenStatus()) {
                case MALFORMED:
                    handleMalformedToken(accessToken, request, resp);
                    return;
                case UNKNOWN:
                    handleUnknownToken(accessToken, request, resp);
                    return;
                case EXPIRED:
                    handleExpiredToken(accessToken, request, resp);
                    return;
                case VALID:
                    handleValidToken(accessToken, parameters, request, resp);
                    return;
                default:
                    handleUnknownToken(accessToken, request, resp);
                    return;
            }
        } catch (Exception e) {
            LOG.error("Token validation failed", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "{\"error_description\":\"internal error\",\"error\":\"server_error\"}");
        }
    }

    /**
     * Gets the names of required query parameters (beside <code>"access_token"</code>) to check for.
     *
     * @return The required query parameters' names
     */
    protected abstract Set<String> requiredQueryParameters();

    /**
     * Handles the {@link ValidationResponse.TokenStatus.MALFORMED malformed} validation response for specified token.
     *
     * @param accessToken The malformed access token
     * @param request The HTTP request
     * @param resp The HTTP response
     * @throws Exception If anything goes wrong here
     */
    protected abstract void handleMalformedToken(String accessToken, HttpServletRequest request, HttpServletResponse resp) throws Exception;

    /**
     * Handles the {@link ValidationResponse.TokenStatus.UNKNOWN unknown} validation response for specified token.
     *
     * @param accessToken The unknown access token
     * @param request The HTTP request
     * @param resp The HTTP response
     * @throws Exception If anything goes wrong here
     */
    protected abstract void handleUnknownToken(String accessToken, HttpServletRequest request, HttpServletResponse resp) throws Exception;

    /**
     * Handles the {@link ValidationResponse.TokenStatus.EXPIRED expired} validation response for specified token.
     *
     * @param accessToken The expired access token
     * @param request The HTTP request
     * @param resp The HTTP response
     * @throws Exception If anything goes wrong here
     */
    protected abstract void handleExpiredToken(String accessToken, HttpServletRequest request, HttpServletResponse resp) throws Exception;

    /**
     * Handles the {@link ValidationResponse.TokenStatus.VALID valid} validation response for specified token.
     *
     * @param accessToken The valid access token
     * @param parameters The required parameters (except <code>"access_token"</code>)
     * @param request The HTTP request
     * @param resp The HTTP response
     * @throws Exception If anything goes wrong here
     */
    protected abstract void handleValidToken(String accessToken, Map<String, String> parameters, HttpServletRequest request, HttpServletResponse resp) throws Exception;

}
