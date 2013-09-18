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

package com.openexchange.ajax.login;

import static com.openexchange.ajax.login.LoginTools.updateIPAddress;
import static com.openexchange.login.Interface.HTTP_JSON;
import static com.openexchange.tools.servlet.http.Tools.copyHeaders;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.log.LogFactory;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link AutoLogin}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AutoLogin extends AbstractLoginRequestHandler {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AutoLogin.class));

    private LoginConfiguration conf;

    /**
     * Initializes a new {@link AutoLogin}.
     * 
     */
    public AutoLogin(LoginConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        Tools.disableCaching(resp);
        resp.setContentType(Login.CONTENTTYPE_JAVASCRIPT);
        final Response response = new Response();
        Session session = null;
        try {
            if (!conf.isSessiondAutoLogin()) {
                if (doAutoLogin(req, resp)) {
                    throw AjaxExceptionCodes.DISABLED_ACTION.create("autologin");
                }
                return;
            }

            Cookie[] cookies = req.getCookies();
            if (cookies == null) {
                cookies = new Cookie[0];
            }

            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
            if (null == sessiondService) {
                final OXException se = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
                LOG.error(se.getMessage(), se);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            String secret = null;
            final String hash = HashCalculator.getInstance().getHash(req);
            final String sessionCookieName = Login.SESSION_PREFIX + hash;
            final String secretCookieName = Login.SECRET_PREFIX + hash;

            NextCookie: for (final Cookie cookie : cookies) {
                final String cookieName = cookie.getName();
                if (cookieName.startsWith(sessionCookieName)) {
                    final String sessionId = cookie.getValue();
                    session = sessiondService.getSession(sessionId);
                    if (null != session) {
                        // IP check if enabled; otherwise update session's IP address if different to request's IP address
                        // Insecure check is done in updateIPAddress method.
                        if (!conf.isIpCheck()) {
                            // Update IP address if necessary
                            updateIPAddress(conf, req.getRemoteAddr(), session);
                        } else {
                            final String newIP = req.getRemoteAddr();
                            SessionServlet.checkIP(true, conf.getRanges(), session, newIP, conf.getIpCheckWhitelist());
                            // IP check passed: update IP address if necessary
                            updateIPAddress(conf, newIP, session);
                        }
                        try {
                            final Context ctx = ContextStorage.getInstance().getContext(session.getContextId());
                            if (!ctx.isEnabled()) {
                                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                            }
                            final User user = UserStorage.getInstance().getUser(session.getUserId(), ctx);
                            if (!user.isMailEnabled()) {
                                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                            }
                        } catch (final UndeclaredThrowableException e) {
                            throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
                        }

                        // Request modules
                        final Future<Object> optModules = getModulesAsync(session, req);

                        // Create JSON object
                        final JSONObject json = new JSONObject(8);
                        LoginWriter.write(session, json);

                        if (null != optModules) {
                            // Append "config/modules"
                            try {
                                final Object oModules = optModules.get();
                                if (null != oModules) {
                                    json.put("modules", oModules);
                                }
                            } catch (final InterruptedException e) {
                                // Keep interrupted state
                                Thread.currentThread().interrupt();
                                throw LoginExceptionCodes.UNKNOWN.create(e, "Thread interrupted.");
                            } catch (final ExecutionException e) {
                                // Cannot occur
                                final Throwable cause = e.getCause();
                                LOG.warn("Modules could not be added to login JSON response: " + cause.getMessage(), cause);
                            }
                        }

                        // Set data
                        response.setData(json);

                        // Secret already found?
                        if (null != secret) {
                            break NextCookie;
                        }
                    }
                } else if (cookieName.startsWith(secretCookieName)) {
                    secret = cookie.getValue();
                    /*
                     * Session already found?
                     */
                    if (null != session) {
                        break NextCookie;
                    }
                }
            }
            if (null == response.getData() || session == null || secret == null || !(session.getSecret().equals(secret))) {
                SessionServlet.removeOXCookies(hash, req, resp);
                SessionServlet.removeJSESSIONID(req, resp);
                if (doAutoLogin(req, resp)) {
                    throw OXJSONExceptionCodes.INVALID_COOKIE.create();
                }
                return;
            }

            /*-
             * Ensure appropriate public-session-cookie is set
             */
            writePublicSessionCookie(resp, session, req.isSecure(), req.getServerName(), conf);

        } catch (final OXException e) {
            if (AjaxExceptionCodes.DISABLED_ACTION.equals(e)) {
                LOG.debug(e.getMessage(), e);
            } else {
                e.log(LOG);
            }
            if (SessionServlet.isIpCheckError(e) && null != session) {
                try {
                    // Drop Open-Xchange cookies
                    final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                    SessionServlet.removeOXCookies(session.getHash(), req, resp);
                    SessionServlet.removeJSESSIONID(req, resp);
                    sessiondService.removeSession(session.getSessionID());
                } catch (final Exception e2) {
                    LOG.error("Cookies could not be removed.", e2);
                }
            }
            response.setException(e);
        } catch (final JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
        }
        // The magic spell to disable caching
        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(Login.CONTENTTYPE_JAVASCRIPT);
        try {
            if (response.hasError()) {
                ResponseWriter.write(response, resp.getWriter(), Login.localeFrom(session));
            } else {
                ((JSONObject) response.getData()).write(resp.getWriter());
            }
        } catch (final JSONException e) {
            LOG.error(Login.RESPONSE_ERROR, e);
            Login.sendError(resp);
        }
    }

    private boolean doAutoLogin(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, OXException {
        return loginOperation(req, resp, new LoginClosure() {

            @Override
            public LoginResult doLogin(final HttpServletRequest req2) throws OXException {
                final LoginRequest request = parseAutoLoginRequest(req2);
                return LoginPerformer.getInstance().doAutoLogin(request);
            }
        }, conf);
    }

    private LoginRequest parseAutoLoginRequest(final HttpServletRequest req) throws OXException {
        final String authId = LoginTools.parseAuthId(req, false);
        final String client = LoginTools.parseClient(req, false, conf.getDefaultClient());
        final String clientIP = LoginTools.parseClientIP(req);
        final String userAgent = LoginTools.parseUserAgent(req);
        final Map<String, List<String>> headers = copyHeaders(req);
        final com.openexchange.authentication.Cookie[] cookies = Tools.getCookieFromHeader(req);
        final String httpSessionId = req.getSession(true).getId();
        return new LoginRequestImpl(
            null,
            null,
            clientIP,
            userAgent,
            authId,
            client,
            null,
            HashCalculator.getInstance().getHash(req, client),
            HTTP_JSON,
            headers,
            cookies,
            Tools.considerSecure(req, conf.isCookieForceHTTPS()),
            req.getServerName(),
            req.getServerPort(),
            httpSessionId);
    }
}
