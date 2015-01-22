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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.DefaultLoginInfo;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.osgi.Services;

/**
 * {@link AuthServlet}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AuthServlet extends HttpServlet {

    private static final long serialVersionUID = 3426581605901554006L;

    public static final String PATH = "authorize";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String redirect_uri = req.getParameter(OAuth.OAUTH_REDIRECT_URI);
        String response_type = req.getParameter(OAuth.OAUTH_RESPONSE_TYPE);

        req.setAttribute(OAuth.OAUTH_REDIRECT_URI, redirect_uri);
        req.setAttribute(OAuth.OAUTH_RESPONSE_TYPE, response_type);
        req.getRequestDispatcher("/authorize.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        OAuthProviderService service = Services.getService(OAuthProviderService.class);

        String redirectURI = validateRedirectURI(req);
        Authenticated authenticated = null;
        try {
            authenticated = handleLogin(req);
        } catch (OXException e1) {
            // TODO handle bad login
            e1.printStackTrace();
        }

        try {
            OAuthResponse response = OAuthASResponse
                .authorizationResponse(req, HttpServletResponse.SC_FOUND)
                .setCode(getCode(service, authenticated))
                .location(redirectURI)
                .buildQueryMessage();

            resp.sendRedirect(response.getLocationUri());
        } catch (OAuthSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param req
     * @return
     * @throws OXException
     */
    private Authenticated handleLogin(HttpServletRequest req) throws OXException {
        String login = req.getParameter("login");
        String password = req.getParameter("password");

        AuthenticationService authService = Services.getService(AuthenticationService.class);
        Authenticated authenticated = authService.handleLoginInfo(new DefaultLoginInfo(login, password));
        return authenticated;
    }

    private String getCode(OAuthProviderService provider, Authenticated authenticated) throws OXException {
        Context ctx = LoginPerformer.findContext(authenticated.getContextInfo());
        User user = LoginPerformer.findUser(ctx, authenticated.getUserInfo());
        return provider.generateAuthToken(ctx.getContextId(), user.getId());
    }

    private String validateRedirectURI(HttpServletRequest req) {
        //TODO
        return req.getParameter(OAuth.OAUTH_REDIRECT_URI);
    }
}
