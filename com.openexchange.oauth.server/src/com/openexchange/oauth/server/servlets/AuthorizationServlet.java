/*
 * Copyright 2007 AOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openexchange.oauth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.example.provider.core.SampleOAuthProvider;
import net.oauth.server.OAuthServlet;

/**
 * Autherization request handler.
 * 
 * @author Praveen Alavilli
 */
public class AuthorizationServlet extends HttpServlet {

    private static final long serialVersionUID = 1107814765742902151L;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        try {
            final OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);

            final OAuthAccessor accessor = SampleOAuthProvider.getAccessor(requestMessage);

            if (Boolean.TRUE.equals(accessor.getProperty("authorized"))) {
                // already authorized send the user back
                returnToConsumer(request, response, accessor);
            } else {
                sendToAuthorizePage(request, response, accessor);
            }

        } catch (final Exception e) {
            SampleOAuthProvider.handleException(e, request, response, true);
        }

    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        try {
            final OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);

            final OAuthAccessor accessor = SampleOAuthProvider.getAccessor(requestMessage);

            final String userId = request.getParameter("userId");
            if (userId == null) {
                sendToAuthorizePage(request, response, accessor);
            }
            // set userId in accessor and mark it as authorized
            SampleOAuthProvider.markAsAuthorized(accessor, userId);

            returnToConsumer(request, response, accessor);

        } catch (final Exception e) {
            SampleOAuthProvider.handleException(e, request, response, true);
        }
    }

    private void sendToAuthorizePage(final HttpServletRequest request, final HttpServletResponse response, final OAuthAccessor accessor) throws IOException, ServletException {
        String callback = request.getParameter("oauth_callback");
        if (isEmpty(callback)) {
            callback = "none";
        }
        final String consumer_description = (String) accessor.consumer.getProperty("description");
        request.setAttribute("CONS_DESC", consumer_description);
        request.setAttribute("CALLBACK", callback);
        request.setAttribute("TOKEN", accessor.requestToken);
        request.getRequestDispatcher //
        ("/authorize.jsp").forward(request, response);

    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private void returnToConsumer(final HttpServletRequest request, final HttpServletResponse response, final OAuthAccessor accessor) throws IOException, ServletException {
        // send the user back to site's callBackUrl
        String callback = request.getParameter("oauth_callback");
        if ("none".equals(callback) && accessor.consumer.callbackURL != null && accessor.consumer.callbackURL.length() > 0) {
            // first check if we have something in our properties file
            callback = accessor.consumer.callbackURL;
        }

        if ("none".equals(callback)) {
            // no call back it must be a client
            response.setContentType("text/plain");
            final PrintWriter out = response.getWriter();
            out.println("You have successfully authorized '" + accessor.consumer.getProperty("description") + "'. Please close this browser window and click continue" + " in the client.");
            out.close();
        } else {
            // if callback is not passed in, use the callback from config
            if (callback == null || callback.length() <= 0) {
                callback = accessor.consumer.callbackURL;
            }
            final String token = accessor.requestToken;
            if (token != null) {
                callback = OAuth.addParameters(callback, "oauth_token", token);
            }

            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            response.setHeader("Location", callback);
        }
    }

}
