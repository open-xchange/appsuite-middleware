package com.openexchange.oidc.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.authentication.Authenticated;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.spi.OIDCBackend;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;


public class OIDCLoginRequestHandler implements LoginRequestHandler {
    
    private LoginConfiguration loginConfiguration;
    private OIDCBackend backend;
    
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
    
    private void performLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException, OXException {
        String sessionToken = req.getParameter(OIDCTools.SESSION_TOKEN);
        if (Strings.isEmpty(sessionToken)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        SessionReservationService sessionReservationService = Services.getService(SessionReservationService.class);
        Reservation reservation = sessionReservationService.removeReservation(sessionToken);
        if (null == reservation) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        ContextService contextService = Services.getService(ContextService.class);
        Context context = contextService.getContext(reservation.getContextId());
        if (!context.isEnabled()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        UserService userService = Services.getService(UserService.class);
        User user = userService.getUser(reservation.getUserId(), context);
        if (!user.isMailEnabled()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        LoginResult result = loginUser(req, context, user, reservation.getState());

        Session session = result.getSession();
        
     // Add session log properties
        LogProperties.putSessionProperties(session);

        // Add headers and cookies from login result
        LoginServlet.addHeadersAndCookies(result, resp);

        // Store session
        SessionUtility.rememberSession(req, new ServerSessionAdapter(session));
        LoginServlet.writeSecretCookie(req, resp, session, session.getHash(), req.isSecure(), req.getServerName(), this.loginConfiguration);
        
        sendRedirect(session, req, resp);
    }

    private LoginResult loginUser(HttpServletRequest httpRequest, final Context context, final User user, final Map<String, String> state) throws OXException {
        final LoginRequest loginRequest = backend.getLoginRequest(httpRequest, user.getId(), context.getContextId(), loginConfiguration);

        //TODO QS-VS: Wirft eine Exception: Missing parameter in user's mail config: Session password not set.
        LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest, new HashMap<String, Object>(), new LoginMethodClosure() {
          
          @Override
          public Authenticated doAuthentication(LoginResultImpl loginResult) throws OXException {
              Authenticated authenticated = enhanceAuthenticated(getDefaultAuthenticated(context, user), state);
              return authenticated;
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
    
    private void sendRedirect(Session session, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
//        String uiWebPath = this.loginConfiguration.getUiWebPath();
        //TODO QS-VS: Load UI-Webpath from where??
        String uiWebPath = "/appsuite/ui";
        httpResponse.sendRedirect(OIDCTools.buildFrontendRedirectLocation(session, uiWebPath));
    }

}
