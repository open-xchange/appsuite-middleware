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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.servlets;

import static com.openexchange.ajax.AJAXUtility.encodeUrl;
import static com.openexchange.tools.servlet.http.Tools.sendErrorResponse;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.Scope;

/**
 * {@link AuthorizationServlet2} - Authorization request handler for OAuth2.0.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AuthorizationServlet2 extends AbstractAuthorizationServlet {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AuthorizationServlet2.class);

    private static final long serialVersionUID = 6393806486708501254L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            String responseType = request.getParameter(OAuthProviderConstants.PARAM_RESPONSE_TYPE);
            if (Strings.isEmpty(responseType)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"missing required parameter: "+OAuthProviderConstants.PARAM_RESPONSE_TYPE+"\",\"error\":\"invalid_request\"}");
                return;
            }
            if (!"code".equals(responseType)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, new JSONObject(2).put("error_description", "response type not supported: " + responseType).put("error", "unsupported_response_type").toString());
                return;
            }

            String clientId = request.getParameter(OAuthProviderConstants.PARAM_CLIENT_ID);
            if (Strings.isEmpty(clientId)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"missing required parameter: "+OAuthProviderConstants.PARAM_CLIENT_ID+"\",\"error\":\"invalid_request\"}");
                return;
            }

            String state = request.getParameter(OAuthProviderConstants.PARAM_STATE);
            if (Strings.isEmpty(state)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"missing required parameter: "+OAuthProviderConstants.PARAM_STATE+"\",\"error\":\"invalid_request\"}");
                return;
            }

            String redirectUri = request.getParameter(OAuthProviderConstants.PARAM_REDIRECT_URI);
            if (Strings.isEmpty(redirectUri)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"missing required parameter: "+OAuthProviderConstants.PARAM_REDIRECT_URI+"\",\"error\":\"invalid_request\"}");
                return;
            }

            String sScope = request.getParameter(OAuthProviderConstants.PARAM_SCOPE);
            if (Strings.isEmpty(sScope)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"missing required parameter: "+OAuthProviderConstants.PARAM_SCOPE+"\",\"error\":\"invalid_request\"}");
                return;
            }

            OAuthProviderService providerService = getProviderService();
            if (null == providerService) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, new JSONObject(2).put("error_description", "missing required service").put("error", "server_error").toString());
                return;
            }

            Scope scope = providerService.validateScope(sScope);
            if (null == scope) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, new JSONObject(2).put("error_description", "invalid parameter value: " + OAuthProviderConstants.PARAM_SCOPE).put("error", "invalid_scope").toString());
                return;
            }

            if (false == providerService.validateClientId(clientId)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, new JSONObject(2).put("error_description", "invalid parameter value: " + OAuthProviderConstants.PARAM_CLIENT_ID).put("error", "invalid_request").toString());
                return;
            }

            if (false == providerService.validateRedirectUri(clientId, redirectUri)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, new JSONObject(2).put("error_description", "invalid parameter value: " + OAuthProviderConstants.PARAM_REDIRECT_URI).put("error", "invalid_request").toString());
                return;
            }

            // YOUR_REDIRECT_URI/?code=AUTHORIZATION_CODE&state=STATE

            String code = providerService.generateAuthorizationCodeFor(clientId, scope);

            StringBuilder builder = new StringBuilder(encodeUrl(redirectUri, true, false));
            char concat = '?';
            if (redirectUri.indexOf('?') >= 0) {
                concat = '&';
            }

            {
                builder.append(concat);
                concat = '&';
                builder.append(OAuthProviderConstants.PARAM_CODE).append('=').append(encodeUrl(code, true, true));
            }

            {
                builder.append(concat);
                builder.append(OAuthProviderConstants.PARAM_STATE).append('=').append(encodeUrl(state, true, true));
            }

            response.sendRedirect(builder.toString());
        } catch (OXException e) {
            LOGGER.error("Authorization request failed", e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"internal error\",\"error\":\"server_error\"}");
        } catch (JSONException e) {
            LOGGER.error("Authorization request failed", e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"internal error\",\"error\":\"server_error\"}");
        }
    }

}
