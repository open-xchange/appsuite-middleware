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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.exception.Classes;
import com.openexchange.sessiond.exception.SessionExceptionFactory;
import com.openexchange.sessiond.exception.SessiondException;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * Overridden service method that checks if a valid session can be found for the
 * request.
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@OXExceptionSource(classId = Classes.SESSION_SERVLET, component = EnumComponent.SESSION)
public abstract class SessionServlet extends AJAXServlet {

    /**
     * Serial version UID for serialization
     */
    private static final long serialVersionUID = -8308340875362868795L;

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(SessionServlet.class);

    /**
     * Factory for creating exceptions.
     */
    private static final SessionExceptionFactory EXCEPTION = new SessionExceptionFactory(SessionServlet.class);

    /**
     * Name of the key to remember the session for the request.
     */
    public static final String SESSION_KEY = "sessionObject";

    private boolean checkIP = true;

    /**
     * Default constructor.
     */
    protected SessionServlet() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        checkIP = Boolean.parseBoolean(config.getInitParameter(ServerConfig.Property.IP_CHECK.getPropertyName()));
    }

    /**
     * Checks the session ID supplied as a query parameter in the request URI.
     * {@inheritDoc}
     */
    @Override
    @OXThrows(category = Category.TRY_AGAIN, desc = "", exceptionId = 4, msg = "Context is locked.")
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        Tools.disableCaching(resp);
        try {
            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
            if (sessiondService == null) {
                throw new SessiondException(
                    new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, SessiondService.class.getName()));
            }
            final Session session = getSession(req, resp, getCookieId(req), sessiondService);
            final String sessionId = session.getSessionID();
            final Context ctx = ContextStorage.getStorageContext(session.getContextId());
            if (!ctx.isEnabled()) {
                final SessiondService sessiondCon = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                sessiondCon.removeSession(sessionId);
                throw EXCEPTION.create(4);
            }
            checkIP(session, req.getRemoteAddr());
            rememberSession(req, new ServerSessionAdapter(session, ctx));
            super.service(req, resp);
        } catch (final SessiondException e) {
            LOG.debug(e.getMessage(), e);
            final Response response = new Response();
            response.setException(e);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, writer);
                writer.flush();
            } catch (final JSONException e1) {
                log(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        } catch (final ContextException e) {
            LOG.debug(e.getMessage(), e);
            final Response response = new Response();
            response.setException(e);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, writer);
                writer.flush();
            } catch (final JSONException e1) {
                log(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private void checkIP(Session session, String actual) throws SessiondException {
        checkIP(checkIP, session, actual);
    }

    /**
     * Checks if the client IP address of the current request matches the one through that the session has been created.
     * @param checkIP <code>true</code> to deny request with an exception.
     * @param session session object
     * @param actual IP address of the current request.
     * @throws SessionException
     *             if the IP addresses don't match.
     */
    @OXThrows(
        category = Category.PERMISSION,
        desc = "If a session exists every request is checked for its client IP address to match the one while creating the session.",
        exceptionId = 5,
        msg = "Wrong client IP address."
    )
    public static void checkIP(boolean checkIP, Session session, String actual) throws SessiondException {
        if (null == actual || !actual.equals(session.getLocalIp())) {
            if (checkIP) {
                LOG.info("Request with session " + session.getSessionID() + " denied. IP changed to " + actual + " but login came from " + session.getLocalIp());
                throw EXCEPTION.create(5);
            }
            LOG.debug("Session " + session.getSessionID() + " requests now from " + actual + " but login came from " + session.getLocalIp());
        }
    }

    /**
     * Gets the cookie identifier from the request.
     * 
     * @param req
     *            servlet request.
     * @return the cookie identifier.
     * @throws SessionException
     *             if the cookie identifier can not be found.
     */
    @OXThrows(category = Category.CODE_ERROR, desc = "Every AJAX request must contain a parameter named session "
            + "that value contains the identifier of the session cookie.", exceptionId = 1, msg = "The session parameter is missing.")
    private static String getCookieId(final ServletRequest req) throws SessiondException {
        final String retval = req.getParameter(PARAMETER_SESSION);
        if (null == retval) {
            if (LOG.isDebugEnabled()) {
                final StringBuilder debug = new StringBuilder();
                debug.append("Parameter session not found: ");
                final Enumeration<?> enm = req.getParameterNames();
                while (enm.hasMoreElements()) {
                    debug.append(enm.nextElement());
                    debug.append(',');
                }
                if (debug.length() > 0) {
                    debug.setCharAt(debug.length() - 1, '.');
                }
                LOG.debug(debug.toString());
            }
            throw EXCEPTION.create(1);
        }
        return retval;
    }

    /**
     * Gets the session identifier from the cookies.
     * @param req HTTP servlet request.
     * @param cookieId cookie identifier from the session parameter.
     * @return a found session identifier or <code>null</code> if the session
     * identifier can not be found.
     */
    private static String getSessionId(final HttpServletRequest req, final String cookieId) {
        final Cookie[] cookies = req.getCookies();
        String sessionId = null;
        if (cookies != null) {
            final String cookieName = new StringBuilder(Login.COOKIE_PREFIX).append(cookieId).toString();
            for (final Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }
        return sessionId;
    }

    /**
     * Gets the session identifier from the request.
     * 
     * @param req
     *            HTTP servlet request.
     * @param resp
     *            HTTP servlet response
     * @param cookieId
     *            Identifier of the cookie.
     * @param sessiondService
     *            The SessionD service
     * @return The appropriate session.
     * @throws SessionException
     *             if either the session identifier can not be found in a cookie
     *             or the session is missing at all.
     */
    @OXThrows(category = Category.CODE_ERROR, desc = "Your browser does not send the cookie for identifying your "
            + "session.", exceptionId = 2, msg = "The cookie with the session identifier is missing.")
    private static Session getSession(final HttpServletRequest req, final HttpServletResponse resp,
            final String cookieId, final SessiondService sessiondService)
            throws SessiondException {
        /*
         * Look for a local session
         */
        try {
            final String sessionId = getSessionId(req, cookieId);
            if (null != sessionId) {
                return getLocalSession(sessionId, sessiondService);
            }
        } catch (final SessiondException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No appropriate local session found");
            }
            /*
             * Look for a cached session on second try
             */
            final Session session = getCachedSession(req, resp, cookieId, sessiondService);
            if (null != session) {
                return session;
            }
            /*
             * Re-throw original exception to indicate session absence
             */
            throw e;
        }
        /*
         * No appropriate cookie found: check session cache
         */
        final Session session = getCachedSession(req, resp, cookieId, sessiondService);
        if (null != session) {
            return session;
        }
        /*
         * A cache miss: throw error
         */
        final Cookie[] cookies = req.getCookies();
        if (LOG.isDebugEnabled() && cookies != null) {
            final StringBuilder debug = new StringBuilder(256);
            debug.append("No cookie for ID: ");
            debug.append(cookieId);
            debug.append(". Cookie names: ");
            for (final Cookie cookie : cookies) {
                debug.append(cookie.getName());
                debug.append(',');
            }
            debug.setCharAt(debug.length() - 1, '.');
            LOG.debug(debug.toString());
        }
        throw EXCEPTION.create(2);
    }

    /**
     * Finds appropriate cached session.
     * 
     * @param req
     *            HTTP servlet request
     * @param resp
     *            HTTP servlet response
     * @param cookieId
     *            Identifier of the cookie
     * @param sessiondService
     *            The SessionD service
     * @return The session if fetched from cache; otherwise <code>null</code>.
     */
    private static Session getCachedSession(final HttpServletRequest req, final HttpServletResponse resp,
            final String cookieId, final SessiondService sessiondService) {
        final Session session = sessiondService.getCachedSession(cookieId, req.getRemoteAddr());
        if (null != session) {
            /*
             * Adapt cookie
             */
            Login.writeCookie(resp, session);
            return session;
        }
        return null;
    }

    /**
     * Finds appropriate local session.
     * 
     * @param sessionId
     *            identifier of the session.
     * @param sessiondService
     *            The SessionD service
     * @return the session.
     * @throws SessionException
     *             if the session can not be found.
     */
    @OXThrows(category = Category.TRY_AGAIN, desc = "A session with the given identifier can not be found.", exceptionId = 3, msg = "Your session %s expired. Please start a new browser session.")
    private static Session getLocalSession(final String sessionId, final SessiondService sessiondService)
            throws SessiondException {
        final Session retval = sessiondService.getSession(sessionId);
        if (null == retval) {
            throw EXCEPTION.create(3, sessionId);
        }
        try {
            final Context context = ContextStorage.getStorageContext(retval.getContextId());
            final User user = UserStorage.getInstance().getUser(retval.getUserId(), context);
            if (!user.isMailEnabled()) {
                throw EXCEPTION.create(3, sessionId);
            }
        } catch (final UndeclaredThrowableException e) {
            throw EXCEPTION.create(3, sessionId);
        } catch (final AbstractOXException e) {
            throw EXCEPTION.create(3, sessionId);
        }
        return retval;
    }

    /**
     * Convenience method to remember the session for a request in the servlet
     * attributes.
     * 
     * @param req
     *            servlet request.
     * @param session
     *            session to remember.
     */
    public static void rememberSession(final ServletRequest req, final ServerSession session) {
        req.setAttribute(SESSION_KEY, session);
    }

    /**
     * Returns the remembered session.
     * 
     * @param req
     *            servlet request.
     * @return the remembered session.
     */
    protected static ServerSession getSessionObject(final ServletRequest req) {
        return (ServerSession) req.getAttribute(SESSION_KEY);
    }
}
