package com.openexchange.ajax.session;

import java.io.IOException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession.AJAXSession;
import com.openexchange.tools.servlet.AjaxException;

public class LoginTools {

    private LoginTools() {
        super();
    }
    
    public static LoginResponse login(final AJAXSession session,
        final LoginRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (LoginResponse) AJAXClient.execute(session, request);
    }

    public static LogoutResponse logout(final AJAXSession session,
        final LogoutRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (LogoutResponse) AJAXClient.execute(session, request);
    }

    public static RedirectResponse redirect(final AJAXSession session,
        final RedirectRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (RedirectResponse) AJAXClient.execute(session, request);
    }
}
