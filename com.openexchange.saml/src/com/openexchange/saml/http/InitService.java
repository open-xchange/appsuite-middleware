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

package com.openexchange.saml.http;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.SAMLSessionParameters;
import com.openexchange.saml.SAMLWebSSOProvider;
import com.openexchange.saml.impl.LoginConfigurationLookup;
import com.openexchange.saml.spi.ExceptionHandler;
import com.openexchange.saml.tools.SAMLLoginTools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Tools;


/**
 * The service to initiate SAML flows.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class InitService extends SAMLServlet {

    private static final Logger LOG = LoggerFactory.getLogger(InitService.class);

    private static final long serialVersionUID = -4022982444417155759L;

    private final SAMLConfig config;

    private final LoginConfigurationLookup loginConfigurationLookup;

    private final ServiceLookup services;


    /**
     * Initializes a new {@link InitService}.
     * @param config
     * @param provider
     * @param exceptionHandler
     * @param loginConfigurationLookup
     * @param services
     */
    public InitService(SAMLConfig config, SAMLWebSSOProvider provider, ExceptionHandler exceptionHandler, LoginConfigurationLookup loginConfigurationLookup,
        ServiceLookup services) {
        super(provider, exceptionHandler);
        this.config = config;
        this.loginConfigurationLookup = loginConfigurationLookup;
        this.services = services;
    }

    @Override
    protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
        Tools.disableCaching(httpResponse);

        String flow = httpRequest.getParameter("flow");
        if (flow == null) {
            LOG.debug("Received SAML init request without flow parameter");
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            // Create a new HttpSession if missing
            httpRequest.getSession(true);

            String redirectURI;
            if (flow.equals("login") || flow.equals("relogin")) {
                redirectURI = tryAutoLogin(httpRequest, httpResponse);
                if (redirectURI == null) {
                    redirectURI = provider.getStaticLoginRedirectLocation(httpRequest, httpResponse);
                }
                if (redirectURI == null) {
                    redirectURI = provider.buildAuthnRequest(httpRequest, httpResponse);
                }
            } else if (flow.equals("logout")) {
                String sessionId = httpRequest.getParameter("session");
                if (sessionId == null) {
                    LOG.debug("Received SAML init request without session parameter");
                    httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                SessiondService sessiondService = services.getService(SessiondService.class);
                Session session = sessiondService.getSession(sessionId);
                if (session == null) {
                    LOG.debug("Received SAML init request with invalid session parameter '{}'", sessionId);
                    httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                redirectURI = provider.buildLogoutRequest(httpRequest, httpResponse, session);
            } else {
                LOG.debug("Received SAML init request with unknown flow parameter '{}'", flow);
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String respondWithRedirect = httpRequest.getParameter("redirect");
            if (respondWithRedirect != null && Boolean.parseBoolean(respondWithRedirect)) {
                httpResponse.sendRedirect(redirectURI);
                return;
            }

            httpResponse.setStatus(HttpServletResponse.SC_OK);
            httpResponse.setCharacterEncoding(Charsets.UTF_8_NAME);
            httpResponse.setContentType("application/json");
            PrintWriter writer = httpResponse.getWriter();
            writer.write("{\"redirect_uri\":\"" + redirectURI + "\"}");
            writer.flush();
        } catch (OXException e) {
            LOG.error("Could not init SAML flow {}", flow, e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String tryAutoLogin(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        if (config.isAutoLoginEnabled()) {
            LoginConfiguration loginConfiguration = loginConfigurationLookup.getLoginConfiguration();
            Cookie sessionCookie = SAMLLoginTools.getSessionCookie(httpRequest, loginConfiguration);
            if (sessionCookie == null) {
                return null;
            }

            Session session = SAMLLoginTools.getSessionForSessionCookie(sessionCookie, services.getService(SessiondService.class));
            if (session == null) {
                // cookie exists but no according session was found => remove it
                Cookie toRemove = (Cookie) sessionCookie.clone();
                toRemove.setMaxAge(0);
                httpResponse.addCookie(toRemove);

                LOG.debug("Found no session for cookie '{}' with value '{}'", sessionCookie.getName(), sessionCookie.getValue());
                return null;
            }

            try {
                LOG.debug("Found session '{}' for open-xchange-session cookie '{}' with value '{}'", session.getSessionID(), sessionCookie.getName(), sessionCookie.getValue());
                Object parameter = session.getParameter(SAMLSessionParameters.SESSION_NOT_ON_OR_AFTER);
                if (null != parameter) {
                    long expiryDate = Long.parseLong((String) parameter);
                    if (System.currentTimeMillis() > expiryDate) {
                        LOG.debug("Session {} expired.", session.getSessionID());
                        services.getServiceSafe(SessiondService.class).removeSession(session.getSessionID());
                        return null;
                    }
                }
                String hash = HashCalculator.getInstance().getHash(httpRequest, LoginTools.parseUserAgent(httpRequest), LoginTools.parseClient(httpRequest, false, loginConfiguration.getDefaultClient()));
                SAMLLoginTools.validateSession(httpRequest, session, hash, loginConfiguration);
                String uiWebPath = httpRequest.getParameter(SAMLLoginTools.PARAM_LOGIN_PATH);
                if (Strings.isEmpty(uiWebPath)) {
                    uiWebPath = loginConfiguration.getUiWebPath();
                }
                return SAMLLoginTools.buildAbsoluteFrontendRedirectLocation(httpRequest, session, uiWebPath, services.getOptionalService(HostnameService.class));
            } catch (OXException e) {
                LOG.debug("Ignoring SAML auto-login attempt due to failed IP or secret check", e);
            }
        }

        return null;
    }

}
