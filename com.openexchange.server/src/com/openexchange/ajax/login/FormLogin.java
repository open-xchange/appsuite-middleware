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

import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER_ID;
import static com.openexchange.ajax.login.AutoLoginTools.reAuthenticate;
import static com.openexchange.ajax.login.AutoLoginTools.tryAutologin;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.user.User;

/**
 * Implementes the formLogin action.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FormLogin implements LoginRequestHandler {

    private final LoginConfiguration conf;

    public FormLogin(LoginConfiguration conf) {
        super();
        this.conf = conf;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws IOException {
        try {
            doFormLogin(req, resp);
            requestContext.getMetricProvider().recordSuccess();
        } catch (OXException e) {
            LoginTools.useErrorPageTemplateOrSendException(e, conf.getErrorPageTemplate(), req, resp);
            requestContext.getMetricProvider().recordException(e);
        }
    }

    private void doFormLogin(HttpServletRequest req, HttpServletResponse resp) throws OXException, IOException {
        // Parse HTTP request
        LoginRequest request = LoginTools.parseLogin(req, LoginFields.LOGIN_PARAM, true, conf.getDefaultClient(), conf.isCookieForceHTTPS(), conf.isDisableTrimLogin(), !conf.isFormLoginWithoutAuthId());

        // Fill properties for re-authenticate attempt
        Map<String, Object> properties = new HashMap<String, Object>(4);
        {
            String capabilities = req.getParameter("capabilities");
            if (null != capabilities) {
                properties.put("client.capabilities", capabilities);
            }
            Map<String, List<String>> headers = request.getHeaders();
            if (headers != null) {
                properties.put("headers", headers);
            }
            com.openexchange.authentication.Cookie[] cookies = request.getCookies();
            if (null != cookies) {
                properties.put("cookies", cookies);
            }
        }

        // Try to lookup session by auto-login
        LoginResult result = reAuthenticate(tryAutologin(conf, req, resp, request.getHash()), request.getLogin(), request.getPassword(), properties);
        if (null == result) {
            // Continue with form login
            result = LoginPerformer.getInstance().doLogin(request, properties);
        }

        // Such a session is already available, so reuse it
        Session session = result.getSession();
        User user = result.getUser();

        Tools.disableCaching(resp);
        LoginServlet.writeSecretCookie(req, resp, session, session.getHash(), req.isSecure(), req.getServerName(), conf);
        LoginServlet.writeSessionCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName());
        LoginServlet.addHeadersAndCookies(result, resp);
        resp.sendRedirect(generateRedirectURL(req.getParameter(LoginFields.UI_WEB_PATH_PARAM), session, user.getPreferredLanguage(), conf.getUiWebPath()));
    }

    private static String generateRedirectURL(String uiWebPathParam, Session session, String language, String uiWebPath) {
        String retval = uiWebPathParam;
        if (null == retval) {
            retval = uiWebPath;
        }
        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_SESSION, session.getSessionID());
        // App Suite UI requires some additional values.
        // retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER, session.getLogin()); <--- Removed because login string might exposing sensitive user data; e.g. E-Mail address
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER_ID, Integer.toString(session.getUserId()));
        retval = LoginTools.addFragmentParameter(retval, "context_id", String.valueOf(session.getContextId()));
        retval = LoginTools.addFragmentParameter(retval, LoginFields.LANGUAGE_PARAM, language);
        retval = LoginTools.addFragmentParameter(retval, LoginFields.LOCALE_PARAM, language);
        retval = LoginTools.addFragmentParameter(retval, LoginFields.STORE_LOCALE, "true");
        return retval;
    }
}
