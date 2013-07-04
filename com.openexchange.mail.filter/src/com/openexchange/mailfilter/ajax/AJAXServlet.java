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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mailfilter.ajax;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.log.LogFactory;
import com.openexchange.mailfilter.ajax.actions.AbstractAction;
import com.openexchange.mailfilter.ajax.actions.AbstractRequest;
import com.openexchange.mailfilter.ajax.exceptions.OXMailfilterExceptionCode;
import com.openexchange.mailfilter.services.MailFilterServletServiceRegistry;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.CountingHttpServletRequest;
import com.openexchange.tools.servlet.RateLimitedException;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AJAXServlet extends HttpServlet {

	private static final long serialVersionUID = 3006497622205429579L;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AJAXServlet.class));

    private static final String PARAMETER_SESSION = com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;

    /**
     * The content type if the response body contains javascript data. Set it
     * with <code>resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT)</code>.
     */
    public static final String CONTENTTYPE_JAVASCRIPT = "text/javascript; charset=UTF-8";

    private CookieHashSource hashSource;

    protected AJAXServlet() {
        super();
    }

    /**
     * Gets the locale for given session
     *
     * @param session The session
     * @return The locale
     */
    protected static Locale localeFrom(final Session session) {
        if (null == session) {
            return Locale.US;
        }
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        return UserStorage.getStorageUser(session.getUserId(), session.getContextId()).getLocale();
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            // create a new HttpSession if it's missing
            req.getSession(true);
            super.service(new CountingHttpServletRequest(req), resp);
        } catch (final RateLimitedException e) {
            resp.setContentType("text/plain; charset=UTF-8");
            resp.sendError(429, "Too Many Requests - Your request is being rate limited.");
        }
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        hashSource = CookieHashSource.parse(config.getInitParameter(Property.COOKIE_HASH.getPropertyName()));
    }

    protected static void sendError(final HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final Response response = new Response();
        Session session = null;
        try {
            final String sessionId = req.getParameter(PARAMETER_SESSION);
            if (sessionId == null) {
                throw OXMailfilterExceptionCode.MISSING_PARAMETER.create("session");
            }

            final SessiondService service = MailFilterServletServiceRegistry.getServiceRegistry().getService(SessiondService.class);
            if (null == service) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
            }
            session = service.getSession(sessionId);
            if (null == session) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("There is no session associated with session identifier: " + sessionId);
                }
                throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
            }
            response.setLocale(session);
            final String secret = SessionServlet.extractSecret(hashSource, req, session.getHash(), session.getClient());
            // Check if session is valid
            if (!session.getSecret().equals(secret)) {
                if (LOG.isInfoEnabled() && null != secret) {
                    LOG.info("Session secret is different. Given secret \"" + secret + "\" differs from secret in session \"" + session.getSecret() + "\".");
                }
                throw SessionExceptionCodes.WRONG_SESSION_SECRET.create();
            }

            final AbstractRequest request = createRequest();
            request.setSession(session);

            request.setParameters(new AbstractRequest.Parameters() {
                @Override
                public String getParameter(final Parameter param) throws OXException {
                    final String value = req.getParameter(param.getName());
                    if (param.isRequired() && null == value) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create( param.getName());
                    }
                    return value;
                }
            });
            /*
             * A non-download action
             */
            final AbstractAction action = createAction(session);
            response.setData(action.action(request));
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        }
        /*
         * Disable browser cache
         */
        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            LOG.error("Error while writing JSON.", e);
            sendError(resp);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final Response response = new Response();
        Session session = null;
        try {
            final String sessionId = req.getParameter(PARAMETER_SESSION);
            final SessiondService service = MailFilterServletServiceRegistry.getServiceRegistry().getService(SessiondService.class);
            if (null == service) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class);
            }
            session = service.getSession(sessionId);
            if (null == session) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("There is no session associated with session identifier: " + sessionId);
                }
                throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
            }
            response.setLocale(session);
            final String secret = SessionServlet.extractSecret(hashSource, req, session.getHash(), session.getClient());
            // Check if session is valid
            if (!session.getSecret().equals(secret)) {
                if (LOG.isInfoEnabled() && null != secret) {
                    LOG.info("Session secret is different. Given secret \"" + secret + "\" differs from secret in session \"" + session.getSecret() + "\".");
                }
                throw SessionExceptionCodes.WRONG_SESSION_SECRET.create();
            }

            final AbstractRequest request = createRequest();
            request.setSession(session);

            request.setParameters(new AbstractRequest.Parameters() {
                @Override
                public String getParameter(final Parameter param) throws OXException {
                    final String value = req.getParameter(param.getName());
                    if (null == value) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create( param.getName());
                    }
                    return value;
                }
            });
            request.setBody(com.openexchange.ajax.AJAXServlet.getBody(req));
            final AbstractAction action = createAction(session);
            response.setData(action.action(request));
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        }
        /*
         * Disable browser cache
         */
        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            LOG.error("Error while writing JSON.", e);
            sendError(resp);
        }
    }

    protected abstract AbstractRequest createRequest();

    protected abstract AbstractAction<?, ? extends AbstractRequest> createAction(Session session);
}
