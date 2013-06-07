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

package com.openexchange.realtime.client.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang.Validate;
import org.json.JSONException;
import org.json.JSONObject;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Cookie;
import com.ning.http.client.Response;
import com.ning.http.client.generators.InputStreamBodyGenerator;
import com.openexchange.realtime.client.Constants;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTUserState;

/**
 * {@link Login}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class Login {

    private static final String HTTP = "http";

    private static final String HTTPS = "https";
    
    private static final String JSESSIONID = "JSESSIONID";
    
    private static final String SECRET_KEY = "open-xchange-secret-key";
    
    private static final String SECRET_VALUE = "open-xchange-secret-value";
    
    public static RTUserState doLogin(final boolean secure, String host, int port, String username, String password) throws RTException {
        String loginUrl = buildLoginUrl(secure, host, port);
        String loginBody = buildLoginBody(Constants.LOGIN_ACTION, username, password, Constants.CLIENT_ID);

        try {
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            Future<Response> f = asyncHttpClient.preparePost(loginUrl)
                .setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .setBody(new InputStreamBodyGenerator(new ByteArrayInputStream(loginBody.getBytes("UTF-8"))))
                .execute();
            Response response = f.get();
            int statusCode = response.getStatusCode();
            
            if(statusCode != 200) {
                throw new RTException("Login failed, expected HTTP 200 status instead of " + statusCode);
            }
            
            return createUserState(response);
        } catch (UnsupportedEncodingException e) {
            throw new RTException("Login failed.", e);
        } catch (IOException e) {
            throw new RTException("Login failed.", e);
        } catch (InterruptedException e) {
            throw new RTException("Login failed.", e);
        } catch (ExecutionException e) {
            throw new RTException("Login failed.", e);
        }
    }

    /**
     * Build the request Body used for the login request.
     * 
     * @param action the login action to execute
     * @param username the username to user for the login request
     * @param password the password to use for the login request
     * @param clientID the clientID to use for the login request. This is used for cookie hash calculations
     * @return the built login body
     */
    private static String buildLoginBody(String action, String username, String password, String clientID) {
        Validate.notEmpty(action);
        Validate.notEmpty(username);
        Validate.notEmpty(password);
        Validate.notEmpty(clientID);
        
        StringBuilder sb = new StringBuilder();

        sb.append("action=").append(action);
        sb.append("&");
        sb.append("name=").append(username);
        sb.append("&");
        sb.append("password=").append(password);
        sb.append("&");
        sb.append("client=").append(clientID);

        return sb.toString();
    }

    /**
     * Build the login Url used to connect to the backend server.
     * 
     * @param secure if true use https for login, use http otherise
     * @param host ip/name of the backend server
     * @return the built login url
     */
    private static String buildLoginUrl(boolean secure, String host, int port) {
        Validate.notEmpty(host);
        Validate.isTrue( port == -1 || (port > 0 && port <= 65535), "The port must be between 1 and 65535: ", port);
        
        StringBuilder sb = new StringBuilder();

        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }

        if (secure) {
            sb.append(HTTPS);
        } else {
            sb.append(HTTP);
        }
        sb.append("://").append(host);
        
        if(port != -1) {
            sb.append(":").append(port);
        }
        
        sb.append(Constants.LOGIN_PATH);
        
        return sb.toString();
    }

    /**
     * Create a UserState that collects infos returned via the login response
     * 
     * @param the response the reveived server response
     * @return the RTUserState based on the values parsed from the server response
     * @Throws RTException if the server response couldn't be parsed
     */
    private static RTUserState createUserState(Response response) throws RTException {
        Validate.notNull(response, "Response must not be null");
        
        try {
            Map<String, String> cookieMap = mapCookies(response.getCookies());
            JSONObject responseBody = new JSONObject(response.getResponseBody());
            
            long contextID = responseBody.getLong("context_id");
            String sessionID = responseBody.getString("session");
            String locale = responseBody.getString("locale");
            String random = responseBody.getString("random");
            long userID = responseBody.getLong("user_id");
            String user = responseBody.getString("user");
            
            return new RTUserState(contextID, sessionID, locale, random, userID, user, cookieMap.get(SECRET_KEY), cookieMap.get(SECRET_VALUE), cookieMap.get(JSESSIONID));
        } catch (JSONException e) {
            throw new RTException(e);
        } catch (IOException e) {
            throw new RTException(e);
        }
    }

    /**
     * Parse a list of cookies for the needed values of jsessiond, open-xchange-secret-key, open-xchange-secret-value.
     * 
     * @param cookies A list of cookies to parse must not be null
     * @returns a map with the keys jsessiond, open-xchange-secret-key, open-xchange-secret-value
     * @throws RTException if one of the expected cookies is missing.
     */
    private static Map<String, String> mapCookies(List<Cookie> cookies) throws RTException {
        Validate.notNull(cookies, "ERROR: Cookie list mustn't be null!");
        
        Map<String, String> cookieMap = new HashMap<String, String>();
        String secretKey = null;
        String secretValue = null;
        String jsessionId = null;

        for (Cookie cookie : cookies) {
            if (cookie.getName().startsWith("open-xchange-secret-")) {
                secretKey = cookie.getName();
                secretValue = cookie.getValue();
            } else if (cookie.getName().equals(JSESSIONID)) {
                jsessionId = cookie.getValue();
            }
        }

        if (secretKey == null) {
            throw new RTException("Error during login. Secret cookie is missing in response.");
        }

        if (jsessionId == null) {
            throw new RTException("Error during login. JSESSIONID cookie is missing in response.");
        }

        cookieMap.put(JSESSIONID, jsessionId);
        cookieMap.put(SECRET_KEY, secretKey);
        cookieMap.put(SECRET_VALUE, secretValue);

        return cookieMap;
    }

}
