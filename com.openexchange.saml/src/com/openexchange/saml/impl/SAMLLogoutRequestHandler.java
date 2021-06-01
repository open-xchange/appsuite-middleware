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

package com.openexchange.saml.impl;

import static com.openexchange.ajax.LoginServlet.getPublicSessionCookieName;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.LoginRequestContext;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.exception.OXException;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.saml.tools.SAMLLoginTools;
import com.openexchange.session.Session;


/**
 * Handles the logout of a user after the response for a SP-initiated single logout has been received.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLLogoutRequestHandler implements LoginRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLLogoutRequestHandler.class);

    private final SAMLBackend backend;

    private final LoginConfigurationLookup loginConfigurationLookup;

    public SAMLLogoutRequestHandler(SAMLBackend backend, LoginConfigurationLookup loginConfigurationLookup) {
        super();
        this.backend = backend;
        this.loginConfigurationLookup = loginConfigurationLookup;
    }

    @Override
    public void handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, LoginRequestContext requestContext) throws IOException {
        try {
            Session session = null;
            String sessionId = httpRequest.getParameter(LoginServlet.PARAMETER_SESSION);
            if (sessionId == null) {
                LOG.info("Missing session id in SAML logout request");
            } else {
                LOG.debug("Received logout request for session {}", sessionId);
                session = LoginPerformer.getInstance().lookupSession(sessionId);
            }

            if (session != null) {
                String hash = session.getHash();
                SAMLLoginTools.validateSession(httpRequest, session, hash, loginConfigurationLookup.getLoginConfiguration());
                LOG.debug("Performing logout for session {}", sessionId);
                LoginPerformer.getInstance().doLogout(sessionId);
                SessionUtility.removeOXCookies(httpRequest, httpResponse, Arrays.asList(LoginServlet.SESSION_PREFIX + hash,
                    LoginServlet.SHARD_COOKIE_NAME, LoginServlet.SECRET_PREFIX + hash,
                    getPublicSessionCookieName(httpRequest, new String[] { String.valueOf(session.getContextId()),
                    String.valueOf(session.getUserId()) }), LoginServlet.SESSION_PREFIX + hash));
                SessionUtility.removeJSESSIONID(httpRequest, httpResponse);
            }
        } catch (OXException e) {
            LOG.error("Logout failed", e);
            requestContext.getMetricProvider().recordException(e);
        }

        backend.finishLogout(httpRequest, httpResponse);
        if(requestContext.getMetricProvider().isStateUnknown()) {
            requestContext.getMetricProvider().recordSuccess();
        }
    }
}
