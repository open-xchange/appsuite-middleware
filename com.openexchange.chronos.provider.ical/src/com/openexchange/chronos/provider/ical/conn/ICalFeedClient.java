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

package com.openexchange.chronos.provider.ical.conn;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import com.openexchange.auth.info.AuthInfo;
import com.openexchange.auth.info.AuthType;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.provider.ical.ICalCalendarFeedConfig;
import com.openexchange.chronos.provider.ical.auth.AdvancedAuthInfo;
import com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionCodes;
import com.openexchange.chronos.provider.ical.osgi.Services;
import com.openexchange.chronos.provider.ical.properties.ICalCalendarProviderProperties;
import com.openexchange.chronos.provider.ical.result.GetResponse;
import com.openexchange.chronos.provider.ical.result.GetResponseState;
import com.openexchange.chronos.provider.ical.utils.ICalProviderUtils;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;
import com.openexchange.session.Session;

/**
 *
 * {@link ICalFeedClient}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalFeedClient {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalFeedClient.class);

    protected final ICalCalendarFeedConfig iCalFeedConfig;
    protected final Session session;

    public ICalFeedClient(Session session, ICalCalendarFeedConfig iCalFeedConfig) {
        this.session = session;
        this.iCalFeedConfig = iCalFeedConfig;
    }

    private HttpGet prepareGet() {
        HttpGet request = new HttpGet(iCalFeedConfig.getFeedUrl());
        request.addHeader(HttpHeaders.ACCEPT, "text/calendar");
        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
        if (Strings.isNotEmpty(iCalFeedConfig.getEtag())) {
            request.addHeader(HttpHeaders.IF_NONE_MATCH, iCalFeedConfig.getEtag());
        }
        String ifModifiedSince = DateUtils.formatDate(new Date(iCalFeedConfig.getLastUpdated()));
        if (Strings.isNotEmpty(ifModifiedSince)) {
            request.setHeader(HttpHeaders.IF_MODIFIED_SINCE, ifModifiedSince);
        }

        handleAuth(request);
        return request;
    }

    protected void handleAuth(HttpRequestBase method) {
        AdvancedAuthInfo authInfo = this.iCalFeedConfig.getAuthInfo();
        AuthType authType = authInfo.getAuthType();
        switch (authType) {
            case BASIC: {
                StringBuilder auth = new StringBuilder();
                String login = authInfo.getLogin();
                if (Strings.isNotEmpty(login)) {
                    auth.append(login);
                }
                auth.append(':');
                String password = authInfo.getPassword();
                if (Strings.isNotEmpty(password)) {
                    auth.append(password);
                }

                byte[] encodedAuth = Base64.encodeBase64(auth.toString().getBytes(Charset.forName("ISO-8859-1")));
                String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.US_ASCII);

                method.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
            }
                break;
            case TOKEN: {
                String token = authInfo.getToken();
                String authHeader = "Token token=" + token;
                method.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
            }
                break;
            case NONE:
            default:
                break;
        }
    }

    private ImportedCalendar importCalendar(HttpEntity httpEntity) throws OXException {
        if (null == httpEntity) {
            return null;
        }
        ICalService iCalService = Services.getService(ICalService.class);
        ICalParameters parameters = iCalService.initParameters();
        parameters.set(ICalParameters.IGNORE_UNSET_PROPERTIES, Boolean.TRUE);
        try (InputStream inputStream = Streams.bufferedInputStreamFor(httpEntity.getContent())) {
            return iCalService.importICal(inputStream, parameters);
        } catch (UnsupportedOperationException | IOException e) {
            LOG.error("Error while processing the retrieved information:{}.", e.getMessage(), e);
            throw ICalProviderExceptionCodes.UNEXPECTED_FEED_ERROR.create(iCalFeedConfig.getFeedUrl(), e.getMessage());
        }
    }

    /**
     * Returns the calendar behind the given feed URL if available. If there is no update (based on etag and last modification header) the contained calendar will be <code>null</code>.
     *
     * @return GetResponse containing the feed content
     */
    public GetResponse executeRequest() throws OXException {
        ICalProviderUtils.verifyURI(this.iCalFeedConfig.getFeedUrl());
        HttpGet request = prepareGet();

        try (CloseableHttpResponse response = ICalFeedHttpClient.getInstance().execute(request)) {
            int statusCode = assertStatusCode(response);
            if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
                // OK, nothing was modified, no response body, return as is
                return new GetResponse(request.getURI(), GetResponseState.NOT_MODIFIED, response.getAllHeaders());
            } else if (statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                return new GetResponse(request.getURI(), GetResponseState.REMOVED, response.getAllHeaders());
            }
            // Prepare the response
            return prepareResponse(request.getURI(), response);
        } catch (ClientProtocolException e) {
            LOG.error("Error while processing the retrieved information:{}.", e.getMessage(), e);
            throw ICalProviderExceptionCodes.CLIENT_PROTOCOL_ERROR.create(e, e.getMessage());
        } catch (UnknownHostException | NoHttpResponseException e) {
            LOG.debug("Error while processing the retrieved information:{}.", e.getMessage(), e);
            throw ICalProviderExceptionCodes.NO_FEED.create(e, iCalFeedConfig.getFeedUrl());
        } catch (IOException e) {
            LOG.error("Error while processing the retrieved information:{}.", e.getMessage(), e);
            throw ICalProviderExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            reset(request);
        }
    }

    private GetResponse prepareResponse(URI uri, HttpResponse httpResponse) throws OXException {
        GetResponse response = new GetResponse(uri, GetResponseState.MODIFIED, httpResponse.getAllHeaders());

        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
            return response;
        }
        long contentLength = entity.getContentLength();
        String contentLength2 = response.getContentLength();

        long allowedFeedSize = ICalCalendarProviderProperties.allowedFeedSize();
        if (contentLength > allowedFeedSize || (Strings.isNotEmpty(contentLength2) && Long.parseLong(contentLength2) > allowedFeedSize)) {
            throw ICalProviderExceptionCodes.FEED_SIZE_EXCEEDED.create(iCalFeedConfig.getFeedUrl(), allowedFeedSize, contentLength2 != null ? contentLength2 : contentLength);
        }
        response.setCalendar(importCalendar(entity));
        return response;
    }

    /**
     * Asserts the status code for any errors
     *
     * @param httpResponse The {@link HttpResponse}'s status code to assert
     * @return The status code
     * @throws OXException if an HTTP error is occurred (4xx or 5xx)
     */
    private int assertStatusCode(HttpResponse httpResponse) throws OXException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        // Assert the 4xx codes
        switch (statusCode) {
            case HttpStatus.SC_UNAUTHORIZED:
                throw unauthorizedException(httpResponse);
            case HttpStatus.SC_NOT_FOUND:
                throw ICalProviderExceptionCodes.NO_FEED.create(iCalFeedConfig.getFeedUrl());
        }
        if (statusCode >= 400 && statusCode <= 499) {
            throw ICalProviderExceptionCodes.UNEXPECTED_FEED_ERROR.create(iCalFeedConfig.getFeedUrl(), "Unknown server response. Status code " + statusCode);
        }

        // Assert the 5xx codes
        switch (statusCode) {
            case 500:
                throw ICalProviderExceptionCodes.REMOTE_INTERNAL_SERVER_ERROR.create(httpResponse.getStatusLine().getReasonPhrase());
            case 503:
                LOG.info("The previously existing feed '{}' seems to be removed.", iCalFeedConfig.getFeedUrl());
                return statusCode;
        }
        if (statusCode >= 500 && statusCode <= 599) {
            throw ICalProviderExceptionCodes.REMOTE_SERVER_ERROR.create(String.valueOf(httpResponse.getStatusLine()));
        }
        return statusCode;
    }

    /**
     * Resets given HTTP request
     *
     * @param request The HTTP request
     */
    protected static void reset(HttpRequestBase request) {
        if (null != request) {
            try {
                request.reset();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    public static void reset() {
        ICalFeedHttpClient.reset();
    }

    private static class ICalFeedHttpClient {

        private static volatile CloseableHttpClient httpClient;

        static CloseableHttpClient getInstance() {
            CloseableHttpClient tmp = httpClient;
            if (tmp == null) {
                synchronized (ICalFeedHttpClient.class) {
                    tmp = httpClient;
                    if (tmp == null) {
                        ClientConfig config = ClientConfig.newInstance();
                        config.setUserAgent("Open-Xchange Calendar Feed Client");
                        init(config);
                        tmp = HttpClients.getHttpClient(config, Services.getService(SSLSocketFactoryProvider.class), Services.getService(SSLConfigurationService.class));
                        httpClient = tmp;
                    }
                }
            }
            return tmp;
        }

        static void init(ClientConfig config) {
            LeanConfigurationService leanConfigurationService = Services.getService(LeanConfigurationService.class);
            int maxConnections = leanConfigurationService.getIntProperty(ICalCalendarProviderProperties.maxConnections);
            config.setMaxTotalConnections(maxConnections);

            int maxConnectionsPerRoute = leanConfigurationService.getIntProperty(ICalCalendarProviderProperties.maxConnectionsPerRoute);
            config.setMaxConnectionsPerRoute(maxConnectionsPerRoute);

            int connectionTimeout = leanConfigurationService.getIntProperty(ICalCalendarProviderProperties.connectionTimeout);
            config.setConnectionTimeout(connectionTimeout);

            int socketReadTimeout = leanConfigurationService.getIntProperty(ICalCalendarProviderProperties.socketReadTimeout);
            config.setSocketReadTimeout(socketReadTimeout);
        }

        static void reset() {
            CloseableHttpClient tmp = httpClient;
            if (tmp != null) {
                httpClient = null;
                HttpClients.shutDown(tmp);
            }
        }
    }

    /**
     * Prepares an appropriate exception for a response with status <code>401 Unauthorized</code>.
     *
     * @param response The HTTP response to generate the exception for
     * @return An appropriate {@link OXException}
     */
    private OXException unauthorizedException(HttpResponse response) {
        String feedUrl = iCalFeedConfig.getFeedUrl();
        AuthInfo authInfo = iCalFeedConfig.getAuthInfo();
        
        boolean hadCredentials = null != authInfo && (Strings.isNotEmpty(authInfo.getPassword()) || Strings.isNotEmpty(authInfo.getToken()));
        String realm = getFirstHeaderElement(response, HttpHeaders.WWW_AUTHENTICATE, "Basic realm");
        if (null != realm && realm.contains("Share/Anonymous/")) {
            /*
             * anonymous, password-protected share
             */
            if (hadCredentials) {
                return ICalProviderExceptionCodes.PASSWORD_WRONG.create(feedUrl, String.valueOf(response.getStatusLine()), realm);
            }
            return ICalProviderExceptionCodes.PASSWORD_REQUIRED.create(feedUrl, String.valueOf(response.getStatusLine()), realm);
        }
        /*
         * generic credentials required, otherwise
         */
        if (hadCredentials) {
            if (iCalFeedConfig.getLastUpdated() > 0 || Strings.isNotEmpty(iCalFeedConfig.getEtag())) {
                return ICalProviderExceptionCodes.CREDENTIALS_CHANGED.create(feedUrl, String.valueOf(response.getStatusLine()), realm);
            }
            return ICalProviderExceptionCodes.CREDENTIALS_WRONG.create(feedUrl, String.valueOf(response.getStatusLine()), realm);
        }
        return ICalProviderExceptionCodes.CREDENTIALS_REQUIRED.create(feedUrl, String.valueOf(response.getStatusLine()), realm);
    }

    private static String getFirstHeaderElement(HttpResponse response, String headerName, String elementName) {
        Header header = response.getFirstHeader(headerName);
        if (null != header) {
            HeaderElement[] elements = header.getElements();
            if (null != elements && 0 < elements.length) {
                for (HeaderElement element : elements) {
                    if (elementName.equalsIgnoreCase(element.getName())) {
                        return element.getValue();
                    }
                }
            }
        }
        return null;
    }

}
