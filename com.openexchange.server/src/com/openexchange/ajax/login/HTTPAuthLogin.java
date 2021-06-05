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

package com.openexchange.ajax.login;

import static com.openexchange.ajax.login.LoginTools.parseClient;
import static com.openexchange.ajax.login.LoginTools.parseUserAgent;
import static com.openexchange.login.Interface.HTTP_JSON;
import static com.openexchange.tools.servlet.http.Tools.copyHeaders;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionServletInterceptor;
import com.openexchange.ajax.SessionServletInterceptorRegistry;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.fields.Header;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.user.User;

/**
 * {@link HTTPAuthLogin}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.1
 */
public final class HTTPAuthLogin implements LoginRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HTTPAuthLogin.class);

    private final LoginConfiguration conf;

    /**
     * Initializes a new {@link HTTPAuthLogin}.
     */
    public HTTPAuthLogin(LoginConfiguration conf) {
        super();
        this.conf = conf;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws IOException {
        try {
            doAuthHeaderLogin(req, resp, requestContext);
            if(requestContext.getMetricProvider().isStateUnknown()) {
                requestContext.getMetricProvider().recordSuccess();
            }
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            resp.addHeader("WWW-Authenticate", "NEGOTIATE");
            resp.addHeader("WWW-Authenticate", "Basic realm=\"Open-Xchange\"");
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            requestContext.getMetricProvider().recordException(e);
        }
    }

    private void doAuthHeaderLogin(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws OXException, IOException {
        /*
         * Try to lookup session by auto-login
         */
        LoginResult loginResult = tryAutologin(req, resp);
        if (null == loginResult) {
            /*
             * continue with auth header login
             */

            final String auth = req.getHeader(Header.AUTH_HEADER);
            if (null == auth) {
                resp.addHeader("WWW-Authenticate", "NEGOTIATE");
                resp.addHeader("WWW-Authenticate", "Basic realm=\"Open-Xchange\"");
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            final String version;
            final Credentials creds;
            if (!Authorization.checkForAuthorizationHeader(auth)) {
                throw LoginExceptionCodes.UNKNOWN_HTTP_AUTHORIZATION.create("");
            }
            if (Authorization.checkForBasicAuthorization(auth)) {
                creds = Authorization.decode(auth);
                LogProperties.putProperty(LogProperties.Name.LOGIN_LOGIN, creds.getLogin());
                version = conf.getClientVersion();
            } else if (Authorization.checkForKerberosAuthorization(auth)) {
                creds = new Credentials("kerberos", "");
                version = "Kerberos";
            } else {
                throw LoginExceptionCodes.UNKNOWN_HTTP_AUTHORIZATION.create("");
            }

            LoginRequest request;
            {
                String client = LoginTools.parseClient(req, false, conf.getDefaultClient());
                String clientIP = LoginTools.parseClientIP(req);
                String userAgent = LoginTools.parseUserAgent(req);
                Map<String, List<String>> headers = copyHeaders(req);
                com.openexchange.authentication.Cookie[] cookies = Tools.getCookieFromHeader(req);
                HttpSession httpSession = req.getSession(true);
                String httpAuthAutoLogin = conf.getHttpAuthAutoLogin();
                boolean staySignedIn = Strings.isNotEmpty(httpAuthAutoLogin) && Boolean.parseBoolean(httpAuthAutoLogin);

                LoginRequestImpl.Builder b = new LoginRequestImpl.Builder().login(creds.getLogin()).password(creds.getPassword()).clientIP(clientIP);
                b.userAgent(userAgent).authId(UUIDs.getUnformattedString(UUID.randomUUID())).client(client).version(version);
                b.hash(HashCalculator.getInstance().getHash(req, userAgent, client));
                b.iface(HTTP_JSON).headers(headers).requestParameter(req.getParameterMap());
                b.cookies(cookies).secure(Tools.considerSecure(req, conf.isCookieForceHTTPS()));
                b.serverName(req.getServerName()).serverPort(req.getServerPort()).httpSession(httpSession);
                b.staySignedIn(staySignedIn);
                request = b.build();
            }

            Map<String, Object> properties = new HashMap<String, Object>(1);
            {
                final String capabilities = req.getParameter("capabilities");
                if (null != capabilities) {
                    properties.put("client.capabilities", capabilities);
                }
            }
            loginResult = LoginPerformer.getInstance().doLogin(request, properties);
        }
        /*
         * render redirect response
         */
        Session session = loginResult.getSession();
        Tools.disableCaching(resp);
        LoginServlet.writeSecretCookie(req, resp, session, session.getHash(), req.isSecure(), req.getServerName(), conf);
        LoginServlet.addHeadersAndCookies(loginResult, resp);
        LoginServlet.writeSessionCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName());
        resp.sendRedirect(LoginTools.generateRedirectURL(null, session.getSessionID(), conf.getUiWebPath()));
    }

    /**
     * Tries to lookup an exiting session by the cookies supplied with the request.
     *
     * @param request The request to try and perform the auto-login for
     * @return The login result if a valid session was found, or <code>null</code>, otherwise
     * @throws OXException
     */
    private LoginResult tryAutologin(HttpServletRequest request, HttpServletResponse resp) throws OXException {
        Cookie[] cookies = request.getCookies();
        if (!Boolean.valueOf(conf.getHttpAuthAutoLogin()).booleanValue() || null == cookies || 0 == cookies.length) {
            return null;
        }
        /*
         * extract session & secret from supplied cookies
         */
        String sessionID = null;
        String secret = null;
        String hash = HashCalculator.getInstance().getHash(request, parseUserAgent(request), parseClient(request, false, conf.getDefaultClient()));
        String sessionCookieName = LoginServlet.SESSION_PREFIX + hash;
        String secretCookieName = LoginServlet.SECRET_PREFIX + hash;
        for (int i = 0; i < cookies.length && (null == sessionID || null == secret); i++) {
            String name = cookies[i].getName();
            if (name.startsWith(sessionCookieName)) {
                sessionID = cookies[i].getValue();
            } else if (name.startsWith(secretCookieName)) {
                secret = cookies[i].getValue();
            }
        }
        if (null == sessionID || null == secret) {
            return null;
        }
        /*
         * lookup matching session
         */
        SessiondService sessiond = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        Session session = sessiond.getSession(sessionID);
        if (null == session || !session.getSecret().equals(secret)) {
            return null;
        }
        /*
         * check & take over remote IP
         */
        String remoteAddress = request.getRemoteAddr();
        SessionUtility.checkIP(session, remoteAddress);
        LoginTools.updateIPAddress(conf, remoteAddress, session);
        /*
         * ensure user & context are enabled
         */
        Context context = ContextStorage.getInstance().getContext(session.getContextId());
        User user = UserStorage.getInstance().getUser(session.getUserId(), context);
        if (false == context.isEnabled() || false == user.isMailEnabled()) {
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        }
        for (SessionServletInterceptor interceptor : SessionServletInterceptorRegistry.getInstance().getInterceptors()) {
            try {
                interceptor.intercept(session, request, resp);
            } catch (@SuppressWarnings("unused") OXException e) {
                // Session is not valid anymore.
                sessiond.removeSession(sessionID);
                SessionUtility.removeOXCookies(request, resp, Arrays.asList(LoginServlet.SESSION_PREFIX + hash));
                return null;
            }
        }
        /*
         * wrap valid session into login result
         */
        return new LoginResultImpl(session, context, user);
    }
}
