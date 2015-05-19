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

package com.openexchange.oauth.provider.impl.servlets;

import static com.openexchange.osgi.Tools.requireService;
import static com.openexchange.tools.servlet.http.Tools.sendErrorPage;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientManagementException;
import com.openexchange.oauth.provider.client.Icon;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.scope.OAuthScopeProvider;
import com.openexchange.oauth.provider.scope.Scope;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;


/**
 * {@link AuthInfoEndpoint}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class AuthInfoEndpoint extends OAuthEndpoint {

    private static final long serialVersionUID = 5716238700714249548L;

    private static final Logger LOG = LoggerFactory.getLogger(AuthInfoEndpoint.class);

    private final ServiceLookup services;

    public AuthInfoEndpoint(OAuthProviderService oAuthProvider, ServiceLookup services) {
        super(oAuthProvider);
        this.services = services;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            handle(request, response);
        } catch (OXException e) {
            LOG.error("AuthInfo request failed", e);
            sendJSONError(request, response, e);
        }
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, OXException {
        try {
            Tools.disableCaching(response);
            String clientId = request.getParameter(OAuthProviderConstants.PARAM_CLIENT_ID);
            if (Strings.isEmpty(clientId)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "missing required parameter: " + OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            Client client = oAuthProvider.getClientManagement().getClientById(clientId);
            if (client == null) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameter value: " + OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            if (!client.isEnabled()) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameter value: " + OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            String scope = request.getParameter(OAuthProviderConstants.PARAM_SCOPE);
            if (Strings.isEmpty(scope)) {
                scope = client.getDefaultScope().toString();
            } else {
                // Validate scope
                if (!oAuthProvider.isValidScope(scope)) {
                    sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameter value: "+OAuthProviderConstants.PARAM_SCOPE);
                    return;
                }
            }

            if (isInvalidCSRFToken(request)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request contained no or invalid CSRF token. Ensure that cookies are allowed.");
                return;
            }

            Locale locale = determineLocale(request);
            JSONObject jClient = new JSONObject();
            jClient.put("name", client.getName());
            jClient.put("description", client.getDescription());
            jClient.put("contact_address", client.getContactAddress());
            jClient.put("website", client.getWebsite());
            Icon icon = client.getIcon();
            jClient.put("icon", icon2HTMLDataSource(icon));

            JSONObject jScopes = new JSONObject();
            Set<String> scopeTokens = Scope.parseScope(scope).get();
            Translator translator = requireService(TranslatorFactory.class, services).translatorFor(locale);
            for (String token : scopeTokens) {
                OAuthScopeProvider scopeProvider = oAuthProvider.getScopeProvider(token);
                String description;
                if (scopeProvider == null) {
                    LOG.warn("No scope provider available for token {}", token);
                    description = token;
                } else {
                    description = translator.translate(scopeProvider.getDescription());
                }
                jScopes.put(token, description);
            }

            JSONObject jResult = new JSONObject();
            jResult.put("client", jClient);
            jResult.put("scopes", jScopes);
            sendJSONResponse(request, response, jResult);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (ClientManagementException e) {
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
