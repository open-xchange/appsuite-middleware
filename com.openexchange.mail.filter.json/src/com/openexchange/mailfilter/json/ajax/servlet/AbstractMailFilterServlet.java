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

package com.openexchange.mailfilter.json.ajax.servlet;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.log.LogProperties;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.json.ajax.Parameter;
import com.openexchange.mailfilter.json.ajax.actions.AbstractAction;
import com.openexchange.mailfilter.json.ajax.actions.AbstractRequest;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessiond.ExpirationReason;
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
    protected static Locale localeFrom(Session session) throws OXException {
        if (null == session) {
            return Locale.US;
        }
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getLocale();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // create a new HttpSession if it's missing
            req.getSession(true);
            super.service(new CountingHttpServletRequest(req), resp);
        } catch (RateLimitedException e) {
            // Mark optional HTTP session as rate-limited
            HttpSession optionalHttpSession = req.getSession(false);
            if (optionalHttpSession != null) {
                optionalHttpSession.setAttribute(com.openexchange.servlet.Constants.HTTP_SESSION_ATTR_RATE_LIMITED, Boolean.TRUE);
            }
            // Send error response
            e.send(resp);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        hashSource = CookieHashSource.parse(config.getInitParameter(Property.COOKIE_HASH.getPropertyName()));
    }

    protected static void sendError(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Response response = new Response();
        Session session = null;
        try {
            String sessionId = req.getParameter(PARAMETER_SESSION);
            if (sessionId == null) {
                throw MailFilterExceptionCode.MISSING_PARAMETER.create("session");
            }

            SessiondService service = Services.getService(SessiondService.class);
            if (null == service) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
            }
            session = service.getSession(sessionId);
            if (null == session) {
                LOG.info("There is no session associated with session identifier: {}", sessionId);
                OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
                oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.NO_SUCH_SESSION.getIdentifier());
                throw oxe;
            }
            LogProperties.putSessionProperties(session);
            response.setLocale(session);

            // Check if session is valid
            String secret = SessionUtility.extractSecret(hashSource, req, session);
            if (!session.getSecret().equals(secret)) {
                LOG.info("Session secret is different. Given secret \"{}\" differs from secret in session \"{}\".", secret, session.getSecret());
                OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
                oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.SECRET_MISMATCH.getIdentifier());
                throw oxe;
            }
            checkMailfilterAvailable(session);

            AbstractRequest request = createRequest();
            request.setSession(session);

            request.setParameters(new AbstractRequest.Parameters() {

                @Override
                public String getParameter(Parameter param) throws OXException {
                    String value = req.getParameter(param.getName());
                    if (param.isRequired() && null == value) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(param.getName());
                    }
                    return value;
                }
            });
            /*
             * A non-download action
             */
            @SuppressWarnings("rawtypes") AbstractAction action = getAction();
            response.setData(action.action(request));
        } catch (OXException e) {
            if (SessionExceptionCodes.hasPrefix(e)) {
                LOG.debug("", e);
            } else {
                LOG.error("", e);
            }
            response.setException(e);
        }
        /*
         * Disable browser cache
         */
        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        AJAXServlet.setDefaultContentType(resp);
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
        } catch (JSONException e) {
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
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Response response = new Response();
        Session session = null;
        try {
            String sessionId = req.getParameter(PARAMETER_SESSION);
            SessiondService service = Services.getService(SessiondService.class);
            if (null == service) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class);
            }
            session = service.getSession(sessionId);
            if (null == session) {
                LOG.info("There is no session associated with session identifier: {}", sessionId);
                OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
                oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.NO_SUCH_SESSION.getIdentifier());
                throw oxe;
            }
            LogProperties.putSessionProperties(session);
            response.setLocale(session);

            // Check if session is valid
            String secret = SessionUtility.extractSecret(hashSource, req, session);
            if (!session.getSecret().equals(secret)) {
                LOG.info("Session secret is different. Given secret \"{}\" differs from secret in session \"{}\".", secret, session.getSecret());
                OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
                oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.SECRET_MISMATCH.getIdentifier());
                throw oxe;
            }
            checkMailfilterAvailable(session);

            AbstractRequest request = createRequest();
            request.setSession(session);

            request.setParameters(param -> {
                String value = req.getParameter(param.getName());
                if (null == value) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create(param.getName());
                }
                return value;
            });
            request.setBody(com.openexchange.ajax.AJAXServlet.getBody(req));
            @SuppressWarnings("rawtypes") AbstractAction action = getAction();
            response.setData(action.action(request));
        } catch (OXException e) {
            if (SessionExceptionCodes.hasPrefix(e)) {
                LOG.debug("", e);
            } else {
                LOG.error("", e);
            }
            response.setException(e);
        }
        /*
         * Disable browser cache
         */
        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        AJAXServlet.setDefaultContentType(resp);
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
        } catch (JSONException e) {
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
        UserConfiguration userConfig = serverSession.getUserConfiguration();
        if (userConfig == null) {
            throw new IllegalStateException("User configuration object could not be loaded for user " + session.getUserId() + " in context " + session.getContextId());
        }
        if (false == userConfig.hasWebMail()) {
            throw MailFilterExceptionCode.MAILFILTER_NOT_AVAILABLE.create(Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
    }

}
