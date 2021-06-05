/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.dav;

import static com.openexchange.java.Autoboxing.I;
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
import com.openexchange.dav.reports.SyncCollectionReportInfo;
import com.openexchange.dav.reports.SyncCollectionReportMethod;
import com.openexchange.dav.reports.SyncCollectionResponse;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.test.pool.TestUser;

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

    public WebDAVClient(TestUser testUser, String userAgent) throws OXException {
        this(testUser, userAgent, null);
    }

    public WebDAVClient(TestUser testUser, String userAgent, Grant oAuthGrant) throws OXException {
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
            this.setCredentials(testUser.getLogin(), testUser.getPassword());
        }
        this.setBaseURI(Config.getBaseUri());
        this.setUserAgent(userAgent);
    }

    private static HttpClient newOAuthHTTPClient() {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setAuthenticationPreemptive(false);
        httpClient.getParams().setParameter("http.protocol.single-cookie-header", Boolean.TRUE);
        httpClient.getParams().setParameter("http.protocol.max-redirects", I(0));
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

    public MultiStatusResponse[] doReport(final ReportMethod report, final int expectedStatus) throws HttpException, IOException {
        try {
            Assert.assertEquals("unexpected http status", expectedStatus, executeMethod(report));
            return report.getResponseBodyAsMultiStatus().getResponses();
        } catch (DavException e) {
            Assert.assertEquals("unexpected http status", expectedStatus, e.getErrorCode());
            return null;
        } finally {
            release(report);
        }
    }

    public SyncCollectionResponse doReport(SyncCollectionReportMethod report, int expectedStatus) throws HttpException, IOException {
        try {
            Assert.assertEquals("unexpected http status", expectedStatus, executeMethod(report));
            return report.getResponseBodyAsSyncCollection();
        } catch (DavException e) {
            Assert.assertEquals("unexpected http status", expectedStatus, e.getErrorCode());
            return null;
        } finally {
            release(report);
        }
    }

    public MultiStatusResponse[] doReport(final ReportMethod report) throws HttpException, IOException {
        return this.doReport(report, StatusCodes.SC_MULTISTATUS);
    }

    public MultiStatusResponse[] doReport(final ReportInfo reportInfo, final String uri) throws IOException {
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

    public SyncCollectionResponse doReport(SyncCollectionReportInfo reportInfo, String uri) throws IOException {
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

    public MultiStatusResponse[] doPropFind(final PropFindMethod propFind, final int expectedStatus) throws HttpException, IOException {
        try {
            int executeMethod = executeMethod(propFind);
            Assert.assertEquals("unexpected http status", expectedStatus, executeMethod);
            return propFind.getResponseBodyAsMultiStatus().getResponses();
        } catch (DavException e) {
            Assert.assertEquals("unexpected http status", expectedStatus, e.getErrorCode());
            return null;
        } finally {
            release(propFind);
        }
    }

    public MultiStatusResponse[] doPropPatch(PropPatchMethod propPatch, int expectedStatus) throws HttpException, IOException {
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

    public MultiStatusResponse[] doPropFind(final PropFindMethod propFind) throws HttpException, IOException {
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
