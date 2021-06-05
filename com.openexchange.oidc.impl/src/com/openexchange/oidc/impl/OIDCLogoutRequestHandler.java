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

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.LoginRequestContext;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.exception.OXException;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.session.Session;

/**
 * {@link OIDCLogoutRequestHandler} Handles logout requests which terminate a valid
 * {@link Session}.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCLogoutRequestHandler implements LoginRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OIDCLogoutRequestHandler.class);
    private final OIDCBackend backend;

    public OIDCLogoutRequestHandler(OIDCBackend oidcBackend) {
        super();
        this.backend = oidcBackend;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, LoginRequestContext requestContext) throws IOException {
        try {
            String sessionId = request.getParameter(LoginServlet.PARAMETER_SESSION);
            if (sessionId == null) {
                LOG.info("Missing session id in OIDC logout request");
            } else {
                LOG.debug("Received logout request for session {}", sessionId);
                Session session = LoginPerformer.getInstance().lookupSession(sessionId);
                if (session != null) {
                    this.backend.logoutCurrentUser(session, request, response);
                } else {
                    LOG.info("Received logout request for a session that does not exist, session id: {}", sessionId);
                }
            }
        } catch (OXException e) {
            LOG.error("Failed to logout local OX session with id", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        this.backend.finishLogout(request, response);
        requestContext.getMetricProvider().recordSuccess();
    }
}
