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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.ajax.login;

import static com.openexchange.login.Interface.HTTP_JSON;
import static com.openexchange.tools.servlet.http.Tools.copyHeaders;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.server.services.SessionInspector;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.Reason;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AutoLogin}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AutoLogin extends AbstractLoginRequestHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AutoLogin.class);

    private final LoginConfiguration conf;
    private final ShareLoginConfiguration shareConf;

    /**
     * Initializes a new {@link AutoLogin}.
     *
     * @param conf A reference to the login configuration
     * @param shareConf A reference to the share login configuration
     */
    public AutoLogin(LoginConfiguration conf, ShareLoginConfiguration shareConf, Set<LoginRampUpService> rampUp) {
        super(rampUp);
        this.conf = conf;
        this.shareConf = shareConf;
    }

    @Override
    public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        Tools.disableCaching(resp);
        resp.setContentType(LoginServlet.CONTENTTYPE_JAVASCRIPT);
        Response response = new Response();
        Session session = null;
        try {
            /*
             * try guest auto-login first
             */
            LoginResult loginResult = AutoLoginTools.tryGuestAutologin(shareConf.getLoginConfig(), req, resp);
            if (null == loginResult) {
                if (false == conf.isSessiondAutoLogin()) {
                    // Auto-login disabled per configuration.
                    // Try to perform a login using HTTP request/response to see if invocation signals that an auto-login should proceed afterwards
                    if (doAutoLogin(req, resp)) {
                        if (Reply.STOP == SessionInspector.getInstance().getChain().onAutoLoginFailed(Reason.AUTO_LOGIN_DISABLED, req, resp)) {
                            return;
                        }
                        throw AjaxExceptionCodes.DISABLED_ACTION.create("autologin");
                    }
                    return;
                }
                /*
                 * try auto-login for regular user
                 */
                String hash = HashCalculator.getInstance().getHash(req, LoginTools.parseUserAgent(req), LoginTools.parseClient(req, false, conf.getDefaultClient()), LoginTools.parseShareInformation(req));
                loginResult = AutoLoginTools.tryAutologin(conf, req, resp, hash);
                if (null == loginResult) {
                    /*
                     * auto-login failed
                     */
                    SessionUtility.removeOXCookies(hash, req, resp);
                    SessionUtility.removeJSESSIONID(req, resp);
                    if (doAutoLogin(req, resp)) {
                        if (Reply.STOP == SessionInspector.getInstance().getChain().onAutoLoginFailed(Reason.AUTO_LOGIN_FAILED, req, resp)) {
                            return;
                        }
                        throw OXJSONExceptionCodes.INVALID_COOKIE.create();
                    }
                    return;
                }
            }
            /*
             * auto-login successful, prepare result
             */
            ServerSession serverSession = ServerSessionAdapter.valueOf(loginResult.getSession(), loginResult.getContext(), loginResult.getUser());
            session = serverSession;

            // Trigger client-specific ramp-up
            Future<JSONObject> optRampUp = rampUpAsync(serverSession, req);

            // Request modules
            Future<Object> optModules = getModulesAsync(session, req);

            // Create JSON object
            final JSONObject json = new JSONObject(8);
            LoginWriter.write(session, json);

            // Append "config/modules"
            if (null != optModules) {
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
                    LOG.warn("Modules could not be added to login JSON response", cause);
                }
            }

            // Await client-specific ramp-up and add to JSON object
            if (null != optRampUp) {
                try {
                    JSONObject jsonObject = optRampUp.get();
                    for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                        json.put(entry.getKey(), entry.getValue());
                    }
                } catch (InterruptedException e) {
                    // Keep interrupted state
                    Thread.currentThread().interrupt();
                    throw LoginExceptionCodes.UNKNOWN.create(e, "Thread interrupted.");
                } catch (ExecutionException e) {
                    // Cannot occur
                    final Throwable cause = e.getCause();
                    LOG.warn("Ramp-up information could not be added to login JSON response", cause);
                }
            }

            // Set data
            response.setData(json);

            /*-
             * Ensure appropriate public-session-cookie is set
             */
            LoginServlet.writePublicSessionCookie(req, resp, session, req.isSecure(), req.getServerName());

        } catch (final OXException e) {
            if (AjaxExceptionCodes.DISABLED_ACTION.equals(e)) {
                LOG.debug("", e);
            } else {
                switch (e.getCategories().get(0).getLogLevel()) {
                    case TRACE:
                        LOG.trace("", e);
                        break;
                    case DEBUG:
                        LOG.debug("", e);
                        break;
                    case INFO:
                        LOG.info("", e);
                        break;
                    case WARNING:
                        LOG.warn("", e);
                        break;
                    case ERROR:
                        LOG.error("", e);
                        break;
                    default:
                        break;
                }
            }
            if (SessionUtility.isIpCheckError(e) && null != session) {
                try {
                    // Drop Open-Xchange cookies
                    final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                    SessionUtility.removeOXCookies(session.getHash(), req, resp);
                    SessionUtility.removeJSESSIONID(req, resp);
                    sessiondService.removeSession(session.getSessionID());
                } catch (final Exception e2) {
                    LOG.error("Cookies could not be removed.", e2);
                }
            }
            response.setException(e);
        } catch (final JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
        }
        // The magic spell to disable caching
        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(LoginServlet.CONTENTTYPE_JAVASCRIPT);
        try {
            if (response.hasError()) {
                ResponseWriter.write(response, resp.getWriter(), LoginServlet.localeFrom(session));
            } else {
                ((JSONObject) response.getData()).write(resp.getWriter());
            }
        } catch (final JSONException e) {
            LOG.error(LoginServlet.RESPONSE_ERROR, e);
            LoginServlet.sendError(resp);
        }
    }

    /**
     * Performs a login while providing the auto-login {@link LoginClosure closure}.
     *
     * @param req The associated HTTP request
     * @param resp The associated HTTP response
     * @return <code>true</code> if an auto login should proceed afterwards; otherwise <code>false</code>
     */
    private boolean doAutoLogin(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, OXException {
        return loginOperation(req, resp, new LoginClosure() {

            @Override
            public LoginResult doLogin(final HttpServletRequest req2) throws OXException {
                final LoginRequest request = parseAutoLoginRequest(req2);
                return LoginPerformer.getInstance().doAutoLogin(request);
            }
        }, conf);
    }

    /**
     * Parses the given HTTP request into an appropriate {@link LoginRequest} instance.
     *
     * @param req The HTTP request to parse
     * @return The resulting {@link LoginRequest} instance
     * @throws OXException If parse operation fails
     */
    LoginRequest parseAutoLoginRequest(final HttpServletRequest req) throws OXException {
        final String authId = LoginTools.parseAuthId(req, false);
        final String client = LoginTools.parseClient(req, false, conf.getDefaultClient());
        final String clientIP = LoginTools.parseClientIP(req);
        final String userAgent = LoginTools.parseUserAgent(req);
        final Map<String, List<String>> headers = copyHeaders(req);
        final com.openexchange.authentication.Cookie[] cookies = Tools.getCookieFromHeader(req);
        final String httpSessionId = req.getSession(true).getId();

        LoginRequestImpl.Builder b = new LoginRequestImpl.Builder().login(null).password(null).clientIP(clientIP);
        b.userAgent(userAgent).authId(authId).client(client).version(null);
        b.hash(HashCalculator.getInstance().getHash(req, client));
        b.iface(HTTP_JSON).headers(headers).cookies(cookies).secure(Tools.considerSecure(req, conf.isCookieForceHTTPS()));
        b.serverName(req.getServerName()).serverPort(req.getServerPort()).httpSessionID(httpSessionId);
        return b.build();
    }

}
