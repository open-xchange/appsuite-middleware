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
    public void handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
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
                SessionUtility.removeOXCookies(httpRequest, httpResponse, Arrays.asList(LoginServlet.SESSION_PREFIX + hash, LoginServlet.SECRET_PREFIX + hash, getPublicSessionCookieName(httpRequest, new String[] { String.valueOf(session.getContextId()), String.valueOf(session.getUserId()) }), SAMLLoginTools.AUTO_LOGIN_COOKIE_PREFIX + hash));
                SessionUtility.removeJSESSIONID(httpRequest, httpResponse);
            }
        } catch (OXException e) {
            LOG.error("Logout failed", e);
        }

        backend.finishLogout(httpRequest, httpResponse);
    }

}
