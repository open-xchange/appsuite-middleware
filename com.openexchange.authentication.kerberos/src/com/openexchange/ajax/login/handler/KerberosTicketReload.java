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

package com.openexchange.ajax.login.handler;

import static com.openexchange.ajax.SessionUtility.getSession;
import static com.openexchange.ajax.SessionUtility.getSessionId;
import static com.openexchange.ajax.SessionUtility.verifySession;
import static com.openexchange.kerberos.KerberosUtils.SESSION_PRINCIPAL;
import static com.openexchange.kerberos.KerberosUtils.SESSION_SUBJECT;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.login.LoginRequestContext;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.kerberos.ClientPrincipal;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.kerberos.KerberosUtils;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.SessionResult;
import com.openexchange.sessiond.ExpirationReason;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.session.ServerSession;

/**
 * Implements a servlet action to refetch the kerberos ticket from the client again within a running session.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.1
 */
public final class KerberosTicketReload extends SessionServlet implements LoginRequestHandler {

    private static final long serialVersionUID = 3738282837792003027L;
    private static final Logger LOG = LoggerFactory.getLogger(KerberosTicketReload.class);

    private final SessiondService sessiondService;
    private final KerberosService kerberosService;
    private final EventAdmin eventAdmin;

    public KerberosTicketReload(SessiondService sessiondService, KerberosService kerberosService, EventAdmin eventAdmin) {
        super();
        this.sessiondService = sessiondService;
        this.kerberosService = kerberosService;
        this.eventAdmin = eventAdmin;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws IOException {
        try {
            doAuthHeaderTicketReload(req, resp, requestContext);
            if(requestContext.getMetricProvider().isStateUnknown()) {
               requestContext.getMetricProvider().recordSuccess();
            }
        } catch (OXException e) {
            requestContext.getMetricProvider().recordException(e);
            if (SessionExceptionCodes.hasPrefix(e)) {
                // Is a session exception
                LOG.debug(e.getMessage(), e);
            } else {
                LOG.error(e.getMessage(), e);
            }
            notAuthorized(resp, e.getMessage());
        }
    }

    private void doAuthHeaderTicketReload(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws OXException, IOException {
        final String sessionId = getSessionId(req);
        SessionResult<ServerSession> result = getSession(req, resp, sessionId, sessiondService);
        if (Reply.STOP == result.getReply()) {
            return;
        }
        ServerSession session = result.getSession();
        if (null == session) {
            // No such session
            OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
            oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.NO_SUCH_SESSION.getIdentifier());
            throw oxe;
        }
        verifySession(req, sessiondService, sessionId, session);
        if (session.containsParameter(SESSION_PRINCIPAL)) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        final String auth = req.getHeader(Header.AUTH_HEADER);
        if (null == auth) {
            notAuthorized(resp, "Authorization Required!");
            requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (!Authorization.checkForAuthorizationHeader(auth)) {
            throw LoginExceptionCodes.UNKNOWN_HTTP_AUTHORIZATION.create("null");
        }
        if (!Authorization.checkForKerberosAuthorization(auth)) {
            throw LoginExceptionCodes.UNKNOWN_HTTP_AUTHORIZATION.create(Authorization.extractAuthScheme(auth));
        }

        final byte[] ticket = Base64.decode(auth.substring("Negotiate ".length()));
        ClientPrincipal principal;
        try {
            principal = kerberosService.verifyAndDelegate(ticket);
        } catch (IllegalStateException e) {
            // Is thrown if the ticket is no longer valid. See bug 35182.
            LOG.error(e.getMessage(), e);
            notAuthorized(resp, e.getMessage());
            requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        session.setParameter(SESSION_SUBJECT, principal.getDelegateSubject());
        session.setParameter(SESSION_PRINCIPAL, principal);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        postEvent(session);
    }

    private void postEvent(Session session) {
        final Dictionary<String, Object> dic = new Hashtable<String, Object>();
        dic.put(SessiondEventConstants.PROP_SESSION, session);
        eventAdmin.postEvent(new Event(KerberosUtils.TOPIC_TICKET_READDED, dic));
    }

    private static void notAuthorized(HttpServletResponse resp, String message) throws IOException {
        resp.addHeader("WWW-Authenticate", "NEGOTIATE");
        resp.addHeader("WWW-Authenticate", "Basic realm=\"Open-Xchange\"");
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}
