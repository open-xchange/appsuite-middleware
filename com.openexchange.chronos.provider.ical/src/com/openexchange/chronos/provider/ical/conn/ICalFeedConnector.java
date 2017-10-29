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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.util.EntityUtils;
import com.openexchange.auth.info.AuthInfo;
import com.openexchange.auth.info.AuthType;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.ical.ICalFeedConfig;
import com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionCodes;
import com.openexchange.chronos.provider.ical.internal.ICalCalendarProviderProperties;
import com.openexchange.chronos.provider.ical.internal.Services;
import com.openexchange.chronos.provider.ical.internal.utils.ICalProviderUtils;
import com.openexchange.chronos.provider.ical.result.HeadResult;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 *
 * {@link ICalFeedConnector}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalFeedConnector {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalFeedConnector.class);

    protected final ICalFeedConfig iCalFeedConfig;
    protected Session session;
    protected long allowedFeedSize;

    public ICalFeedConnector(Session session, ICalFeedConfig iCalFeedConfig) {
        this.session = session;
        this.iCalFeedConfig = iCalFeedConfig;
        String maxFileSize = Services.getService(LeanConfigurationService.class).getProperty(ICalCalendarProviderProperties.maxFileSize);
        this.allowedFeedSize = ConfigTools.parseBytes(maxFileSize);
    }

    public HeadResult head() throws OXException {
        ICalProviderUtils.verifyURI(this.iCalFeedConfig.getFeedUrl());

        HttpHead headMethod = null;
        CloseableHttpResponse response = null;
        try {
            headMethod = prepareHead();
            response = ICalFeedHttpClient.getInstance().execute(headMethod);
            HeadResult result = new HeadResult(response.getStatusLine(), response.getAllHeaders());

            if (result.getStatusCode() >= 200 && result.getStatusCode() < 300) {
                long contentLength = Long.parseLong(result.getContentLength());
                if (contentLength > this.allowedFeedSize) {
                    throw ICalProviderExceptionCodes.FEED_SIZE_EXCEEDED.create(iCalFeedConfig.getFeedUrl(), contentLength, this.allowedFeedSize);
                }
                return result;
            }
            if (result.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                throw CalendarExceptionCodes.AUTH_FAILED.create(iCalFeedConfig.getFeedUrl());
            }
            if (result.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw ICalProviderExceptionCodes.NO_FEED.create(iCalFeedConfig.getFeedUrl());
            }
            throw ICalProviderExceptionCodes.UNEXPECTED_FEED_ERROR.create(iCalFeedConfig.getFeedUrl(), "Unknown server response.");
        } catch (IOException e) {
            LOG.error("Error while executing the head request targeting {}: {}.", iCalFeedConfig.getFeedUrl(), e.getMessage(), e);
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        } finally {
            close(headMethod, response);
            Streams.close(response);
        }
    }

    private HttpHead prepareHead() {
        HttpHead headMethod = new HttpHead(iCalFeedConfig.getFeedUrl());
        headMethod.addHeader(HttpHeaders.ACCEPT, "text/calendar");

        if (Strings.isNotEmpty(iCalFeedConfig.getEtag())) {
            headMethod.addHeader(HttpHeaders.IF_NONE_MATCH, iCalFeedConfig.getEtag());
        }

        String ifModifiedSince = DateUtils.formatDate(new Date(iCalFeedConfig.getLastUpdated()));
        if (Strings.isNotEmpty(ifModifiedSince)) {
            headMethod.setHeader(HttpHeaders.IF_MODIFIED_SINCE, ifModifiedSince);
        }
        handleAuth(headMethod);
        addCustomHeader(headMethod);

        return headMethod;
    }

    protected void handleAuth(HttpRequestBase method) {
        AuthInfo authInfo = this.iCalFeedConfig.getAuthInfo();
        AuthType authType = authInfo.getAuthType();
        switch (authType) {
            case BASIC: {
                StringBuilder auth = new StringBuilder();
                String login = authInfo.getLogin();
                if (Strings.isNotEmpty(login)) {
                    auth.append(login).append(":");
                }
                String password = authInfo.getPassword();
                if (Strings.isNotEmpty(password)) {
                    auth.append(password);
                }

                byte[] encodedAuth = Base64.encodeBase64(auth.toString().getBytes(Charset.forName("ISO-8859-1")));
                String authHeader = "Basic " + new String(encodedAuth);

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

    protected void addCustomHeader(HttpRequestBase method) {
        Map<String, String> customHeaders = iCalFeedConfig.getCustomHeaders();
        for (Map.Entry<String, String> header : customHeaders.entrySet()) {
            method.addHeader(header.getKey(), header.getValue());
        }
    }

    /**
     * Closes the supplied HTTP request & response resources silently.
     *
     * @param request The HTTP request to reset
     * @param response The HTTP response to consume and close
     */
    protected static void close(HttpRequestBase request, HttpResponse response) {
        consume(response);
        reset(request);
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

    /**
     * Ensures that the entity content is fully consumed and the content stream, if exists, is closed silently.
     *
     * @param response The HTTP response to consume and close
     */
    protected static void consume(HttpResponse response) {
        if (null != response) {
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                try {
                    EntityUtils.consume(entity);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
}
