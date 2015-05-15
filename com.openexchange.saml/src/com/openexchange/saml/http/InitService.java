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

package com.openexchange.saml.http;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.saml.SAMLWebSSOProvider;
import com.openexchange.saml.spi.ExceptionHandler;
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

    private final SessiondService sessiondService;


    /**
     * Initializes a new {@link InitService}.
     * @param provider
     * @param exceptionHandler
     * @param sessiondService
     */
    public InitService(SAMLWebSSOProvider provider, ExceptionHandler exceptionHandler, SessiondService sessiondService) {
        super(provider, exceptionHandler);
        this.sessiondService = sessiondService;
    }

    @Override
    protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
        Tools.removeCachingHeader(httpResponse);

        String flow = httpRequest.getParameter("flow");
        if (flow == null) {
            LOG.debug("Received SAML init request without flow parameter");
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            String redirectURI;
            if (flow.equals("login") || flow.equals("relogin")) {
                redirectURI = provider.buildAuthnRequest(httpRequest, httpResponse);
            } else if (flow.equals("logout")) {
                String sessionId = httpRequest.getParameter("session");
                if (sessionId == null) {
                    LOG.debug("Received SAML init request without session parameter");
                    httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

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

}
