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

package com.openexchange.ajax.login;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER_ID;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.user.User;

/**
 * Implements the tokenLogin action.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TokenLogin implements LoginRequestHandler {

    private final LoginConfiguration conf;

    public TokenLogin(LoginConfiguration conf) {
        super();
        this.conf = conf;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws IOException {
        try {
            doTokenLogin(req, resp);
            if(requestContext.getMetricProvider().isStateUnknown()) {
                requestContext.getMetricProvider().recordSuccess();
            }
        } catch (OXException e) {
            LoginServlet.logAndSendException(resp, e);
            requestContext.getMetricProvider().recordException(e);
        }
    }

    private void doTokenLogin(HttpServletRequest req, HttpServletResponse resp) throws OXException, IOException {
        LoginRequestImpl request = LoginTools.parseLogin(req, LoginFields.LOGIN_PARAM, true, conf.getDefaultClient(), conf.isCookieForceHTTPS(), conf.isDisableTrimLogin(), true);
        request.setClientToken(LoginTools.parseParameter(req, LoginFields.CLIENT_TOKEN, true, null));
        Map<String, Object> properties = new HashMap<String, Object>(1);
        {
            String capabilities = req.getParameter("capabilities");
            if (null != capabilities) {
                properties.put("client.capabilities", capabilities);
            }
        }
        LoginResult result = LoginPerformer.getInstance().doLogin(request, properties);
        String serverToken = result.getServerToken();
        if (null == serverToken) {
            throw LoginExceptionCodes.SERVER_TOKEN_NOT_CREATED.create();
        }
        Session session = result.getSession();
        User user = result.getUser();

        Tools.disableCaching(resp);

        String redirectURL = generateRedirectURL(LoginTools.encodeUrl(req.getParameter(LoginFields.UI_WEB_PATH_PARAM), true), session, user.getPreferredLanguage(), conf.getUiWebPath(), request.getHttpSessionID(), serverToken);
        if (AJAXRequestDataTools.parseBoolParameter(req.getParameter("jsonResponse"))) {
            // Client demands no redirect, but a JSON response
            JSONObject response = new JSONObject();
            try {
                response.put("serverToken", serverToken);
                response.put("jsessionid", request.getHttpSessionID());
                response.put(PARAMETER_USER, session.getLogin());
                response.put(PARAMETER_USER_ID, session.getUserId());
                response.put("url", redirectURL);
                response.write(resp.getWriter());
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
            }
        } else {
            resp.sendRedirect(redirectURL);
        }
    }

    private static String generateRedirectURL(String uiWebPathParam, Session session, String language, String uiWebPath, String httpSessionId, String serverToken) {
        String retval = uiWebPathParam;
        if (null == retval) {
            retval = uiWebPath;
        }
        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = LoginTools.addFragmentParameter(retval, "jsessionid", httpSessionId);
        retval = LoginTools.addFragmentParameter(retval, LoginFields.SERVER_TOKEN, serverToken);
        // App Suite UI requires some additional values.
        // retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER, session.getLogin()); <--- Removed because login string might exposing sensitive user data; e.g. E-Mail address
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER_ID, Integer.toString(session.getUserId()));
        retval = LoginTools.addFragmentParameter(retval, LoginFields.LANGUAGE_PARAM, language);
        retval = LoginTools.addFragmentParameter(retval, LoginFields.LOCALE_PARAM, language);
        // Pass through parameter
        // client side token should be added to the end by the client
        return retval;
    }
}
