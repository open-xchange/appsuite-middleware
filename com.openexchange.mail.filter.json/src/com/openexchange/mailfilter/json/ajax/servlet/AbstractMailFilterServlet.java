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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mailfilter.json.ajax.servlet;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.log.LogProperties;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.json.ajax.Parameter;
import com.openexchange.mailfilter.json.ajax.actions.AbstractAction;
import com.openexchange.mailfilter.json.ajax.actions.AbstractRequest;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.CountingHttpServletRequest;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractMailFilterServlet extends HttpServlet {

	private static final long serialVersionUID = 3006497622205429579L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractMailFilterServlet.class);

    private static final String PARAMETER_SESSION = com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;

    /**
     * The content type if the response body contains javascript data. Set it
     * with <code>resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT)</code>.
     */
    public static final String CONTENTTYPE_JAVASCRIPT = "text/javascript; charset=UTF-8";

    private CookieHashSource hashSource;

    protected AbstractMailFilterServlet() {
        super();
    }

    /**
     * Gets the locale for given session
     *
     * @param session The session
     * @return The locale
     * @throws OXException
     */
    protected static Locale localeFrom(final Session session) throws OXException {
        if (null == session) {
            return Locale.US;
        }
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getLocale();
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            // create a new HttpSession if it's missing
            req.getSession(true);
            super.service(new CountingHttpServletRequest(req), resp);
        } catch (RateLimitedException e) {
            e.send(resp);
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
                throw MailFilterExceptionCode.MISSING_PARAMETER.create("session");
            }

            final SessiondService service = Services.getService(SessiondService.class);
            if (null == service) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
            }
            session = service.getSession(sessionId);
            if (null == session) {
                LOG.info("There is no session associated with session identifier: {}", sessionId);
                throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
            }
            LogProperties.putSessionProperties(session);
            response.setLocale(session);

            // Check if session is valid
            String secret = SessionUtility.extractSecret(hashSource, req, session);
            if (!session.getSecret().equals(secret)) {
                LOG.info("Session secret is different. Given secret \"{}\" differs from secret in session \"{}\".", secret, session.getSecret());
                throw SessionExceptionCodes.WRONG_SESSION_SECRET.create();
            }
            checkMailfilterAvailable(session);

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
            final AbstractAction action = getAction();
            response.setData(action.action(request));
        } catch (final OXException e) {
            LOG.error("", e);
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
        } catch (OXException e) {
            LOG.error("", e);
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
            final SessiondService service = Services.getService(SessiondService.class);
            if (null == service) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class);
            }
            session = service.getSession(sessionId);
            if (null == session) {
                LOG.info("There is no session associated with session identifier: {}", sessionId);
                throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
            }
            LogProperties.putSessionProperties(session);
            response.setLocale(session);

            // Check if session is valid
            String secret = SessionUtility.extractSecret(hashSource, req, session);
            if (!session.getSecret().equals(secret)) {
                LOG.info("Session secret is different. Given secret \"{}\" differs from secret in session \"{}\".", secret, session.getSecret());
                throw SessionExceptionCodes.WRONG_SESSION_SECRET.create();
            }
            checkMailfilterAvailable(session);

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
            final AbstractAction action = getAction();
            response.setData(action.action(request));
        } catch (final OXException e) {
            LOG.error("", e);
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
        } catch (OXException e) {
            LOG.error("", e);
            sendError(resp);
        }
    }

    protected abstract AbstractRequest createRequest();

    protected abstract AbstractAction<?, ? extends AbstractRequest> getAction();

    private void checkMailfilterAvailable(Session session) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (false == serverSession.getUserConfiguration().hasWebMail()) {
            throw MailFilterExceptionCode.MAILFILTER_NOT_AVAILABLE.create(Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
    }

}
