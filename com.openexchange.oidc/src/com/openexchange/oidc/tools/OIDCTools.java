package com.openexchange.oidc.tools;

import com.openexchange.java.Strings;

public class OIDCTools {

    public static final String SESSION_TOKEN = "sessionToken";
    
    public static final String LOGIN_ACTION = "loginAction";
    
    public static String getPathString(String path) {
        if (Strings.isEmpty(path)) {
            return "";
        }
        return path;
    }
}
