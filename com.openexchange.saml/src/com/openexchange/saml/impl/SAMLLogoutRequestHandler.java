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

package com.openexchange.saml.impl;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.exception.OXException;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;


/**
 * {@link SAMLLogoutRequestHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLLogoutRequestHandler implements LoginRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLLogoutHandler.class);

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Session session = null;
            String sessionId = req.getParameter(AJAXServlet.PARAMETER_SESSION);
            if (sessionId == null) {
                LOG.info("Missing session id in SAML logout request");
            } else {
                session = LoginPerformer.getInstance().lookupSession(sessionId);
            }

            if (session != null) {
                LoginConfiguration conf = LoginServlet.getLoginConfiguration();
                SessionUtility.checkIP(conf.isIpCheck(), conf.getRanges(), session, req.getRemoteAddr(), conf.getIpCheckWhitelist());
                final String secret = SessionUtility.extractSecret(
                    conf.getHashSource(),
                    req,
                    session.getHash(),
                    session.getClient());

                if (secret != null && !session.getSecret().equals(secret)) {
                    LoginPerformer.getInstance().doLogout(sessionId);
                    // Drop relevant cookies
                    SessionUtility.removeOXCookies(session.getHash(), req, resp);
                    SessionUtility.removeJSESSIONID(req, resp);
                } else {
                    LOG.info("Missing or non-matching secret for session {}", sessionId);
                }
            }
        } catch (OXException e) {
            LOG.error("Logout failed", e);
        }

        resp.reset();
        resp.setContentType(null);
        Tools.disableCaching(resp);
        String redirectLocation = req.getParameter("location");
        if (redirectLocation == null) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.sendRedirect(redirectLocation);
        }
    }

}
