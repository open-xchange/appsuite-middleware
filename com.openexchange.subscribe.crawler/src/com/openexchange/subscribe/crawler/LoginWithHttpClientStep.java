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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import com.gargoylesoftware.htmlunit.CrawlerWebConnection;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.WebResponseImpl;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.LoginStep;

/**
 * {@link LoginWithHttpClientStep} This step opens a single URL in HttpClient and passes this session on to the standard WebClient. This
 * allows a login for some exotic scenarios when all parameters of the relevant login-form are passed via URL. An example for this is
 * LinkedIn in its current (2010/03/25) incarnation.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class LoginWithHttpClientStep extends AbstractStep<Object, Object> implements LoginStep {

    private String url, regex;

    public LoginWithHttpClientStep() {

    }

    public LoginWithHttpClientStep(String description, String url, String regex) {
        this.description = description;
        this.url = url;
        this.regex = regex;
    }

    @Override
    public void execute(WebClient webClient) throws OXException {
        MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
        final HttpClient httpClient = new HttpClient(manager);

        try {
            GetMethod getMethod = new GetMethod(url);
            getMethod.setFollowRedirects(true);
            int code = httpClient.executeMethod(getMethod);

            String page = getMethod.getResponseBodyAsString();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(page);
            if (!matcher.find()) {
                throw SubscriptionErrorMessage.INVALID_LOGIN.create();
            }

            webClient.setWebConnection(new CrawlerWebConnection(webClient) {

                @Override
                public WebResponse getResponse(WebRequestSettings settings) throws IOException {
                    URL url = settings.getUrl();
                    GetMethod getMethod2 = new GetMethod(url.toString());
                    int statusCode = httpClient.executeMethod(getMethod2);

                    Header[] responseHeaders = getMethod2.getResponseHeaders();
                    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                    for (int i = 0; i < responseHeaders.length; i++) {
                        Header header = responseHeaders[i];
                        pairs.add(new NameValuePair(header.getName(), header.getValue()));
                    }

                    WebResponseData responseData = new WebResponseData(
                        getMethod2.getResponseBody(),
                        statusCode,
                        getMethod2.getStatusText(),
                        pairs);

                    HttpMethod method = HttpMethod.GET;
                    long loadTime = 23;

                    return new WebResponseImpl(responseData, url, method, loadTime);
                }

                @Override
                public HttpClient getHttpClient() {
                    return httpClient;
                }

            });

            executedSuccessfully = true;

        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(LoginWithHttpClientStep.class).error("", e);
        }
    }

    @Override
    public String getBaseUrl() {
        return "";
    }

    @Override
    public void setPassword(String password) {
        url = url.replace("PASSWORD", password);
    }

    @Override
    public void setUsername(String username) {
        url = url.replace("USERNAME", username);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getRegex() {
        return regex;
    }


    public void setRegex(String regex) {
        this.regex = regex;
    }

}
