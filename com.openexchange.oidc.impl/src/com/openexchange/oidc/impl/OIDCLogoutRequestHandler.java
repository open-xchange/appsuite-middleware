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

package com.openexchange.oidc.impl;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.LoginServlet;
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
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        }

        this.backend.finishLogout(request, response);
    }
}
