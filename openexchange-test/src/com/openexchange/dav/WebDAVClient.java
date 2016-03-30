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

package com.openexchange.dav;

import java.io.IOException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.junit.Assert;
import com.openexchange.ajax.oauth.provider.protocol.Grant;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.dav.reports.SyncCollectionReportInfo;
import com.openexchange.dav.reports.SyncCollectionReportMethod;
import com.openexchange.dav.reports.SyncCollectionResponse;
import com.openexchange.exception.OXException;

/**
 * {@link WebDAVClient}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class WebDAVClient {

    private final HttpClient httpClient;
    private final boolean useOAuth;
    private Grant oauthGrant;

    private String baseURI = null;

    public WebDAVClient(String userAgent) throws OXException {
        this(userAgent, null);
    }

    public WebDAVClient(String userAgent, Grant oAuthGrant) throws OXException {
        super();
        this.useOAuth = oAuthGrant != null;
        /*
         * init web client
         */
        if (useOAuth) {
            httpClient = newOAuthHTTPClient();
            this.oauthGrant = oAuthGrant;
        } else {
            httpClient = newDefaultHTTPClient();
            this.setCredentials(Config.getLogin(), Config.getPassword());
        }
        this.setBaseURI(Config.getBaseUri());
        this.setUserAgent(userAgent);
    }

    private static HttpClient newOAuthHTTPClient() {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setAuthenticationPreemptive(false);
        httpClient.getParams().setParameter("http.protocol.single-cookie-header", Boolean.TRUE);
        httpClient.getParams().setParameter("http.protocol.max-redirects", 0);
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        return httpClient;
    }

    private static HttpClient newDefaultHTTPClient() {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setAuthenticationPreemptive(true);
        httpClient.getParams().setParameter("http.protocol.single-cookie-header", Boolean.TRUE);
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        return httpClient;
    }

    /**
     * @return the baseUrl
     */
    public String getBaseURI() {
        return this.baseURI;
    }

    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseURI(final String baseURI) {
        this.baseURI = baseURI;
    }

    public void setUserAgent(final String userAgent) {
        this.httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);
    }

    public void setCredentials(final Credentials credentials) {
        this.httpClient.getState().setCredentials(AuthScope.ANY, credentials);
    }

    public void setCredentials(final String login, final String password) {
        this.setCredentials(new UsernamePasswordCredentials(login, password));
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public int executeMethod(final HttpMethod method) throws HttpException, IOException {
        if (useOAuth) {
            method.addRequestHeader("Authorization", "Bearer " + oauthGrant.getAccessToken());
        }
        return this.httpClient.executeMethod(method);
    }

    public void doPut(final PutMethod put, final int expectedStatus) throws HttpException, IOException {
        try {
            Assert.assertEquals("unexpected http status", expectedStatus, executeMethod(put));
        } finally {
            release(put);
        }
    }

    public void doPut(final PutMethod put) throws HttpException, IOException {
        this.doPut(put, StatusCodes.SC_CREATED);
    }

    public String doGet(GetMethod get, int expectedStatus) throws HttpException, IOException {
        try {
            Assert.assertEquals("unexpected http status", expectedStatus, executeMethod(get));
            return get.getResponseBodyAsString();
        } finally {
            release(get);
        }
    }

    public String doGet(GetMethod get) throws HttpException, IOException {
        return doGet(get, StatusCodes.SC_OK);
    }

    public MultiStatusResponse[] doReport(final ReportMethod report, final int expectedStatus) throws HttpException, IOException, DavException {
        try {
            Assert.assertEquals("unexpected http status", expectedStatus, executeMethod(report));
            return report.getResponseBodyAsMultiStatus().getResponses();
        } catch (final DavException e) {
            Assert.assertEquals("unexpected http status", expectedStatus, e.getErrorCode());
            return null;
        } finally {
            release(report);
        }
    }

    public SyncCollectionResponse doReport(SyncCollectionReportMethod report, int expectedStatus) throws HttpException, IOException, DavException {
        try {
            Assert.assertEquals("unexpected http status", expectedStatus, executeMethod(report));
            return report.getResponseBodyAsSyncCollection();
        } catch (final DavException e) {
            Assert.assertEquals("unexpected http status", expectedStatus, e.getErrorCode());
            return null;
        } finally {
            release(report);
        }
    }

    public MultiStatusResponse[] doReport(final ReportMethod report) throws HttpException, IOException, DavException {
        return this.doReport(report, StatusCodes.SC_MULTISTATUS);
    }

    public MultiStatusResponse[] doReport(final ReportInfo reportInfo, final String uri) throws ConfigurationException, IOException, DavException {
        ReportMethod report = null;
        MultiStatusResponse[] responses = null;
        try {
            report = new ReportMethod(uri, reportInfo);
            responses = this.doReport(report, StatusCodes.SC_MULTISTATUS);
        } finally {
            release(report);
        }
        Assert.assertNotNull("got no response", responses);
        return responses;
    }

    public SyncCollectionResponse doReport(SyncCollectionReportInfo reportInfo, String uri) throws ConfigurationException, IOException, DavException {
        SyncCollectionReportMethod report = null;
        SyncCollectionResponse response = null;
        try {
            report = new SyncCollectionReportMethod(uri, reportInfo);
            response = this.doReport(report, StatusCodes.SC_MULTISTATUS);
        } finally {
            release(report);
        }
        Assert.assertNotNull("got no response", response);
        return response;
    }

    public MultiStatusResponse[] doPropFind(final PropFindMethod propFind, final int expectedStatus) throws HttpException, IOException, DavException {
        try {
            int executeMethod = executeMethod(propFind);
            Assert.assertEquals("unexpected http status", expectedStatus, executeMethod);
            return propFind.getResponseBodyAsMultiStatus().getResponses();
        } catch (final DavException e) {
            Assert.assertEquals("unexpected http status", expectedStatus, e.getErrorCode());
            return null;
        } finally {
            release(propFind);
        }
    }

    public MultiStatusResponse[] doPropPatch(PropPatchMethod propPatch, int expectedStatus) throws HttpException, IOException, DavException {
        try {
            Assert.assertEquals("unexpected http status", expectedStatus, executeMethod(propPatch));
            return propPatch.getResponseBodyAsMultiStatus().getResponses();
        } catch (DavException e) {
            Assert.assertEquals("unexpected http status", expectedStatus, e.getErrorCode());
            return null;
        } finally {
            release(propPatch);
        }
    }

    public MultiStatusResponse[] doPropFind(final PropFindMethod propFind) throws HttpException, IOException, DavException {
        return this.doPropFind(propFind, StatusCodes.SC_MULTISTATUS);
    }

    public String doPost(PostMethod post) throws HttpException, IOException {
        return this.doPost(post, StatusCodes.SC_OK);
    }

    public String doPost(PostMethod post, int expectedStatus) throws HttpException, IOException {
        try {
            Assert.assertEquals("unexpected http status", expectedStatus, executeMethod(post));
            return post.getResponseBodyAsString();
        } finally {
            release(post);
        }
    }

    private static void release(final HttpMethod method) {
        if (null != method) {
            method.releaseConnection();
        }
    }
}
