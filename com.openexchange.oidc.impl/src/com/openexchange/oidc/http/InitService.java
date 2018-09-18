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
