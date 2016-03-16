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

package com.openexchange.subscribe.crawler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.client.httpclient4.HttpClientPool;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.HasLoginPage;
import com.openexchange.subscribe.crawler.internal.LoginStep;

/**
 * {@link StringByOAuthRequestStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class StringByOAuthRequestStep extends AbstractStep<String, Object> implements LoginStep, HasLoginPage {

    private String username, password, consumerSecret, consumerKey, requestUrl, authorizationUrl, accessUrl, callbackUrl;

    private String requestToken, tokenSecret;
    private String nameOfUserField, nameOfPasswordField;
    private String accessToken, apiRequest;

    private OAuthAccessor oAuthAccessor;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StringByOAuthRequestStep.class);

    private Page loginPage;

    private final SingleConnectionPool connectionPool = new SingleConnectionPool();

    public StringByOAuthRequestStep() {
    }

    public StringByOAuthRequestStep(final String username, final String password, final String consumerSecret, final String consumerKey, final String requestUrl, final String authorizationUrl, final String accessUrl, final String callbackUrl, final String nameOfUserField, final String nameOfPasswordField, final String apiRequest) {
        this.username = username;
        this.password = password;
        this.consumerSecret = consumerSecret;
        this.consumerKey = consumerKey;
        this.requestUrl = requestUrl;
        this.authorizationUrl = authorizationUrl;
        this.accessUrl = accessUrl;
        this.callbackUrl = callbackUrl;
        this.nameOfPasswordField = nameOfPasswordField;
        this.nameOfUserField = nameOfUserField;
        this.apiRequest = apiRequest;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.AbstractStep#execute(com.gargoylesoftware.htmlunit.WebClient)
     */
    @Override
    public void execute(final WebClient webClient) throws OXException {

        try {
            // Request the OAuth token
            final OAuthClient client = new OAuthClient(new HttpClient4(connectionPool));
            try {
                oAuthAccessor = createOAuthAccessor();
                client.getRequestToken(oAuthAccessor);
                requestToken = oAuthAccessor.requestToken;
                tokenSecret = oAuthAccessor.tokenSecret;

                LOG.info("Successfully requested OAuth token");
            } catch (final IOException e) {
                LOG.error(e.toString());
            } catch (final URISyntaxException e) {
                LOG.error(e.toString());
            } catch (final NullPointerException e) {
                LOG.error(e.toString());
            } catch (final OAuthException e) {
                LOG.error(e.toString());
            }

            // Authorize the request
            String verifier = "";
            try {
                oAuthAccessor = createOAuthAccessor();
                final Properties paramProps = new Properties();
                paramProps.setProperty("application_name", "Open-Xchange Contact Aggregator");
                paramProps.setProperty("oauth_token", requestToken);
                final OAuthMessage response = sendRequest(paramProps, oAuthAccessor.consumer.serviceProvider.userAuthorizationURL);
                LOG.info("Successfully requested authorization-url: {}", response.URL);

                // Fill out form / confirm the access otherwise
                final LoginPageByFormActionRegexStep authorizeStep = new LoginPageByFormActionRegexStep("", response.URL,  username, password, "/uas/oauth/authorize/submit", nameOfUserField, nameOfPasswordField, ".*", 1, "");
                authorizeStep.execute(webClient);
                loginPage = authorizeStep.getLoginPage();
                final HtmlPage pageWithVerifier = authorizeStep.getOutput();
                final String pageString2 = pageWithVerifier.getWebResponse().getContentAsString();
                LOG.debug("Page contains the verifier : {}", pageString2.contains("access-code"));
                LOG.debug("Cookie-Problem : {}", pageString2.contains("Please make sure you have cookies"));

                // get the verifier
                final Pattern pattern = Pattern.compile("access-code\">([0-9]*)<");
                final Matcher matcher = pattern.matcher(pageString2);
                if (matcher.find() && matcher.groupCount() == 1){
                    verifier = matcher.group(1);
                    LOG.info("Request authorized, verifier found.");
                } else {
                    LOG.error("Verifier not found");
                }
                LOG.debug("This is the verifier : {}", verifier);
                //openPageInBrowser(pageWithVerifier);
            } catch (final IOException e) {
                LOG.error(e.toString());
            } catch (final URISyntaxException e) {
                LOG.error(e.toString());
            } catch (final OAuthException e) {
                LOG.error(e.toString());
            }


            // Access and confirm using the verifier
            try {
                final Properties paramProps = new Properties();
                paramProps.setProperty("oauth_token", requestToken);
                //not in OAuth-Spec and maybe specific to linkedin
                paramProps.setProperty("oauth_verifier", verifier);
                final OAuthMessage response = sendRequest(paramProps, accessUrl);
                accessToken = response.getParameter("oauth_token");
                tokenSecret = response.getParameter("oauth_token_secret");
                LOG.info("Accessed and conformed using the verifier");
            } catch (final IOException e) {
                LOG.error(e.toString());
            } catch (final URISyntaxException e) {
                LOG.error(e.toString());
            } catch (final OAuthException e) {
                LOG.error(e.toString());
            }

            // Execute an API-Request (fully logged in now)
            try {
                final Properties paramProps = new Properties();
                paramProps.setProperty("oauth_token", accessToken);

                final OAuthMessage response = sendRequest(paramProps, apiRequest);
                final String result = response.readBodyAsString();
                LOG.info("Successfully executed an API-Request");
                executedSuccessfully = true;
                LOG.debug("This is the result of the whole operation : {}", result);
                output = result;
            } catch (final IOException e) {
                LOG.error(e.toString());
            } catch (final URISyntaxException e) {
                LOG.error(e.toString());
            } catch (final OAuthException e) {
                LOG.error(e.toString());
            }

        } finally {
            // Ensure connections are closed
            connectionPool.shutdown();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.LoginStep#getBaseUrl()
     */
    @Override
    public String getBaseUrl() {
        return "";
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.LoginStep#setPassword(java.lang.String)
     */
    @Override
    public void setPassword(final String password) {
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.LoginStep#setUsername(java.lang.String)
     */
    @Override
    public void setUsername(final String username) {
        this.username = username;
    }

    private OAuthAccessor createOAuthAccessor() {
        final OAuthServiceProvider provider = new OAuthServiceProvider(requestUrl, authorizationUrl, accessUrl);
        final OAuthConsumer consumer = new OAuthConsumer(callbackUrl, consumerKey, consumerSecret, provider);
        return new OAuthAccessor(consumer);
    }

    private OAuthMessage sendRequest(final Map map, final String url) throws IOException, URISyntaxException, OAuthException {
        final List<Map.Entry> params = new ArrayList<Map.Entry>();
        final Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry p = (Map.Entry) it.next();
            params.add(new OAuth.Parameter((String) p.getKey(), (String) p.getValue()));
        }
        final OAuthAccessor accessor = createOAuthAccessor();
        accessor.tokenSecret = tokenSecret;

        final OAuthClient client = new OAuthClient(new HttpClient4(connectionPool));
        return client.invoke(accessor, "GET", url, params);
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(final String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(final String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(final String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(final String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(final String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(final String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(final String requestToken) {
        this.requestToken = requestToken;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(final String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    public String getNameOfUserField() {
        return nameOfUserField;
    }


    public void setNameOfUserField(final String nameOfUserField) {
        this.nameOfUserField = nameOfUserField;
    }


    public String getNameOfPasswordField() {
        return nameOfPasswordField;
    }


    public void setNameOfPasswordField(final String nameOfPasswordField) {
        this.nameOfPasswordField = nameOfPasswordField;
    }


    public String getApiRequest() {
        return apiRequest;
    }


    public void setApiRequest(final String apiRequest) {
        this.apiRequest = apiRequest;
    }


    @Override
    public Page getLoginPage() {
        return loginPage;
    }

    private static class SingleConnectionPool implements HttpClientPool {

        private DefaultHttpClient client;

        @Override
        public HttpClient getHttpClient(final URL server) {
            if(client != null) {
                return client;
            }
            client = new DefaultHttpClient();
            final ClientConnectionManager mgr = client.getConnectionManager();
            if (!(mgr instanceof ThreadSafeClientConnManager)) {
                final HttpParams params = client.getParams();
                client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
            }

            return client;
        }

        public void shutdown() {
            client.getConnectionManager().shutdown();
        }

    }


}
