package com.openexchange.ajax.session;

import java.io.IOException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.tools.servlet.AjaxException;

public class LoginTools {

    private LoginTools() {
        super();
    }
    
    public static LoginResponse login(final AJAXSession session,
        final LoginRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (LoginResponse) Executor.execute(session, request);
    }

    public static LoginResponse login(final AJAXSession session,
        final LoginRequest request, final String protocol, final String hostname) throws AjaxException, IOException,
        SAXException, JSONException {
        return (LoginResponse) Executor.execute(session, request, protocol, hostname);
    }

    public static LogoutResponse logout(final AJAXSession session,
        final LogoutRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (LogoutResponse) Executor.execute(session, request);
    }
	
    public static LogoutResponse logout(final AJAXSession session,
        final LogoutRequest request, final String protocol, final String hostname) throws AjaxException, IOException,
        SAXException, JSONException {
        return (LogoutResponse) Executor.execute(session, request, protocol, hostname);
    }

    public static RedirectResponse redirect(final AJAXSession session,
        final RedirectRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (RedirectResponse) Executor.execute(session, request);
    }
}
