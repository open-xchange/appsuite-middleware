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

package com.openexchange.oidc.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCBackendConfig.AutologinMode;
import com.openexchange.oidc.OIDCBackendProperty;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.spi.OIDCBackend;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.EnhancedAuthenticated;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * {@link OIDCLoginRequestHandler} Performs a login with a valid {@link Reservation} and
 * creates a {@link Session} in the process. Also tries to login the user into a valid
 * {@link Session} directly, if the {@link OIDCBackendProperty}.autologinCookieMode indicates
 * an enabled auto-login.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCLoginRequestHandler implements LoginRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OIDCLoginRequestHandler.class);
    private LoginConfiguration loginConfiguration;
    private OIDCBackend backend;
    private final ServiceLookup services;

    public OIDCLoginRequestHandler(LoginConfiguration loginConfiguration, OIDCBackend backend, ServiceLookup services) {
        this.loginConfiguration = loginConfiguration;
        this.backend = backend;
        this.services = services;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean respondWithJson = false;
        try {
            respondWithJson = respondWithJson(request);
            performLogin(request, response, respondWithJson);
        } catch (OXException e) {
            LOG.error(e.getLocalizedMessage(), e);
            if (respondWithJson) {
                try {
                    ResponseWriter.writeException(e, new JSONWriter(
                        response.getWriter()).object());
                } catch (final JSONException jsonError) {
                    LOG.error(e.getLocalizedMessage(), jsonError);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    //TODO QS-VS: Die Login Prozedur wohl besser auslagern, aber wohin? Backend oder doch SSOProvider?
    private void performLogin(HttpServletRequest request, HttpServletResponse response, boolean respondWithJson) throws IOException, OXException, JSONException {
        LOG.trace("performLogin(HttpServletRequest request: {}, HttpServletResponse response)", request.getRequestURI());
        String sessionToken = request.getParameter(OIDCTools.SESSION_TOKEN);
        LOG.trace("Login user with session token: {}", sessionToken);
        if (Strings.isEmpty(sessionToken)) {
            handleException(response, respondWithJson, AjaxExceptionCodes.BAD_REQUEST.create(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        SessionReservationService sessionReservationService = Services.getService(SessionReservationService.class);
        Reservation reservation = sessionReservationService.removeReservation(sessionToken);
        if (null == reservation) {
            handleException(response, respondWithJson, LoginExceptionCodes.INVALID_CREDENTIALS.create(), HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idToken = reservation.getState().get(OIDCTools.IDTOKEN);
        if (Strings.isEmpty(idToken)) {
            handleException(response, respondWithJson, AjaxExceptionCodes.BAD_REQUEST.create(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ContextService contextService = Services.getService(ContextService.class);
        Context context = contextService.getContext(reservation.getContextId());
        if (!context.isEnabled()) {
            handleException(response, respondWithJson, LoginExceptionCodes.INVALID_CREDENTIALS.create(), HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        UserService userService = Services.getService(UserService.class);
        User user = userService.getUser(reservation.getUserId(), context);
        if (!user.isMailEnabled()) {
            handleException(response, respondWithJson, LoginExceptionCodes.INVALID_CREDENTIALS.create(), HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        boolean autologinEnabled = this.backend.getBackendConfig().isAutologinEnabled();

        String autologinCookieValue = null;
        if (autologinEnabled) {
            LOG.trace("Try OIDC auto-login with a cookie");
            if (this.performCookieLogin(request, response, reservation, respondWithJson)) {
                return;
            }
            autologinCookieValue = UUIDs.getUnformattedString(UUID.randomUUID());
        }

        LoginResult result = loginUser(request, context, user, reservation.getState(), autologinCookieValue);
        Session session = performSessionAdditions(result, request, response, idToken);

        if (autologinEnabled) {
            response.addCookie(createOIDCAutologinCookie(request, session, autologinCookieValue));
        }

        sendRedirect(session, request, response, respondWithJson);
    }

    private void handleException(HttpServletResponse response, boolean respondWithJson, OXException oxException, int sc) throws OXException, IOException {
        LOG.trace("handleException (HttpServletResponse response, boolean respondWithJson {}, OXException oxException {}, int sc {})", respondWithJson, oxException, sc);
        if (respondWithJson) {
            throw oxException;
        } else {
            response.sendError(sc);
        }
    }

    private boolean respondWithJson(HttpServletRequest request) {
        LOG.trace("respondWithJson (HttpServletRequest request: {})", request.getRequestURI());
        String acceptHeader = request.getHeader("Accept");
        if (null != acceptHeader && acceptHeader.toLowerCase().equals("application/json")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean performCookieLogin(HttpServletRequest request, HttpServletResponse response, Reservation reservation, boolean respondWithJson) throws IOException {
        LOG.trace("performCookieLogin(HttpServletRequest request: {}, HttpServletResponse response, Reservation reservation.token: {}, boolean respondWithJson)", request.getRequestURI(), reservation.getToken(), respondWithJson);
        AutologinMode autologinMode = OIDCBackendConfig.AutologinMode.get(this.backend.getBackendConfig().autologinCookieMode());

        boolean ssoCookieLogin = autologinMode == OIDCBackendConfig.AutologinMode.SSO_REDIRECT;
        if (ssoCookieLogin) {
            try {
                Cookie autologinCookie = OIDCTools.loadAutologinCookie(request, this.loginConfiguration);
                if (autologinCookie != null) {
                    return this.getAutologinByCookieURL(request, response, reservation, autologinCookie, respondWithJson);
                }
            } catch (OXException e) {
                LOG.debug("Ignoring OIDC auto-login attempt due to failed IP or secret check", e);
            }
        }
        return false;
    }

    private boolean getAutologinByCookieURL(HttpServletRequest request, HttpServletResponse response, Reservation reservation, Cookie oidcAtologinCookie, boolean respondWithJson) throws OXException, IOException {
        LOG.trace("getAutologinByCookieURL(HttpServletRequest request: {}, HttpServletResponse response, Reservation reservation.token: {}, Cookie oidcAtologinCookie: {}, boolean respondWithJson)", request.getRequestURI(), reservation.getToken(), oidcAtologinCookie != null ? oidcAtologinCookie.getValue() : "null", respondWithJson);
        if (oidcAtologinCookie != null) {
            try {
                Session session = this.getSessionFromAutologinCookie(oidcAtologinCookie);
                if (session != null) {
                    this.backend.updateSession(session, reservation.getState());
                    // the getRedirectLocationForSession does also the validation check of the session
                    String redirectLocationForSession = this.getRedirectLocationForSession(request, session, reservation);
                    if (respondWithJson) {
                        writeSessionDataAsJson(session, response);
                    } else {
                        response.sendRedirect(redirectLocationForSession);
                    }
                    return true;
                }
                //No session found, log that
                LOG.debug("No session found for OIDC Cookie with value: {}", oidcAtologinCookie.getValue());
            }
            catch (OXException | JSONException e) {
                LOG.debug("Ignoring OIDC auto-login attempt due to failed IP or secret check", e);
            }
        }

        if (oidcAtologinCookie != null) {
            Cookie toRemove = (Cookie) oidcAtologinCookie.clone();
            toRemove.setMaxAge(0);
            response.addCookie(toRemove);
        }

        return false;
    }

    private String getRedirectLocationForSession(HttpServletRequest request, Session session, Reservation reservation) throws OXException {
        LOG.trace("getRedirectLocationForSession(HttpServletRequest request: {}, Session session: {}, Reservation reservation: {})", request.getRequestURI(), session.getSessionID(), reservation.getToken());
        OIDCTools.validateSession(session, request);
        if (session.getContextId() == reservation.getContextId() && session.getUserId() == reservation.getUserId()) {
            throw LoginExceptionCodes.LOGIN_DENIED.create();
        }
        return OIDCTools.buildFrontendRedirectLocation(session, OIDCTools.getUIWebPath(this.loginConfiguration, this.backend.getBackendConfig()));
    }

    private Session getSessionFromAutologinCookie(Cookie oidcAtologinCookie) throws OXException {
        LOG.trace("getSessionFromAutologinCookie(Cookie oidcAtologinCookie: {})", oidcAtologinCookie.getValue());
        Session session = null;
        SessiondService sessiondService = Services.getService(SessiondService.class);
        Collection<String> sessions = sessiondService.findSessions(SessionFilter.create("(" + OIDCTools.SESSION_COOKIE + "=" + oidcAtologinCookie.getValue() + ")"));
        if (sessions.size() > 0) {
            session = sessiondService.getSession(sessions.iterator().next());
        }
        return session;
    }

    private Cookie createOIDCAutologinCookie(HttpServletRequest request, Session session, String uuid) {
        LOG.trace("createOIDCAutologinCookie(HttpServletRequest request: {}, Session session: {}, String uuid: {})", request.getRequestURI(), session.getSessionID(), uuid);
        Cookie oidcAutologinCookie = new Cookie(OIDCTools.AUTOLOGIN_COOKIE_PREFIX + session.getHash(), uuid);
        oidcAutologinCookie.setPath("/");
        oidcAutologinCookie.setSecure(Tools.considerSecure(request));
        oidcAutologinCookie.setMaxAge(-1);

        String domain = OIDCTools.getDomainName(request, services.getOptionalService(HostnameService.class));
        String cookieDomain = Cookies.getDomainValue(domain);
        if (cookieDomain != null) {
            oidcAutologinCookie.setDomain(cookieDomain);
        }
        return oidcAutologinCookie;

    }

    private Session performSessionAdditions(LoginResult loginResult, HttpServletRequest request, HttpServletResponse response, String idToken) throws OXException {
        LOG.trace("performSessionAdditions(LoginResult loginResult.sessionID: {}, HttpServletRequest request: {}, HttpServletResponse response, String idToken: {})", loginResult.getSession().getSessionID(), request.getRequestURI(), idToken);
        Session session = loginResult.getSession();

        LoginServlet.addHeadersAndCookies(loginResult, response);

        SessionUtility.rememberSession(request, new ServerSessionAdapter(session));

        LoginServlet.writeSecretCookie(request, response, session, session.getHash(), request.isSecure(), request.getServerName(), this.loginConfiguration);

        return session;
    }

    private LoginResult loginUser(HttpServletRequest request, final Context context, final User user, final Map<String, String> state, final String oidcAutologinCookieValue) throws OXException {
        LOG.trace("loginUser(HttpServletRequest request: {}, final Context context: {}, final User user: {}, final Map<String, String> state.size: {}, final String oidcAutologinCookieValue: {})",
            request.getRequestURI(), context.getContextId(), user.getId(), state.size(), oidcAutologinCookieValue);
        final LoginRequest loginRequest = backend.getLoginRequest(request, user.getId(), context.getContextId(), loginConfiguration);

        LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest, new HashMap<String, Object>(), new LoginMethodClosure() {

            @Override
            public Authenticated doAuthentication(LoginResultImpl loginResult) throws OXException {
                Authenticated authenticated = enhanceAuthenticated(getDefaultAuthenticated(context, user), state);

                EnhancedAuthenticated enhanced = new EnhancedAuthenticated(authenticated) {

                    @Override
                    protected void doEnhanceSession(Session session) {
                        LOG.trace("doEnhanceSession(Session session: {})", session.getSessionID());
                        if (oidcAutologinCookieValue != null) {
                            session.setParameter(OIDCTools.SESSION_COOKIE, oidcAutologinCookieValue);
                        }
                        session.setParameter(OIDCTools.IDTOKEN, state.get(OIDCTools.IDTOKEN));
                        OIDCTools.addParameterToSession(session, state, OIDCTools.ACCESS_TOKEN, Session.PARAM_OAUTH_ACCESS_TOKEN);
                        OIDCTools.addParameterToSession(session, state, OIDCTools.REFRESH_TOKEN, Session.PARAM_OAUTH_REFRESH_TOKEN);
                        OIDCTools.addParameterToSession(session, state, OIDCTools.ACCESS_TOKEN_EXPIRY, Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE);
                        session.setParameter(OIDCTools.BACKEND_PATH, backend.getPath());
                    }
                };

                return enhanced;
            }
        });

        return loginResult;
    }

    private Authenticated enhanceAuthenticated(Authenticated defaultAuthenticated, final Map<String, String> state) {
        LOG.trace("enhanceAuthenticated(Authenticated defaultAuthenticated.userInfo: {}, final Map<String, String> state.size: {})", defaultAuthenticated.getUserInfo(), state != null ? state.size() : "null");
        Authenticated resultAuth = defaultAuthenticated;
        if (state != null) {
            resultAuth = backend.enhanceAuthenticated(defaultAuthenticated, state);
        }
        return resultAuth;
    }

    private Authenticated getDefaultAuthenticated(final Context context, final User user) {
        LOG.trace("getDefaultAuthenticated(final Context context: {}, final User user: {})", context.getContextId(), user.getId());
        return new Authenticated() {

            @Override
            public String getUserInfo() {
                return user.getLoginInfo();
            }

            @Override
            public String getContextInfo() {
                return context.getLoginInfo()[0];
            }
        };
    }

    private void sendRedirect(Session session, HttpServletRequest request, HttpServletResponse response, boolean respondWithJson) throws IOException, JSONException {
        LOG.trace("sendRedirect(Session session: {}, HttpServletRequest request: {}, HttpServletResponse response, boolean respondWithJson {})", session.getSessionID(), request.getRequestURI(), respondWithJson);
        String uiWebPath = OIDCTools.getUIWebPath(this.loginConfiguration, this.backend.getBackendConfig());
        String frontendRedirectLocation = OIDCTools.buildFrontendRedirectLocation(session, uiWebPath);
        if (respondWithJson) {
            writeSessionDataAsJson(session, response);
        } else {
            response.sendRedirect(frontendRedirectLocation);
        }
    }

    private void writeSessionDataAsJson(Session session, HttpServletResponse response) throws JSONException, IOException {
        LOG.trace("writeSessionDataAsJson(Session session {}, HttpServletResponse response)", session.getSessionID());
        JSONObject json = new JSONObject();
        json.putSafe("session", session.getSessionID());
        json.putSafe("user_id", session.getUserId());
        json.putSafe("context_id", session.getContextId());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(Charsets.UTF_8_NAME);
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        json.write(writer);
        writer.flush();
    }
}
