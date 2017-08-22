package com.openexchange.oidc.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.authentication.Authenticated;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.spi.OIDCBackend;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.EnhancedAuthenticated;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;


public class OIDCLoginRequestHandler implements LoginRequestHandler {
    
    private LoginConfiguration loginConfiguration;
    private OIDCBackend backend;
    //TODO QS-VS: Load UI-Webpath from where??
    private String uiWebPath = "/appsuite/ui";
    
    public OIDCLoginRequestHandler(LoginConfiguration loginConfiguration, OIDCBackend backend) {
        this.loginConfiguration = loginConfiguration;
        this.backend = backend;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        try {
            performLogin(req, resp);
        } catch (OXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    //TODO QS-VS: Struktur der Methode verbessern
    private void performLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, OXException {
        String sessionToken = request.getParameter(OIDCTools.SESSION_TOKEN);
        if (Strings.isEmpty(sessionToken)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        SessionReservationService sessionReservationService = Services.getService(SessionReservationService.class);
        Reservation reservation = sessionReservationService.removeReservation(sessionToken);
        if (null == reservation) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        String idToken = reservation.getState().get(OIDCTools.IDTOKEN);
        if (Strings.isEmpty(idToken)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        ContextService contextService = Services.getService(ContextService.class);
        Context context = contextService.getContext(reservation.getContextId());
        if (!context.isEnabled()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        UserService userService = Services.getService(UserService.class);
        User user = userService.getUser(reservation.getUserId(), context);
        if (!user.isMailEnabled()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        String autologinCookieValue = null;
        //TODO QS-VS: Entweder autologin oder normales login
        if (this.backend.getBackendConfig().isAutologinCookieEnabled()) {
            Cookie autologinCookie = this.loadAutologinCookie(request, reservation);
            if (autologinCookie != null) {
                String cookieRedirectURL = this.getAutologinByCookieURL(request, response, reservation, autologinCookie);
                if (cookieRedirectURL != null) {
                    response.sendRedirect(cookieRedirectURL);
                    return;
                }
            } else {
                autologinCookieValue = UUIDs.getUnformattedString(UUID.randomUUID());
            }
        }
        
        LoginResult result = loginUser(request, context, user, reservation.getState(), autologinCookieValue);

        Session session = performSessionAdditions(result, request, response, idToken);
        
        if (this.backend.getBackendConfig().isAutologinCookieEnabled()) {
            response.addCookie(getOIDCAutologinCookie(request, session, autologinCookieValue));
        }
        
        sendRedirect(session, request, response);
    }

    private String getAutologinByCookieURL(HttpServletRequest request, HttpServletResponse response, Reservation reservation, Cookie oidcAtologinCookie) throws OXException {
        if (oidcAtologinCookie != null) {
            Session session = this.getSessionFromAutologinCookie(oidcAtologinCookie);
            if (session != null) {
                return this.getRedirectLocationForSession(request, session, reservation);
            }
            //No session found, log that
        }
        
        
        if (oidcAtologinCookie != null) {
            Cookie toRemove = (Cookie) oidcAtologinCookie.clone();
            toRemove.setMaxAge(0);
            response.addCookie(toRemove);
        }
        
        return null;
    }

    private String getRedirectLocationForSession(HttpServletRequest request, Session session, Reservation reservation) throws OXException {
        OIDCTools.validateSession(session, request);
        String result = null;
        if (session.getContextId() == reservation.getContextId() && session.getUserId() == reservation.getUserId()) {
            result = OIDCTools.buildFrontendRedirectLocation(session, uiWebPath);
        }
        return result;
    }

    private Session getSessionFromAutologinCookie(Cookie oidcAtologinCookie) {
        Session session = null;
        try {
            SessiondService sessiondService = Services.getService(SessiondService.class);
            Collection<String> sessions = sessiondService.findSessions(SessionFilter.create("(" + OIDCTools.SESSION_COOKIE + "=" + oidcAtologinCookie.getValue() + ")"));
            if (sessions.size() > 0) {
                session = sessiondService.getSession(sessions.iterator().next());
            }
        } catch (OXException e) {
            
        }
        return session;
    }

    private Cookie loadAutologinCookie(HttpServletRequest request, Reservation reservation) throws OXException {
        String hash = HashCalculator.getInstance().getHash(request, LoginTools.parseUserAgent(request), LoginTools.parseClient(request, false, loginConfiguration.getDefaultClient()));
        Map<String, Cookie> cookies = Cookies.cookieMapFor(request);
        return cookies.get(OIDCTools.AUTOLOGIN_COOKIE_PREFIX + hash);
    }

    private Cookie getOIDCAutologinCookie(HttpServletRequest request, Session session, String uuid) {
        Cookie oidcAutologinCookie = new Cookie(OIDCTools.AUTOLOGIN_COOKIE_PREFIX + session.getHash(), uuid);
        oidcAutologinCookie.setPath("/");
        oidcAutologinCookie.setSecure(OIDCTools.considerSecure(request));
        oidcAutologinCookie.setMaxAge(-1);
        
        String domain = OIDCTools.getDomainName(request);
        String cookieDomain = Cookies.getDomainValue(domain);
        if (cookieDomain != null) {
            oidcAutologinCookie.setDomain(cookieDomain);
        }
        return oidcAutologinCookie;
        
    }
    
    private Session performSessionAdditions(LoginResult loginResult, HttpServletRequest request, HttpServletResponse response, String idToken) throws OXException {
        Session session = loginResult.getSession();
        
        LoginServlet.addHeadersAndCookies(loginResult, response);

        SessionUtility.rememberSession(request, new ServerSessionAdapter(session));
        
        LoginServlet.writeSecretCookie(request, response, session, session.getHash(), request.isSecure(), request.getServerName(), this.loginConfiguration);
        
        return session;
    }

    private LoginResult loginUser(HttpServletRequest request, final Context context, final User user, final Map<String, String> state, final String oidcAutologinCookieValue) throws OXException {
        final LoginRequest loginRequest = backend.getLoginRequest(request, user.getId(), context.getContextId(), loginConfiguration);

        //TODO QS-VS: Wirft eine Exception: Missing parameter in user's mail config: Session password not set.
        LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest, new HashMap<String, Object>(), new LoginMethodClosure() {
          
          @Override
          public Authenticated doAuthentication(LoginResultImpl loginResult) throws OXException {
              Authenticated authenticated = enhanceAuthenticated(getDefaultAuthenticated(context, user), state);
              
              EnhancedAuthenticated enhanced = new EnhancedAuthenticated(authenticated) {
                  @Override
                protected void doEnhanceSession(Session session) {
                    if (oidcAutologinCookieValue != null) {
                        session.setParameter(OIDCTools.SESSION_COOKIE, oidcAutologinCookieValue);
                    }
                    
                    session.setParameter(OIDCTools.IDTOKEN, state.get(OIDCTools.IDTOKEN));
                }
              };
              
              return enhanced;
          }
      });

        return loginResult;
    }
    
    private Authenticated enhanceAuthenticated(Authenticated defaultAuthenticated, final Map<String, String> state) {
        Authenticated resultAuth = defaultAuthenticated;
        if (state != null) {
            resultAuth = backend.enhanceAuthenticated(defaultAuthenticated, state);
        }
        return resultAuth;
    }
    
    private Authenticated getDefaultAuthenticated(final Context context, final User user) {
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
    
    private void sendRedirect(Session session, HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String uiWebPath = this.loginConfiguration.getUiWebPath();
        
        response.sendRedirect(OIDCTools.buildFrontendRedirectLocation(session, uiWebPath));
    }

}
