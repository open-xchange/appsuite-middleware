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
import java.io.OutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.example.provider.core.SampleOAuthProvider;
import net.oauth.server.OAuthServlet;

/**
 * Access Token request handler
 * 
 * @author Praveen Alavilli
 */
public class AccessTokenServlet extends HttpServlet {

    private static final long serialVersionUID = -1473491912970781256L;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        // nothing at this point
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        processRequest(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        processRequest(request, response);
    }

    public void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        try {
            final OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);

            final OAuthAccessor accessor = SampleOAuthProvider.getAccessor(requestMessage);
            SampleOAuthProvider.VALIDATOR.validateMessage(requestMessage, accessor);

            // make sure token is authorized
            if (!Boolean.TRUE.equals(accessor.getProperty("authorized"))) {
                final OAuthProblemException problem = new OAuthProblemException("permission_denied");
                throw problem;
            }
            // generate access token and secret
            SampleOAuthProvider.generateAccessToken(accessor);

            response.setContentType("text/plain");
            final OutputStream out = response.getOutputStream();
            OAuth.formEncode(OAuth.newList("oauth_token", accessor.accessToken, "oauth_token_secret", accessor.tokenSecret), out);
            out.close();

        } catch (final Exception e) {
            SampleOAuthProvider.handleException(e, request, response, true);
        }
    }

}
