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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.server.OAuthServlet;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.internal.DatabaseOAuthProviderService;
import com.openexchange.oauth.provider.internal.OAuthProviderServiceLookup;

/**
 * Authorization request handler.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AuthorizationServlet extends AbstractAuthorizationServlet {

    private static final long serialVersionUID = 1107814765742902151L;

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        final OAuthProviderService providerService = getProviderService();
        try {
            final OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);

            final OAuthAccessor accessor = providerService.getAccessor(requestMessage);

            if (Boolean.TRUE.equals(accessor.getProperty(OAuthProviderConstants.PROP_AUTHORIZED))) {
                // already authorized send the user back
                returnToConsumer(request, response, accessor);
            } else {
                sendToAuthorizePage(request, response, accessor);
            }

        } catch (final Exception e) {
            DatabaseOAuthProviderService.handleException(e, request, response, true);
        }

    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        try {
            /*
             * Parse OAuth request message
             */
            final OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
            /*
             * Get accessor from memory
             */
            final OAuthProviderService providerService = getProviderService();
            final OAuthAccessor accessor = providerService.getAccessor(requestMessage);
            /*
             * User + context
             */
            final String login = request.getParameter("login");
            final String password = request.getParameter("password");
            if (isEmpty(login) || isEmpty(password)) {
                sendToAuthorizePage(request, response, accessor);
                return;
            }
            /*
             * Resolve login
             */
            final Map<String, Object> map = resolveLogin(login, password);
            /*
             * Set userId in accessor and mark it as authorized
             */
            accessor.setProperty(OAuthProviderService.PROP_LOGIN, login);
            accessor.setProperty(OAuthProviderService.PROP_PASSWORD, password);
            providerService.markAsAuthorized(accessor, ((User) map.get("user")).getId(), ((Context) map.get("context")).getContextId());
            /*
             * Return to consumer
             */
            returnToConsumer(request, response, accessor);
        } catch (final Exception e) {
            DatabaseOAuthProviderService.handleException(e, request, response, true);
        }
    }

    private void sendToAuthorizePage(final HttpServletRequest request, final HttpServletResponse response, final OAuthAccessor accessor) throws IOException, ServletException {
        String callback = request.getParameter("oauth_callback");
        if (isEmpty(callback)) {
            callback = "none";
        }
        // TODO: Open-Xchange authorize page
        final String consumer_description = accessor.consumer.getProperty("description");
        request.setAttribute("CONS_DESC", consumer_description);
        request.setAttribute("CALLBACK", callback);
        request.setAttribute("TOKEN", accessor.requestToken);
        request.getRequestDispatcher("/authorize.jsp").forward(request, response);
    }

    private void returnToConsumer(final HttpServletRequest request, final HttpServletResponse response, final OAuthAccessor accessor) throws IOException, ServletException {
        /*
         * Send the user back to site's callBackUrl
         */
        String callback = request.getParameter("oauth_callback");
        if ("none".equals(callback) && !isEmpty(accessor.consumer.callbackURL)) {
            // first check if we have something in our properties file
            callback = accessor.consumer.callbackURL;
        }
        /*
         * Check call-back
         */
        if ("none".equals(callback)) {
            // no call back it must be a client
            response.setContentType("text/plain");
            final PrintWriter out = response.getWriter();
            out.println("You have successfully authorized '" + accessor.consumer.getProperty("description") + "'. Please close this browser window and click continue in the client.");
            out.flush();
        } else {
            /*
             * If callback is not passed in, use the callback from config
             */
            if (callback == null || callback.length() <= 0) {
                callback = accessor.consumer.callbackURL;
            }
            final String token = accessor.requestToken;
            if (token != null) {
                callback = OAuth.addParameters(callback, "oauth_token", token);
            }
            /*
             * Set redirect headers
             */
            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            response.setHeader("Location", callback);
        }
    }

    private OAuthProviderService getProviderService() {
        return OAuthProviderServiceLookup.getService(OAuthProviderService.class);
    }

}
