package com.openexchange.oidc.tools;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.tools.servlet.http.Cookies;

public class OIDCTools {

    public static final String SESSION_TOKEN = "sessionToken";
    
    public static final String LOGIN_ACTION = "loginAction";

    public static final String IDTOKEN = "idToken";

    public static final String TYPE = "type";
    
    public static final String END = "end";

    public static final String STATE = "state";

    public static final String AUTOLOGIN_COOKIE_PREFIX = "open-xchange-oidc-";

    public static final String SESSION_COOKIE = "com.openexchange.oidc.SessionCookie";

    public static final String SUBJECT = "sub";
    
    public static String getPathString(String path) {
        if (Strings.isEmpty(path)) {
            return "";
        }
        return path;
    }
    
    /**
     * Generates the relative redirect location to enter the web frontend directly with a session.
     *
     * @param session The session
     * @param uiWebPath The path to use
     */
    public static String buildFrontendRedirectLocation(Session session, String uiWebPath) {
        String retval = uiWebPath;

        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_SESSION, session.getSessionID());
        return retval;
    }
    
    public static URI getURIFromPath(String path) throws OXException{
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_URI.create(e, path);
        }
    }
    
    public static String getDomainName(HttpServletRequest request) {
        HostnameService hostnameService = Services.getService(HostnameService.class);
        if (hostnameService == null) {
            return request.getServerName();
        }

        String hostname = hostnameService.getHostname(-1, -1);
        if (hostname == null) {
            return request.getServerName();
        }

        return hostname;
    }
    
    public static boolean considerSecure(final HttpServletRequest request) {
        final ConfigurationService configurationService = Services.getService(ConfigurationService.class);
        if (configurationService != null && configurationService.getBoolProperty(ServerConfig.Property.FORCE_HTTPS.getPropertyName(), false) && !Cookies.isLocalLan(request)) {
            // HTTPS is enforced by configuration
            return true;
        }
        return request.isSecure();
    }
    
    public static void validateSession(Session session, HttpServletRequest request) throws OXException {
        SessionUtility.checkIP(session, request.getRemoteAddr());
        Map<String, Cookie> cookies = Cookies.cookieMapFor(request);
        Cookie secretCookie = cookies.get(LoginServlet.SECRET_PREFIX + session.getHash());
        if (secretCookie == null || !session.getSecret().equals(secretCookie.getValue())) {
            throw SessionExceptionCodes.WRONG_SESSION_SECRET.create(session.getSessionID());
        }
    }
}
