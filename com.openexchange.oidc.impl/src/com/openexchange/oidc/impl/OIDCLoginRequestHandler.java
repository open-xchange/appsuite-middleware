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
import com.openexchange.ajax.SessionUtility;import com.openexchange.ajax.login.LoginRequestContext;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCBackendConfig.AutologinMode;
import com.openexchange.oidc.OIDCBackendProperty;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;
import com.openexchange.session.SessionDescription;
import com.openexchange.session.reservation.EnhancedAuthenticated;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import static com.openexchange.java.Autoboxing.I;

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
    private OIDCHandler handler;

    /**
     * Initializes a new {@link OIDCLoginRequestHandler}.
     *
     * @param backend The back-end to use
     */
    public OIDCLoginRequestHandler(OIDCBackend backend) {
        super();
        this.handler = new OIDCHandler(backend);
    }

    public void setOIDCHandler(OIDCHandler handler) {
       this.handler = handler;
    }

    public static class OIDCHandler {

        private final OIDCBackend backend;

        @SuppressWarnings("hiding")
        static final Logger LOG = LoggerFactory.getLogger(OIDCLoginRequestHandler.OIDCHandler.class);

        public OIDCHandler(OIDCBackend backend) {
            this.backend = backend;
        }

        private void handleException(HttpServletResponse response, boolean respondWithJson, OXException oxException, int sc) throws OXException, IOException {
            LOG.trace("handleException (HttpServletResponse response, boolean respondWithJson {}, OXException oxException {}, int sc {})", respondWithJson ? Boolean.TRUE : Boolean.FALSE, oxException, I(sc));
            if (respondWithJson) {
                throw oxException;
            }
            response.sendError(sc);
        }

        private String getRedirectLocationForSession(HttpServletRequest request, Session session, Reservation reservation) throws OXException, IOException {
            LOG.trace("getRedirectLocationForSession(HttpServletRequest request: {}, Session session: {}, Reservation reservation: {})", request.getRequestURI(), session.getSessionID(), reservation.getToken());
            OIDCTools.validateSession(session, request);
            if (session.getContextId() != reservation.getContextId() && session.getUserId() != reservation.getUserId()) {
                this.handleException(null, true, LoginExceptionCodes.LOGIN_DENIED.create(), 0);
            }
            return OIDCTools.buildFrontendRedirectLocation(session, OIDCTools.getUIWebPath(backend.getLoginConfiguration(), backend.getBackendConfig()), request.getParameter(OIDCTools.PARAM_DEEP_LINK));
        }

        private boolean getAutologinByCookieURL(HttpServletRequest request, HttpServletResponse response, Reservation reservation, Cookie oidcAtologinCookie, boolean respondWithJson) throws IOException {
            LOG.trace("getAutologinByCookieURL(HttpServletRequest request: {}, HttpServletResponse response, Reservation reservation.token: {}, Cookie oidcAtologinCookie: {}, boolean respondWithJson)", request.getRequestURI(), reservation.getToken(), oidcAtologinCookie != null ? oidcAtologinCookie.getValue() : "null", respondWithJson ? Boolean.TRUE : Boolean.FALSE);
            if (oidcAtologinCookie != null) {
                try {
                    Session session = OIDCTools.getSessionFromAutologinCookie(oidcAtologinCookie, request);
                    if (session != null) {
                        backend.updateSession(session, reservation.getState());
                        // the getRedirectLocationForSession does also the validation check of the session
                        if (respondWithJson) {
                            this.writeSessionDataAsJson(session, response);
                        } else {
                            String redirectLocationForSession = getRedirectLocationForSession(request, session, reservation);
                            response.sendRedirect(redirectLocationForSession);
                        }
                        return true;
                    }
                    //No session found, log that
                    LOG.debug("No session found for OIDC Cookie with value: {}", oidcAtologinCookie.getValue());
                } catch (OXException | JSONException e) {
                    LOG.debug("Ignoring OIDC auto-login attempt due to failed IP or secret check", e);
                }

                Cookie toRemove = (Cookie) oidcAtologinCookie.clone();
                toRemove.setMaxAge(0);
                response.addCookie(toRemove);
            }
            return false;
        }

        /**
         * Perform an Appsuite login. If autologin is enabled, the user will be logged in into his session. The
         * autologin mechanism will check if a valid OIDCCookie is available with all needed information. The
         * method will redirect the user to the Appsuite UI afterwards. If <code>respondWithJson</code> is set true,
         * the redirect location will be wrapped into a JSON Object.
         *
         * @param request The {@link HttpServletRequest}
         * @param response The {@link HttpServletResponse}
         * @param respondWithJson Should the UI location should be wrapped into a JSON Object or not
         * @throws IOException
         * @throws OXException
         * @throws JSONException
         */
        private boolean performCookieLogin(HttpServletRequest request, HttpServletResponse response, Reservation reservation, boolean respondWithJson) throws IOException {
            LOG.trace("performCookieLogin(HttpServletRequest request: {}, HttpServletResponse response, Reservation reservation.token: {}, boolean respondWithJson)", request.getRequestURI(), reservation.getToken(), respondWithJson ? Boolean.TRUE : Boolean.FALSE);
            AutologinMode autologinMode = OIDCBackendConfig.AutologinMode.get(backend.getBackendConfig().autologinCookieMode());

            if (autologinMode == OIDCBackendConfig.AutologinMode.SSO_REDIRECT) {
                try {
                    Cookie autologinCookie = OIDCTools.loadAutologinCookie(request, backend.getLoginConfiguration());
                    return getAutologinByCookieURL(request, response, reservation, autologinCookie, respondWithJson);
                } catch (OXException e) {
                    LOG.debug("Ignoring OIDC auto-login attempt due to failed IP or secret check", e);
                }
            }
            return false;
        }

        void performLogin(HttpServletRequest request, HttpServletResponse response, boolean respondWithJson, LoginRequestContext requestContext) throws IOException, OXException, JSONException {
            LOG.trace("performLogin(HttpServletRequest request: {}, HttpServletResponse response)", request.getRequestURI());
            String sessionToken = request.getParameter(OIDCTools.SESSION_TOKEN);
            LOG.trace("Login user with session token: {}", sessionToken);
            if (Strings.isEmpty(sessionToken)) {
                handleException(response, respondWithJson, AjaxExceptionCodes.BAD_REQUEST.create(), HttpServletResponse.SC_BAD_REQUEST);
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            SessionReservationService sessionReservationService = Services.getService(SessionReservationService.class);
            Reservation reservation = sessionReservationService.removeReservation(sessionToken);
            if (null == reservation) {
                handleException(response, respondWithJson, LoginExceptionCodes.INVALID_CREDENTIALS.create(), HttpServletResponse.SC_FORBIDDEN);
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String idToken = reservation.getState().get(OIDCTools.IDTOKEN);
            if (Strings.isEmpty(idToken)) {
                handleException(response, respondWithJson, AjaxExceptionCodes.BAD_REQUEST.create(), HttpServletResponse.SC_BAD_REQUEST);
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            ContextService contextService = Services.getService(ContextService.class);
            Context context = contextService.getContext(reservation.getContextId());
            if (!context.isEnabled()) {
                handleException(response, respondWithJson, LoginExceptionCodes.INVALID_CREDENTIALS.create(), HttpServletResponse.SC_FORBIDDEN);
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            UserService userService = Services.getService(UserService.class);
            User user = userService.getUser(reservation.getUserId(), context);
            if (!user.isMailEnabled()) {
                handleException(response, respondWithJson, LoginExceptionCodes.INVALID_CREDENTIALS.create(), HttpServletResponse.SC_FORBIDDEN);
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            boolean autologinEnabled = backend.getBackendConfig().isAutologinEnabled();

            String autologinCookieValue = null;
            if (autologinEnabled) {
                LOG.trace("Try OIDC auto-login with a cookie");
                if (performCookieLogin(request, response, reservation, respondWithJson)) {
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

        private Session performSessionAdditions(LoginResult loginResult, HttpServletRequest request, HttpServletResponse response, String idToken) throws OXException {
            LOG.trace("performSessionAdditions(LoginResult loginResult.sessionID: {}, HttpServletRequest request: {}, HttpServletResponse response, String idToken: {})", loginResult.getSession().getSessionID(), request.getRequestURI(), idToken);
            Session session = loginResult.getSession();

            LoginServlet.addHeadersAndCookies(loginResult, response);

            SessionUtility.rememberSession(request, new ServerSessionAdapter(session));

            LoginServlet.writeSecretCookie(request, response, session, session.getHash(), request.isSecure(), request.getServerName(), backend.getLoginConfiguration());

            return session;
        }

        private void writeSessionDataAsJson(Session session, HttpServletResponse response) throws JSONException, IOException {
            LOG.trace("writeSessionDataAsJson(Session session {}, HttpServletResponse response)", session.getSessionID());
            JSONObject json = new JSONObject();
            json.putSafe("session", session.getSessionID());
            json.putSafe("user_id", I(session.getUserId()));
            json.putSafe("context_id", I(session.getContextId()));
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding(Charsets.UTF_8_NAME);
            response.setContentType("application/json");
            PrintWriter writer = response.getWriter();
            json.write(writer);
            writer.flush();
        }

        private Cookie createOIDCAutologinCookie(HttpServletRequest request, Session session, String uuid) throws OXException {
            LOG.trace("createOIDCAutologinCookie(HttpServletRequest request: {}, Session session: {}, String uuid: {})", request.getRequestURI(), session.getSessionID(), uuid);
            String hash = OIDCTools.calculateHash(request, backend.getLoginConfiguration());
            Cookie oidcAutologinCookie = new Cookie(OIDCTools.AUTOLOGIN_COOKIE_PREFIX + hash, uuid);
            oidcAutologinCookie.setPath("/");
            oidcAutologinCookie.setSecure(Tools.considerSecure(request));
            oidcAutologinCookie.setMaxAge(-1);

            String domain = OIDCTools.getDomainName(request, Services.getOptionalService(HostnameService.class));
            String cookieDomain = Cookies.getDomainValue(domain);
            if (cookieDomain != null) {
                oidcAutologinCookie.setDomain(cookieDomain);
            }
            return oidcAutologinCookie;
        }

        private void sendRedirect(Session session, HttpServletRequest request, HttpServletResponse response, boolean respondWithJson) throws IOException, JSONException {
            LOG.trace("sendRedirect(Session session: {}, HttpServletRequest request: {}, HttpServletResponse response, boolean respondWithJson {})", session.getSessionID(), request.getRequestURI(), respondWithJson ? Boolean.TRUE : Boolean.FALSE);
            if (respondWithJson) {
                this.writeSessionDataAsJson(session, response);
            } else {
                String uiWebPath = OIDCTools.getUIWebPath(backend.getLoginConfiguration(), backend.getBackendConfig());
                // get possible deeplink
                String frontendRedirectLocation = OIDCTools.buildFrontendRedirectLocation(session, uiWebPath, request.getParameter(OIDCTools.PARAM_DEEP_LINK));
                response.sendRedirect(frontendRedirectLocation);
            }
        }

        private LoginResult loginUser(HttpServletRequest request, final Context context, final User user, final Map<String, String> state, final String oidcAutologinCookieValue) throws OXException {
            LOG.trace("loginUser(HttpServletRequest request: {}, final Context context: {}, final User user: {}, final Map<String, String> state.size: {}, final String oidcAutologinCookieValue: {})", request.getRequestURI(), I(context.getContextId()), I(user.getId()), I(state.size()), oidcAutologinCookieValue);
            final LoginRequest loginRequest = backend.getLoginRequest(request, user.getId(), context.getContextId(), backend.getLoginConfiguration());

            final OIDCBackend colosureBackend = backend;
            return LoginPerformer.getInstance().doLogin(loginRequest, new HashMap<String, Object>(), new LoginMethodClosure() {

                @Override
                public Authenticated doAuthentication(LoginResultImpl loginResult) throws OXException {
                    Authenticated authenticated = colosureBackend.enhanceAuthenticated(OIDCTools.getDefaultAuthenticated(context, user, state), state);

                    return new EnhancedAuthenticated(authenticated) {

                        @Override
                        protected void doEnhanceSession(Session ses) {
                            LOG.trace("doEnhanceSession(Session session: {})", ses.getSessionID());
                            SessionDescription session = (SessionDescription) ses;
                            session.setStaySignedIn(false);
                            if (oidcAutologinCookieValue != null) {
                                session.setParameter(OIDCTools.SESSION_COOKIE, oidcAutologinCookieValue);
                            }

                            Map<String, String> params = new HashMap<>(state);
                            params.put(OIDCTools.BACKEND_PATH, colosureBackend.getPath());
                            OIDCTools.setSessionParameters(session, params);
                        }
                    };
                }
            });
        }
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, LoginRequestContext requestContext) throws IOException {
        boolean respondWithJson = false;
        try {
            respondWithJson = respondWithJson(request);
            handler.performLogin(request, response, respondWithJson, requestContext);
            if(requestContext.getMetricProvider().isStateUnknown()) {
                requestContext.getMetricProvider().recordSuccess();
            }
        } catch (OXException e) {
            LOG.error(e.getLocalizedMessage(), e);
            if (respondWithJson) {
                try {
                    requestContext.getMetricProvider().recordException(e);
                    ResponseWriter.writeException(e, new JSONWriter(
                        response.getWriter()).object());
                } catch (JSONException jsonError) {
                    LOG.error(e.getLocalizedMessage(), jsonError);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean respondWithJson(HttpServletRequest request) {
        LOG.trace("respondWithJson (HttpServletRequest request: {})", request.getRequestURI());
        String acceptHeader = request.getHeader("Accept");
        return (null != acceptHeader && acceptHeader.equalsIgnoreCase("application/json"));
    }
}
