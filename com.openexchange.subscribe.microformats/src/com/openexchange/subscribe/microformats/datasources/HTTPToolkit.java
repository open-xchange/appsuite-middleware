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

package com.openexchange.subscribe.microformats.datasources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpContentTooLargeException;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.util.URIUtil;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.subscribe.microformats.OXMFServiceRegistry;

/**
 * {@link HTTPToolkit}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HTTPToolkit {

    /**
     * Grabs HTML content from specified site.
     *
     * @param site The site or URL
     * @return The grabbed HTML content
     * @throws HttpException If a HTTP error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException if the site is <code>null</code>
     */
    public static Reader grab(final String site) throws HttpException, IOException, OXException {
        final HttpClient client = new HttpClient();
        final int timeout = 5000;
        client.getParams().setSoTimeout(timeout);
        client.getParams().setIntParameter("http.connection.timeout", timeout);

        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

        client.getParams().setParameter("http.protocol.single-cookie-header", Boolean.TRUE);
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        /*
         * Generate URL
         */
        String encodedSite = URIUtil.encodeQuery(site);

        final java.net.URL javaURL = new java.net.URL(encodedSite);

        checkContentAndLength(javaURL, timeout);

        if (javaURL.getProtocol().equalsIgnoreCase("https")) {
            int port = javaURL.getPort();
            if (port == -1) {
                port = 443;
            }

            final Protocol https = new Protocol("https", new TrustAdapter(), 443);
            client.getHostConfiguration().setHost(javaURL.getHost(), port, https);

            final GetMethod getMethod = new GetMethod(javaURL.getFile());
            getMethod.getParams().setSoTimeout(timeout);
            getMethod.setQueryString(javaURL.getQuery());
            client.executeMethod(getMethod);

            return new InputStreamReader(getMethod.getResponseBodyAsStream(), "UTF-8");
        }
        /*
         * No https, but http
         */
        final GetMethod getMethod = new GetMethod(encodedSite);
        client.executeMethod(getMethod);
        return new InputStreamReader(getMethod.getResponseBodyAsStream(), "UTF-8");
    }

    public static Reader post(final String site, final Map<String, String> values) throws HttpException, IOException {
        final HttpClient client = new HttpClient();
        final int timeout = 5000;
        client.getParams().setSoTimeout(timeout);
        client.getParams().setIntParameter("http.connection.timeout", timeout);

        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
        client.getParams().setParameter("http.protocol.single-cookie-header", Boolean.TRUE);
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        final java.net.URL javaURL = new java.net.URL(site);

        checkContentAndLength(javaURL, timeout);

        if (javaURL.getProtocol().equalsIgnoreCase("https")) {
            int port = javaURL.getPort();
            if (port == -1) {
                port = 443;
            }

            final Protocol https = new Protocol("https", new TrustAdapter(), 443);
            client.getHostConfiguration().setHost(javaURL.getHost(), port, https);

            final PostMethod postMethod = new PostMethod(javaURL.getFile());
            for (final Map.Entry<String, String> entry : values.entrySet()) {
                postMethod.addParameter(new NameValuePair(entry.getKey(), entry.getValue()));
            }

            postMethod.getParams().setSoTimeout(timeout);
            postMethod.setQueryString(javaURL.getQuery());
            client.executeMethod(postMethod);

            return new InputStreamReader(postMethod.getResponseBodyAsStream(), "UTF-8");
        }
        /*
         * No https, but http
         */
        final PostMethod postMethod = new PostMethod(site);
        for (final Map.Entry<String, String> entry : values.entrySet()) {
            postMethod.addParameter(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        client.executeMethod(postMethod);
        return new InputStreamReader(postMethod.getResponseBodyAsStream(), "UTF-8");
    }

    private static void checkContentAndLength(final java.net.URL url, final int timeout) throws IOException, HttpException {
        /*
         * Examine headers for a valid HTML input
         */
        final String mimeType;
        final int length;
        {
            final URLConnection urlCon = url.openConnection();
            try {
                urlCon.setConnectTimeout(timeout);
                urlCon.setReadTimeout(timeout);
                urlCon.connect();
                final String ct = urlCon.getContentType();
                mimeType = null == ct ? "application/octet-stream" : ct.toLowerCase(Locale.ENGLISH);
                length = urlCon.getContentLength();
            } finally {
                /*
                 * The inconvenient way to close an URL connection: Obtain input stream to close it
                 */
                Streams.close(urlCon.getInputStream());
            }
        }
        /*
         * Check content type
         */
        if (!mimeType.startsWith("text/htm")) {
            throw new HttpException(new StringBuilder("No HTML content. Content-Type is ").append(mimeType).toString());
        }
        /*
         * Check content length
         */
        {
            final int maxLen;
            final ConfigurationService configurationService = OXMFServiceRegistry.getInstance().getService(ConfigurationService.class);
            if (null == configurationService) {
                maxLen = -1; // unlimited
            } else {
                maxLen = configurationService.getIntProperty("MAX_UPLOAD_SIZE", -1);
            }
            if (maxLen > 0 && length > maxLen) {
                throw new HttpContentTooLargeException(new StringBuilder("Content-Length is ").append(length).toString(), maxLen);
            }
        }
    }

}
