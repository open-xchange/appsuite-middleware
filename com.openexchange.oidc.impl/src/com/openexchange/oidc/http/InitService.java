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

package com.openexchange.oidc.http;

import java.io.IOException;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.OIDCExceptionHandler;
import com.openexchange.oidc.OIDCWebSSOProvider;
import com.openexchange.oidc.tools.OIDCTools;

/**
 * The servlet to handle OpenID specific requests like login and logout.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class InitService extends OIDCServlet {

    private static final String LOGIN = "login";
    private static final String LOGOUT = "logout";
    private static final String THIRD_PARTY = "thirdParty";

    private static final Logger LOG = LoggerFactory.getLogger(InitService.class);
    private static final long serialVersionUID = -7066156332544428369L;

    private static final Set<String> acceptedFlows = ImmutableSet.of(LOGIN, LOGOUT, THIRD_PARTY);

    public InitService(OIDCWebSSOProvider provider, OIDCExceptionHandler exceptionHandler) {
        super(provider, exceptionHandler);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String flow = request.getParameter("flow");
        if (!validateFlow(flow)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            // Create a new HttpSession if missing
            request.getSession(true);

            String redirectURI = this.getRedirectURI(flow, request, response);
            OIDCTools.buildRedirectResponse(response, redirectURI, request.getParameter("redirect"));
        } catch (OXException e) {
            if (e.getExceptionCode() == OIDCExceptionCode.INVALID_LOGOUT_REQUEST || e.getExceptionCode() == OIDCExceptionCode.INVALID_THIRDPARTY_LOGIN_REQUEST) {
                LOG.error(e.getLocalizedMessage(), e);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else if (e.getExceptionCode() == OIDCExceptionCode.UNABLE_TO_PARSE_SESSIONS_IDTOKEN) {
                LOG.warn("Unable to logout user via oidc roundtrip, because of an invalid IDToken: {}", e.getLocalizedMessage());
                this.exceptionLogout(request, response);
            } else {
                exceptionHandler.handleResponseException(request, response, e);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String flow = request.getParameter("flow");
        if (!flow.equals(THIRD_PARTY) || !provider.validateThirdPartyRequest(request)) {
            LOG.warn("Either wrong flow or unkown issuer in POST request of third-party login initiation attempt.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            // Create a new HttpSession if missing
            request.getSession(true);

            String redirectURI = provider.getLoginRedirectRequest(request, response);
            OIDCTools.buildRedirectResponse(response, redirectURI, request.getParameter("redirect"));
        } catch (OXException e) {
            exceptionHandler.handleResponseException(request, response, e);
        }
    }

    private void exceptionLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.trace("exceptionLogout(HttpServletRequest request: {}, HttpServletResponse response)", request.getRequestURI());
        try {
            this.provider.logoutInCaseOfError(request.getParameter(LoginServlet.PARAMETER_SESSION), request, response);
        } catch (OXException e) {
            exceptionHandler.handleLogoutFailed(request, response, e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    private String getRedirectURI(String flow, HttpServletRequest request, HttpServletResponse response) throws OXException {
        LOG.trace("getRedirectURI(String flow: {}, HttpServletRequest request: {}, HttpServletResponse response)", flow, request.getRequestURI());
        String redirectUri = "";
        if (flow.equals(THIRD_PARTY) && !provider.validateThirdPartyRequest(request)) {
            throw OIDCExceptionCode.INVALID_THIRDPARTY_LOGIN_REQUEST.create("Issuer is unknown to the backend.");
        }

        if (flow.equals(LOGIN) || flow.equals(THIRD_PARTY)) {
            redirectUri = provider.getLoginRedirectRequest(request, response);
        } else if (flow.equals(LOGOUT)) {
            redirectUri = provider.getLogoutRedirectRequest(request, response);
        }
        return redirectUri;
    }

    private boolean validateFlow(String flow) {
        LOG.trace("validateFlow(String flow: {})", flow);
        boolean isValid = true;
        if (flow == null) {
            LOG.debug("OpenID flow parameter not set");
            isValid = false;
        } else if (!acceptedFlows.contains(flow)) {
            LOG.debug("OpenID flow parameter unknown, valid parameters are: {}. Input is: {}", acceptedFlows, flow);
            isValid = false;
        }
        return isValid;
    }
}
