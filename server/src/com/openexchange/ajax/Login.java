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
import java.lang.reflect.UndeclaredThrowableException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.helper.Send;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.authentication.LoginException;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.exception.SessiondException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.servlet.http.Tools;

public class Login extends AJAXServlet {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 7680745138705836499L;

    public static final String _random = "random";

    private static final String _name = "name";

    private static final String _password = "password";

    private static final String _redirectUrl = "/index.html#id=";

    public static final String cookiePrefix = "open-xchange-session-";

    private static transient final Log LOG = LogFactory.getLog(Login.class);

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String action = req.getParameter(PARAMETER_ACTION);
        if (action == null) {
            logAndSendException(resp, new AjaxException(AjaxException.Code.MISSING_PARAMETER, PARAMETER_ACTION));
            return;
        }
        if (ACTION_LOGIN.equals(action)) {
            /*
             * Look-up necessary credentials
             */
            final String name = req.getParameter(_name);
            if (null == name) {
                logAndSendException(resp, new AjaxException(AjaxException.Code.MISSING_PARAMETER, _name));
                return;
            }
            final String password = req.getParameter(_password);
            if (null == password) {
                logAndSendException(resp, new AjaxException(AjaxException.Code.MISSING_PARAMETER, _password));
                return;
            }
            /*
             * Perform the login
             */
            Session session = null;
            final Response response = new Response();
            com.openexchange.login.Login loginResult = null;
            try {
                loginResult = LoginPerformer.getInstance().doLogin(name, password, req.getRemoteAddr());
                session = loginResult.getSession();
            } catch (LoginException e) {
                if (AbstractOXException.Category.USER_INPUT == e.getCategory()) {
                    LOG.debug(e.getMessage(), e);
                } else {
                    LOG.error(e.getMessage(), e);
                }
                response.setException(e);
            }
            if (null != loginResult.getError()) {
                LoginException e = loginResult.getError();
                if (AbstractOXException.Category.USER_INPUT == e.getCategory()) {
                    LOG.debug(e.getMessage(), e);
                } else {
                    LOG.error(e.getMessage(), e);
                }
            }
            /*
             * Store associated session
             */
            SessionServlet.rememberSession(req, session);
            // Write response
            JSONObject login = null;
            if (!response.hasError()) {
                try {
                    login = writeLogin(session);
                } catch (final JSONException e) {
                    final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
                    LOG.error(oje.getMessage(), oje);
                    response.setException(oje);
                }
            }
            // The magic spell to disable caching
            Tools.disableCaching(resp);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            if (!response.hasError()) {
                writeCookie(resp, session);
            }
            try {
                if (null == login) {
                    ResponseWriter.write(response, resp.getWriter());
                    // Response.write(response, resp.getWriter());
                } else {
                    login.write(resp.getWriter());
                }
            } catch (final JSONException e) {
                if (e.getCause() instanceof IOException) {
                    /*
                     * Throw proper I/O error since a serious socket error could been occurred which prevents further communication. Just
                     * throwing a JSON error possibly hides this fact by trying to write to/read from a broken socket connection.
                     */
                    throw (IOException) e.getCause();
                }
                log(RESPONSE_ERROR, e);
                sendError(resp);
            }
        } else if (ACTION_LOGOUT.equals(action)) {
            /*
             * The magic spell to disable caching
             */
            Tools.disableCaching(resp);
            final String cookieId = req.getParameter(PARAMETER_SESSION);
            if (cookieId == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            /*
             * Drop relevant cookies
             */
            String sessionId = null;
            final Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                final String cookieName = new StringBuilder(Login.cookiePrefix).append(cookieId).toString();
                int stat = 0;
                for (int a = 0; a < cookies.length && stat != 3; a++) {
                    if (cookieName.equals(cookies[a].getName())) {
                        sessionId = cookies[a].getValue();
                        final Cookie respCookie = new Cookie(cookieName, sessionId);
                        respCookie.setPath("/");
                        respCookie.setMaxAge(0); // delete
                        resp.addCookie(respCookie);
                        stat |= 1;
                    } else if (AJPv13RequestHandler.JSESSIONID_COOKIE.equals(cookies[a].getName())) {
                        final Cookie jsessionIdCookie = new Cookie(AJPv13RequestHandler.JSESSIONID_COOKIE, cookies[a].getValue());
                        jsessionIdCookie.setPath("/");
                        jsessionIdCookie.setMaxAge(0); // delete
                        resp.addCookie(jsessionIdCookie);
                        stat |= 2;
                    }
                }
            }
            /*
             * Drop the session
             */
            if (sessionId != null) {
                final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                sessiondService.removeSession(sessionId);

            } else if (LOG.isDebugEnabled()) {
                LOG.debug("no session cookie found in request!");
            }
        } else if (ACTION_REDIRECT.equals(action)) {
            /*
             * The magic spell to disable caching
             */
            Tools.disableCaching(resp);
            final String randomToken = req.getParameter(_random);
            if (randomToken == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
            if (sessiondService == null) {
                final ServiceException se = new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, SessiondService.class.getName());
                LOG.error(se.getMessage(), se);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            final Session session = sessiondService.getSessionByRandomToken(randomToken, req.getRemoteAddr());
            if (session == null) {
                /*
                 * Unknown random token; throw error
                 */
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No session could be found for random token: " + randomToken, new Throwable());
                } else if (LOG.isInfoEnabled()) {
                    LOG.info("No session could be found for random token: " + randomToken);
                }
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            try {
                final Context context = ContextStorage.getInstance().getContext(session.getContextId());
                final User user = UserStorage.getInstance().getUser(session.getUserId(), context);
                if (!context.isEnabled() || !user.isMailEnabled()) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            } catch (final UndeclaredThrowableException e) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (final ContextException e) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (final LdapException e) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            writeCookie(resp, session);
            resp.sendRedirect(_redirectUrl + session.getSecret());
        } else if (ACTION_AUTOLOGIN.equals(action)) {
            final Cookie[] cookies = req.getCookies();
            final Response response = new Response();
            try {
                if (cookies == null) {
                    throw new OXJSONException(OXJSONException.Code.INVALID_COOKIE);
                }
                final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                for (final Cookie cookie : cookies) {
                    final String cookieName = cookie.getName();
                    if (cookieName.startsWith(cookiePrefix)) {
                        final String sessionId = cookie.getValue();
                        if (sessiondService.refreshSession(sessionId)) {
                            final Session session = sessiondService.getSession(sessionId);
                            SessionServlet.checkIP(session.getLocalIp(), req.getRemoteAddr());
                            try {
                                final Context ctx = ContextStorage.getInstance().getContext(session.getContextId());
                                final User user = UserStorage.getInstance().getUser(session.getUserId(), ctx);
                                if (!ctx.isEnabled() || !user.isMailEnabled()) {
                                    throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                                }
                            } catch (final UndeclaredThrowableException e) {
                                throw LoginExceptionCodes.UNKNOWN.create(e);
                            }

                            response.setData(writeLogin(session));
                            break;
                        }
                        // Not found session. Delete old OX cookie.
                        final Cookie respCookie = new Cookie(cookie.getName(), cookie.getValue());
                        respCookie.setPath("/");
                        respCookie.setMaxAge(0); // delete
                        resp.addCookie(respCookie);
                    }
                }
                if (null == response.getData()) {
                    throw new OXJSONException(OXJSONException.Code.INVALID_COOKIE);
                }
            } catch (final SessiondException e) {
                LOG.debug(e.getMessage(), e);
                response.setException(e);
            } catch (final OXJSONException e) {
                LOG.debug(e.getMessage(), e);
                response.setException(e);
            } catch (final JSONException e) {
                final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
                LOG.error(oje.getMessage(), oje);
                response.setException(oje);
            } catch (final ContextException e) {
                LOG.debug(e.getMessage(), e);
                response.setException(e);
            } catch (final LdapException e) {
                LOG.debug(e.getMessage(), e);
                response.setException(e);
            } catch (final LoginException e) {
                if (AbstractOXException.Category.USER_INPUT == e.getCategory()) {
                    LOG.debug(e.getMessage(), e);
                } else {
                    LOG.error(e.getMessage(), e);
                }
                response.setException(e);
            }
            /*
             * The magic spell to disable caching
             */
            Tools.disableCaching(resp);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            try {
                if (response.hasError()) {
                    ResponseWriter.write(response, resp.getWriter());
                } else {
                    ((JSONObject) response.getData()).write(resp.getWriter());
                }
            } catch (final JSONException e) {
                log(RESPONSE_ERROR, e);
                sendError(resp);
            }
        } else {
            logAndSendException(resp, new AjaxException(AjaxException.Code.UnknownAction, action));
        }
    }

    private void logAndSendException(final HttpServletResponse resp, final AjaxException e) throws IOException {
        LOG.debug(e.getMessage(), e);
        final Response response = new Response();
        response.setException(e);
        Send.sendResponse(response, resp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    /**
     * Writes the (groupware's) session cookie to specified HTTP servlet response whose name is composed by cookie prefix
     * <code>"open-xchange-session-"</code> and a secret cookie identifier.
     * 
     * @param resp The HTTP servlet response
     * @param session The session providing the secret cookie identifier
     */
    protected static void writeCookie(final HttpServletResponse resp, final Session session) {
        final Cookie cookie = new Cookie(cookiePrefix + session.getSecret(), session.getSessionID());
        cookie.setPath("/");
        resp.addCookie(cookie);
    }

    private JSONObject writeLogin(final Session sessionObj) throws JSONException {
        final JSONObject retval = new JSONObject();
        retval.put(PARAMETER_SESSION, sessionObj.getSecret());
        retval.put(_random, sessionObj.getRandomToken());
        return retval;
    }
}
