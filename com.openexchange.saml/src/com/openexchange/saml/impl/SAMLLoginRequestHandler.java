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

package com.openexchange.saml.impl;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestContext;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.authentication.Authenticated;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.saml.SAMLSessionParameters;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.saml.tools.SAMLLoginTools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.SessionDescription;
import com.openexchange.session.reservation.EnhancedAuthenticated;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.user.UserService;


/**
 * {@link SAMLLoginRequestHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLLoginRequestHandler implements LoginRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLLoginRequestHandler.class);

    private final LoginConfigurationLookup loginConfigurationLookup;
    private final SAMLBackend backend;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link SAMLLoginRequestHandler}.
     */
    public SAMLLoginRequestHandler(SAMLBackend backend, LoginConfigurationLookup loginConfigurationLookup, ServiceLookup services) {
        super();
        this.backend = backend;
        this.loginConfigurationLookup = loginConfigurationLookup;
        this.services = services;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws IOException {
        LoginConfiguration loginConfiguration = loginConfigurationLookup.getLoginConfiguration();
        try {
            doSsoLogin(req, resp, loginConfiguration,requestContext);
            if(requestContext.getMetricProvider().isStateUnknown()) {
               requestContext.getMetricProvider().recordSuccess();
            }
        } catch (OXException e) {
            LoginTools.useErrorPageTemplateOrSendException(e, loginConfiguration.getErrorPageTemplate(), req, resp);
            requestContext.getMetricProvider().recordException(e);
        }
    }

    private void doSsoLogin(HttpServletRequest req, HttpServletResponse resp, LoginConfiguration conf, LoginRequestContext requestContext) throws OXException, IOException {
        String token = req.getParameter(SAMLLoginTools.PARAM_TOKEN);
        if (Strings.isEmpty(token)) {
            LOG.warn("SAML login requested without session reservation token: {}", token);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Reservation reservation;
        {
            SessionReservationService sessionReservationService = services.getServiceSafe(SessionReservationService.class);
            reservation = sessionReservationService.removeReservation(token);
            if (null == reservation) {
                LOG.warn("SAML login requested with invalid or expired session reservation token: {}", token);
                backend.getExceptionHandler().handleSessionReservationExpired(req, resp, token);
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        Context context;
        try {
            ContextService contextService = services.getServiceSafe(ContextService.class);
            context = contextService.getContext(reservation.getContextId());
            if (!context.isEnabled()) {
                LOG.info("Declining SAML login for user {} of context {}: Context is disabled.", I(reservation.getUserId()), I(reservation.getContextId()));
                backend.getExceptionHandler().handleContextDisabled(req, resp, reservation.getContextId());
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } catch (OXException e) {
            requestContext.getMetricProvider().recordException(e);
            if (ContextExceptionCodes.UPDATE.equals(e) || ContextExceptionCodes.UPDATE_NEEDED.equals(e)) {
                LOG.info("Declining SAML login for user {} of context {}: Running or pending update tasks.", I(reservation.getUserId()), I(reservation.getContextId()));
                backend.getExceptionHandler().handleUpdateTasksRunningOrPending(req, resp, reservation.getContextId());
                return;
            }
            throw e;
        }

        User user;
        {
            UserService userService = services.getServiceSafe(UserService.class);
            user = userService.getUser(reservation.getUserId(), context);
            if (!user.isMailEnabled()) {
                LOG.info("Declining SAML login for user {} of context {}: User is disabled.", I(reservation.getUserId()), I(reservation.getContextId()));
                backend.getExceptionHandler().handleUserDisabled(req, resp, reservation.getUserId(), reservation.getContextId());
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        {
            // try autologin (this is needed for the unsolicited response)
            String autologinRedirect = tryAutoLogin(req, resp, reservation, backend);
            if (null != autologinRedirect) {
                resp.sendRedirect(autologinRedirect);
                return;
            }
        }

        {
            // try autologin with SessionIndex
            String sessionIndexAutologinRedirect = trySessionIndexAutoLogin(req, reservation, backend);
            if (null != sessionIndexAutologinRedirect) {
                resp.sendRedirect(sessionIndexAutologinRedirect);
                return;
            }
        }

        // Do the login
        LoginResult result = login(req, context, user, reservation.getState(), conf);

        // Obtain associated session
        Session session = result.getSession();

        // Add session log properties
        LogProperties.putSessionProperties(session);

        // Add headers and cookies from login result
        LoginServlet.addHeadersAndCookies(result, resp);

        // Store session
        SessionUtility.rememberSession(req, new ServerSessionAdapter(session));
        LoginServlet.writeSecretCookie(req, resp, session, session.getHash(), req.isSecure(), req.getServerName(), conf);

        // Set session cookie
        LoginServlet.writeSessionCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName());

        // Send redirect
        String uiWebPath = req.getParameter(SAMLLoginTools.PARAM_LOGIN_PATH);
        if (Strings.isEmpty(uiWebPath)) {
            uiWebPath = conf.getUiWebPath();
        }

        String uriFragment = req.getParameter(SAMLLoginTools.PARAM_URI_FRAGMENT);
        resp.sendRedirect(SAMLLoginTools.buildFrontendRedirectLocation(session, uiWebPath, uriFragment));
    }

    protected LoginResult login(HttpServletRequest httpRequest, final Context context, final User user, final Map<String, String> optState, LoginConfiguration loginConfiguration) throws OXException {
        // The properties derived from optional state
        final Map<String, Object> props = optState == null ? new HashMap<String, Object>(4) : new HashMap<String, Object>(optState);


        // The login request
        final LoginRequest loginRequest = backend.prepareLoginRequest(httpRequest, loginConfiguration, user, context);

        // Do the login
        LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest, props, new LoginMethodClosure() {
            @Override
            public Authenticated doAuthentication(LoginResultImpl retval) throws OXException {
                Authenticated authenticated = enhanceAuthenticated(new AuthenticatedImpl(context.getLoginInfo()[0], user.getLoginInfo()), optState);
                return authenticated;
            }
        });

        return loginResult;
    }

    Authenticated enhanceAuthenticated(Authenticated authenticated, final Map<String, String> reservationState) {
        if (reservationState != null) {
            String samlAuthenticated = reservationState.get(SAMLSessionParameters.AUTHENTICATED);
            if (samlAuthenticated != null && Boolean.parseBoolean(samlAuthenticated)) {
                Authenticated enhanced = backend.enhanceAuthenticated(authenticated, reservationState);
                if (enhanced == null) {
                    enhanced = authenticated;
                }

                EnhancedAuthenticated wrapped = new EnhancedAuthenticated(enhanced) {
                    @Override
                    protected void doEnhanceSession(Session ses) {
                        SessionDescription session = (SessionDescription) ses;
                        session.setStaySignedIn(false);
                        session.setParameter(SAMLSessionParameters.AUTHENTICATED, Boolean.TRUE.toString());
                        String subjectID = reservationState.get(SAMLSessionParameters.SUBJECT_ID);
                        if (subjectID != null) {
                            session.setParameter(SAMLSessionParameters.SUBJECT_ID, subjectID);
                        }
                        String sessionIndex = reservationState.get(SAMLSessionParameters.SESSION_INDEX);
                        if (sessionIndex != null) {
                            session.setParameter(SAMLSessionParameters.SESSION_INDEX, sessionIndex);
                        }
                        String sessionNotOnOrAfter = reservationState.get(SAMLSessionParameters.SESSION_NOT_ON_OR_AFTER);
                        if (sessionNotOnOrAfter != null) {
                            session.setParameter(SAMLSessionParameters.SESSION_NOT_ON_OR_AFTER, sessionNotOnOrAfter);
                        }

                        String singleLogout = reservationState.get(SAMLSessionParameters.SINGLE_LOGOUT);
                        if (singleLogout != null) {
                            session.setParameter(SAMLSessionParameters.SINGLE_LOGOUT, Boolean.valueOf(singleLogout));
                        }

                        String samlPath = reservationState.get(SAMLSessionParameters.SAML_PATH);
                        if (samlPath != null) {
                            session.setParameter(SAMLSessionParameters.SAML_PATH, samlPath);
                        }

                        String accessToken = reservationState.get(SAMLSessionParameters.ACCESS_TOKEN);
                        if (accessToken != null) {
                            session.setParameter(Session.PARAM_OAUTH_ACCESS_TOKEN, accessToken);
                        }

                        String refreshToken = reservationState.get(SAMLSessionParameters.REFRESH_TOKEN);
                        if (refreshToken != null) {
                            session.setParameter(Session.PARAM_OAUTH_REFRESH_TOKEN, refreshToken);
                        }
                    }
                };

                return wrapped;
            }
        }

        // definitely no SAML session
        return authenticated;
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private static final class AuthenticatedImpl implements Authenticated {

        private final String contextInfo;

        private final String userInfo;

        /**
         * Initializes a new {@link AuthenticatedImpl}.
         *
         * @param contextInfo
         * @param userInfo
         */
        AuthenticatedImpl(String contextInfo, String userInfo) {
            super();
            this.contextInfo = contextInfo;
            this.userInfo = userInfo;
        }

        @Override
        public String getContextInfo() {
            return contextInfo;
        }

        @Override
        public String getUserInfo() {
            return userInfo;
        }

    }

    private String tryAutoLogin(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Reservation reservation, SAMLBackend samlBackend) throws OXException {
        if (samlBackend.getConfig().isAutoLoginEnabled()) {
            LoginConfiguration loginConfiguration = loginConfigurationLookup.getLoginConfiguration();
            Cookie sessionCookie = SAMLLoginTools.getSessionCookie(httpRequest, loginConfiguration);
            if (sessionCookie == null) {
                return null;
            }

            Session session = SAMLLoginTools.getSessionForSessionCookie(sessionCookie, services.getService(SessiondService.class));
            if (session == null) {
                // cookie exists but no according session was found => remove it
                Cookie toRemove = (Cookie) sessionCookie.clone();
                toRemove.setMaxAge(0);
                httpResponse.addCookie(toRemove);

                LOG.debug("Found no session for session cookie '{}' with value '{}'", sessionCookie.getName(), sessionCookie.getValue());
                return null;
            }
            int expectedContextId = reservation.getContextId();
            int expectedUserId = reservation.getUserId();
            if (expectedContextId != session.getContextId() || expectedUserId != session.getUserId()) {
                // wrong session
                LOG.debug("Session {} does not match expected session for reservation {}.", session.getSessionID(), reservation.getToken());
                return null;
            }

            try {
                LOG.debug("Found session '{}' for session cookie '{}' with value '{}'", session.getSessionID(), sessionCookie.getName(), sessionCookie.getValue());
                String hash = HashCalculator.getInstance().getHash(httpRequest, LoginTools.parseUserAgent(httpRequest), LoginTools.parseClient(httpRequest, false, loginConfiguration.getDefaultClient()));
                SAMLLoginTools.validateSession(httpRequest, session, hash, loginConfiguration);
                // compare against authInfo
                if (session.getContextId() == reservation.getContextId() && session.getUserId() == reservation.getUserId()) {
                    String uiWebPath = httpRequest.getParameter(SAMLLoginTools.PARAM_LOGIN_PATH);
                    if (Strings.isEmpty(uiWebPath)) {
                        uiWebPath = loginConfiguration.getUiWebPath();
                    }
                    return SAMLLoginTools.buildAbsoluteFrontendRedirectLocation(httpRequest, session, uiWebPath, services.getOptionalService(HostnameService.class));
                }
                LOG.debug("Session in session cookie is different to authInfo user and context");
            } catch (OXException e) {
                LOG.debug("Ignoring SAML auto-login attempt due to failed IP or secret check", e);
            }
        }

        return null;
    }

    private String trySessionIndexAutoLogin(HttpServletRequest httpRequest, Reservation reservation, SAMLBackend samlBackend) throws OXException {
        if (samlBackend.getConfig().isSessionIndexAutoLoginEnabled()) {
            SessiondService sessiondService = services.getService(SessiondService.class);

            Map<String, String> state = reservation.getState();
            String sessionIndex;
            if (null == state) {
                LOG.debug("Reservation does not have any state");
                return null;
            }
            sessionIndex = state.get(SAMLSessionParameters.SESSION_INDEX);
            if (null == sessionIndex) {
                LOG.debug("Reservation state does not include a SessionIndex");
                return null;
            }

            Collection<String> sessions = sessiondService.findSessions(SessionFilter.create("(" + SAMLSessionParameters.SESSION_INDEX + "=" + sessionIndex + ")"));
            if (null == sessions || sessions.size() <= 0) {
                LOG.debug("Found no session for SAML SessionIndex '{}'", sessionIndex);
                return null;
            }
            LoginConfiguration loginConfiguration = loginConfigurationLookup.getLoginConfiguration();
            String hash = HashCalculator.getInstance().getHash(httpRequest, LoginTools.parseUserAgent(httpRequest), LoginTools.parseClient(httpRequest, false, loginConfiguration.getDefaultClient()));
            // iterate over sessions

            for (String sessionString : sessions) {
                Session session = sessiondService.getSession(sessionString);
                if (session == null) {
                    LOG.debug("No session found with session identifier '{}'", sessionString);
                    continue;
                }
                try {
                    LOG.debug("Found session '{}' for SAML SessionIndex '{}'", session.getSessionID(), sessionIndex);
                    SAMLLoginTools.validateSession(httpRequest, session, hash, loginConfiguration);
                    // compare against authInfo
                    if (session.getContextId() == reservation.getContextId() && session.getUserId() == reservation.getUserId()) {
                        String uiWebPath = httpRequest.getParameter(SAMLLoginTools.PARAM_LOGIN_PATH);
                        if (Strings.isEmpty(uiWebPath)) {
                            uiWebPath = loginConfiguration.getUiWebPath();
                        }
                        LOG.debug("Session '{}' is the same for user '{}' in context '{}'",session.getSessionID(), I(session.getUserId()), I(session.getContextId()));
                        return SAMLLoginTools.buildAbsoluteFrontendRedirectLocation(httpRequest, session, uiWebPath, services.getOptionalService(HostnameService.class));
                    }
                    LOG.debug("Session in SAML auto-login cookie is different to authInfo user and context");
                } catch (OXException e) {
                    LOG.debug("Ignoring SAML auto-login attempt for session '{}' due to failed IP or secret check", sessionString, e);
                }
            }

        }
        return null;
    }
}
