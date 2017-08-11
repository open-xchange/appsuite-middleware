package com.openexchange.oidc.tools;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import java.net.URI;
import java.net.URISyntaxException;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.session.Session;

public class OIDCTools {

    public static final String SESSION_TOKEN = "sessionToken";
    
    public static final String LOGIN_ACTION = "loginAction";

    public static final String IDTOKEN = "idToken";

    public static final String TYPE = "type";
    
    public static final String END = "end";

    public static final String STATE = "state";
    
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
}
