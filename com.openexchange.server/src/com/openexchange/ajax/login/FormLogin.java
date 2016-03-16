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

import static com.openexchange.ajax.AJAXServlet.CONTENTTYPE_HTML;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER_ID;
import static com.openexchange.ajax.login.AutoLoginTools.reAuthenticate;
import static com.openexchange.ajax.login.AutoLoginTools.tryAutologin;
import static com.openexchange.tools.servlet.http.Tools.filter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;

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
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doFormLogin(req, resp);
        } catch (OXException e) {
            String errorPage = conf.getErrorPageTemplate().replace("ERROR_MESSAGE", filter(e.getMessage()));
            resp.setContentType(CONTENTTYPE_HTML);
            resp.getWriter().write(errorPage);
        }
    }

    private void doFormLogin(HttpServletRequest req, HttpServletResponse resp) throws OXException, IOException {
        // Parse HTTP request
        LoginRequest request = LoginTools.parseLogin(
            req,
            LoginFields.LOGIN_PARAM,
            true,
            conf.getDefaultClient(),
            conf.isCookieForceHTTPS(),
            conf.isDisableTrimLogin(),
            !conf.isFormLoginWithoutAuthId());

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
        LoginServlet.addHeadersAndCookies(result, resp);
        resp.sendRedirect(generateRedirectURL(
            req.getParameter(LoginFields.UI_WEB_PATH_PARAM),
            req.getParameter(LoginFields.AUTOLOGIN_PARAM),
            session,
            user.getPreferredLanguage(),
            conf.getUiWebPath()));
    }

    private static String generateRedirectURL(String uiWebPathParam, String shouldStore, Session session, String language, String uiWebPath) {
        String retval = uiWebPathParam;
        if (null == retval) {
            retval = uiWebPath;
        }
        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_SESSION, session.getSessionID());
        // App Suite UI requires some additional values.
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER, session.getLogin());
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER_ID, Integer.toString(session.getUserId()));
        retval = LoginTools.addFragmentParameter(retval, "context_id", String.valueOf(session.getContextId()));
        retval = LoginTools.addFragmentParameter(retval, "language", language);
        if (shouldStore != null) {
            retval = LoginTools.addFragmentParameter(retval, "store", shouldStore);
        }
        return retval;
    }
}
