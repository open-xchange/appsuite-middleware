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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.login.handler;

import static com.openexchange.ajax.SessionUtility.getSession;
import static com.openexchange.ajax.SessionUtility.getSessionId;
import static com.openexchange.ajax.SessionUtility.verifySession;
import static com.openexchange.kerberos.KerberosUtils.SESSION_PRINCIPAL;
import static com.openexchange.kerberos.KerberosUtils.SESSION_SUBJECT;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.kerberos.ClientPrincipal;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.session.ServerSession;

/**
 * Implements a servlet action to refetch the kerberos ticket from the client again within a running session.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.0
 */
public final class KerberosTicketReload extends SessionServlet implements LoginRequestHandler {

    private static final long serialVersionUID = 3738282837792003027L;
    private static final Logger LOG = LoggerFactory.getLogger(KerberosTicketReload.class);

    private final SessiondService sessiondService;
    private final KerberosService kerberosService;

    public KerberosTicketReload(SessiondService sessiondService, KerberosService kerberosService) {
        super();
        this.sessiondService = sessiondService;
        this.kerberosService = kerberosService;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doAuthHeaderTicketReload(req, resp);
        } catch (OXException e) {
            LOG.error("", e);
            resp.addHeader("WWW-Authenticate", "NEGOTIATE");
            resp.addHeader("WWW-Authenticate", "Basic realm=\"Open-Xchange\"");
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    private void doAuthHeaderTicketReload(HttpServletRequest req, HttpServletResponse resp) throws OXException, IOException {
        final String sessionId = getSessionId(req);
        ServerSession session = getSession(req, sessionId, sessiondService);
        verifySession(req, sessiondService, sessionId, session);
        if (session.containsParameter(SESSION_PRINCIPAL)) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        final String auth = req.getHeader(Header.AUTH_HEADER);
        if (null == auth) {
            resp.addHeader("WWW-Authenticate", "NEGOTIATE");
            resp.addHeader("WWW-Authenticate", "Basic realm=\"Open-Xchange\"");
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
            return;
        }
        if (!Authorization.checkForAuthorizationHeader(auth)) {
            throw LoginExceptionCodes.UNKNOWN_HTTP_AUTHORIZATION.create("null");
        }
        if (!Authorization.checkForKerberosAuthorization(auth)) {
            throw LoginExceptionCodes.UNKNOWN_HTTP_AUTHORIZATION.create(Authorization.extractAuthScheme(auth));
        }

        final byte[] ticket = Base64.decode(auth.substring("Negotiate ".length()));
        ClientPrincipal principal = kerberosService.verifyAndDelegate(ticket);
        session.setParameter(SESSION_SUBJECT, principal.getDelegateSubject());
        session.setParameter(SESSION_PRINCIPAL, principal);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
