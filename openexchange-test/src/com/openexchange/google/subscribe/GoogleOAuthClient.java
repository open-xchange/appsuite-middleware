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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.google.subscribe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * {@link GoogleOAuthClient}
 * <p>
 * A Simple OAuth client for google. The flow is separated into 3 steps:
 * </p>
 * <ol>
 * <li>Login to Google in order to fetch the required cookies</li>
 * <li>Request an authorization code</li>
 * <li>Request an access token based on the authorization code received from the previous step.</li>
 * </ol>
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GoogleOAuthClient {

    private static final String LOGIN_SERVICE_URL = "https://accounts.google.com/ServiceLoginAuth";

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.143 Safari/537.36";

    private String cookies;

    private final PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();

    private TokenResponse token;

    private final HttpClient httpClient;

    /**
     * Initializes a new {@link GoogleOAuthClient}.
     * 
     * @throws Exception
     */
    public GoogleOAuthClient() {
        super();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(20);
        HttpHost googleoxoe = new HttpHost("google.oxoe.int", 80);
        connectionManager.setMaxPerRoute(new HttpRoute(googleoxoe), 50);
        httpClient = new DefaultHttpClient(connectionManager);
    }

    /**
     * Performs login in to google and fetches the required cookies (Step 0)
     */
    public void login(final String username, final String password) throws Exception {
        List<NameValuePair> postParams = getFormParams(getPageContent(LOGIN_SERVICE_URL), username, password);
        sendPost(LOGIN_SERVICE_URL, postParams, false);
    }

    /**
     * Request for authorization code (Step 1 for OAuth)
     */
    public String getAuthorizationCode(final String clientId, final String clientSecret, final String redirectUri, final Collection<String> scopes) throws Exception {
        String authorizationUrl = new GoogleAuthorizationCodeRequestUrl(clientId, redirectUri, scopes).build();

        HttpGet method = new HttpGet(authorizationUrl);
        setHeaders(method);

        HttpContext context = new BasicHttpContext();

        httpClient.execute(method, context);

        HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        String query = currentReq.getURI().getQuery();
        if (query.startsWith("code")) {
            return query.substring(5);
        } else {
            throw new Exception("Unexpected query: " + query);
        }
    }

    /**
     * Request for accessToken (Step 2 for OAuth)
     */
    public TokenResponse getAccessToken(final String clientId, final String clientSecret, final String authorizationCode, final String redirectUri) throws IOException {
        if (token == null) {
            GoogleAuthorizationCodeTokenRequest request = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                new JacksonFactory(),
                clientId,
                clientSecret,
                authorizationCode,
                redirectUri);
            token = request.execute();
        }
        return token;
    }

    private String sendPost(String url, List<NameValuePair> postParams, final boolean hasBody) throws Exception {
        HttpPost post = new HttpPost(url);
        setHeaders(post);
        post.setEntity(new UrlEncodedFormEntity(postParams));

        final HttpResponse response = httpClient.execute(post);
        if (hasBody) {
            return parseResponse(response);
        } else {
            return "";
        }
    }

    private String getPageContent(String url) throws Exception {
        HttpGet request = new HttpGet(url);
        setHeaders(request);

        HttpResponse response = httpClient.execute(request);
        final String result = parseResponse(response);

        // set cookies
        cookies = response.getFirstHeader("Set-Cookie") == null ? "" : response.getFirstHeader("Set-Cookie").toString();

        return result;
    }

    private List<NameValuePair> getFormParams(String html, String username, String password) throws UnsupportedEncodingException {
        Document doc = Jsoup.parse(html);

        // Google form id
        Element loginform = doc.getElementById("gaia_loginform");
        Elements inputElements = loginform.getElementsByTag("input");

        List<NameValuePair> paramList = new ArrayList<NameValuePair>();

        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            if (key.equals("Email")) {
                value = username;
            } else if (key.equals("Passwd")) {
                value = password;
            }

            paramList.add(new BasicNameValuePair(key, value));

        }

        return paramList;
    }

    private String parseResponse(final HttpResponse response) throws IllegalStateException, IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    private void setHeaders(final AbstractHttpMessage httpMessage) {
        httpMessage.setHeader("Host", "accounts.google.com");
        httpMessage.setHeader("User-Agent", USER_AGENT);
        httpMessage.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpMessage.setHeader("Accept-Language", "en-US,en;q=0.5");
        httpMessage.setHeader("Cookie", cookies);
        httpMessage.setHeader("Connection", "keep-alive");
        httpMessage.setHeader("Referer", LOGIN_SERVICE_URL);
        httpMessage.setHeader("Content-Type", "application/x-www-form-urlencoded");
    }

    /**
     * @param string
     * @throws IOException
     * @throws ClientProtocolException
     */
    public void requestCallback(String string) throws ClientProtocolException, IOException {
        HttpGet method = new HttpGet(string);
        setHeaders(method);
        httpClient.execute(method);
    }
}
