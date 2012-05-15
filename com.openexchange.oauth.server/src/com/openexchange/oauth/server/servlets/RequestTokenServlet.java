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
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.example.provider.core.SampleOAuthProvider;
import net.oauth.server.OAuthServlet;

/**
 * Request token request handler
 * 
 * @author Praveen Alavilli
 */
public class RequestTokenServlet extends HttpServlet {
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // nothing at this point
        try{
            SampleOAuthProvider.loadConsumers(config);
        }catch(IOException e){
            throw new ServletException(e.getMessage());
        }
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }
        
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        try {
            OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
            
            OAuthConsumer consumer = SampleOAuthProvider.getConsumer(requestMessage);
            
            OAuthAccessor accessor = new OAuthAccessor(consumer);
            SampleOAuthProvider.VALIDATOR.validateMessage(requestMessage, accessor);
            {
                // Support the 'Variable Accessor Secret' extension
                // described in http://oauth.pbwiki.com/AccessorSecret
                String secret = requestMessage.getParameter("oauth_accessor_secret");
                if (secret != null) {
                    accessor.setProperty(OAuthConsumer.ACCESSOR_SECRET, secret);
                }
            }
            // generate request_token and secret
            SampleOAuthProvider.generateRequestToken(accessor);
            
            response.setContentType("text/plain");
            OutputStream out = response.getOutputStream();
            OAuth.formEncode(OAuth.newList("oauth_token", accessor.requestToken,
                                           "oauth_token_secret", accessor.tokenSecret),
                             out);
            out.close();
            
        } catch (Exception e){
            SampleOAuthProvider.handleException(e, request, response, true);
        }
        
    }

    private static final long serialVersionUID = 1L;

}
