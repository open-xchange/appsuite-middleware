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
        //TODO QS-VS: Entweder autologin oder normales login
        LoginResult result = loginUser(request, context, user, reservation.getState());

        Session session = performSessionAdditions(result, request, response, idToken);
        
        sendRedirect(session, request, response);
    }
    
    private Session performSessionAdditions(LoginResult loginResult, HttpServletRequest request, HttpServletResponse response, String idToken) throws OXException {
        Session session = loginResult.getSession();
        session.setParameter(OIDCTools.IDTOKEN, idToken);
        
        LoginServlet.addHeadersAndCookies(loginResult, response);

        SessionUtility.rememberSession(request, new ServerSessionAdapter(session));
        LoginServlet.writeSecretCookie(request, response, session, session.getHash(), request.isSecure(), request.getServerName(), this.loginConfiguration);
        return session;
    }

    private LoginResult loginUser(HttpServletRequest request, final Context context, final User user, final Map<String, String> state) throws OXException {
        final LoginRequest loginRequest = backend.getLoginRequest(request, user.getId(), context.getContextId(), loginConfiguration);

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
    
    private void sendRedirect(Session session, HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String uiWebPath = this.loginConfiguration.getUiWebPath();
        //TODO QS-VS: Load UI-Webpath from where??
        String uiWebPath = "/appsuite/ui";
        response.sendRedirect(OIDCTools.buildFrontendRedirectLocation(session, uiWebPath));
    }

}
