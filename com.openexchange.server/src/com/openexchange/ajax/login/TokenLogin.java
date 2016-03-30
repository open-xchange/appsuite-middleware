/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;

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
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doTokenLogin(req, resp);
        } catch (OXException e) {
            LoginServlet.logAndSendException(resp, e);
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

        String redirectURL = generateRedirectURL(LoginTools.encodeUrl(req.getParameter(LoginFields.UI_WEB_PATH_PARAM), true), LoginTools.encodeUrl(req.getParameter(LoginFields.AUTOLOGIN_PARAM), true), session, user.getPreferredLanguage(), conf.getUiWebPath(), request.getHttpSessionID(), serverToken);
        if (req.getParameter("jsonResponse") != null && req.getParameter("jsonResponse").equalsIgnoreCase("true")) {
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

    private static String generateRedirectURL(String uiWebPathParam, String shouldStore, Session session, String language, String uiWebPath, String httpSessionId, String serverToken) {
        String retval = uiWebPathParam;
        if (null == retval) {
            retval = uiWebPath;
        }
        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = LoginTools.addFragmentParameter(retval, "jsessionid", httpSessionId);
        retval = LoginTools.addFragmentParameter(retval, LoginFields.SERVER_TOKEN, serverToken);
        // App Suite UI requires some additional values.
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER, session.getLogin());
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER_ID, Integer.toString(session.getUserId()));
        retval = LoginTools.addFragmentParameter(retval, "language", language);
        // Pass through parameter
        if (shouldStore != null) {
            retval = LoginTools.addFragmentParameter(retval, "store", shouldStore);
        }
        // client side token should be added to the end by the client
        return retval;
    }
}
