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

package com.openexchange.subscribe.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Map;
import javax.activation.FileTypeMap;
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
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.infostore.ConverterException;
import com.openexchange.java.Streams;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.osgi.SubscriptionServiceRegistry;
import com.openexchange.tools.ImageTypeDetector;

/**
 * {@link HTTPToolkit}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HTTPToolkit {

    private static final String UTF_8 = "UTF-8";

    public static InputStream grabStream(final String site) throws IOException {
        return grabStream(site, true);
    }

    public static InputStream grabStream(final String site, boolean check) throws IOException {
        final HttpClient client = new HttpClient();
        final int timeout = 5000;
        client.getParams().setSoTimeout(timeout);
        client.getParams().setIntParameter("http.connection.timeout", timeout);

        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

        client.getParams().setParameter("http.protocol.single-cookie-header", Boolean.TRUE);
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        String encodedSite = URIUtil.encodeQuery(site);

        final java.net.URL javaURL = new java.net.URL(encodedSite);

        if(check) {
            checkContentAndLength(javaURL, timeout);
        }

        if (javaURL.getProtocol().equalsIgnoreCase("https")) {
            int port = javaURL.getPort();
            if (port == -1) {
                port = 443;
            }

            final Protocol https = new Protocol("https", new TrustAllAdapter(), 443);
            client.getHostConfiguration().setHost(javaURL.getHost(), port, https);

            final GetMethod getMethod = new GetMethod(javaURL.getFile());
            getMethod.getParams().setSoTimeout(timeout);
            getMethod.setQueryString(javaURL.getQuery());
            client.executeMethod(getMethod);

            return getMethod.getResponseBodyAsStream();
        }
        /*
         * No HTTPS
         */
        final GetMethod getMethod = new GetMethod(encodedSite);
        client.executeMethod(getMethod);
        return getMethod.getResponseBodyAsStream();
    }

    public static Reader grab(final String site) throws HttpException, IOException {
        return new InputStreamReader(grabStream(site), UTF_8);
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

            final Protocol https = new Protocol("https", new TrustAllAdapter(), 443);
            client.getHostConfiguration().setHost(javaURL.getHost(), port, https);

            final PostMethod postMethod = new PostMethod(javaURL.getFile());
            for (final Map.Entry<String, String> entry : values.entrySet()) {
                postMethod.addParameter(new NameValuePair(entry.getKey(), entry.getValue()));
            }

            postMethod.getParams().setSoTimeout(timeout);
            postMethod.setQueryString(javaURL.getQuery());
            client.executeMethod(postMethod);

            return new InputStreamReader(postMethod.getResponseBodyAsStream(), UTF_8);
        }
        /*
         * No HTTPS
         */
        final PostMethod postMethod = new PostMethod(site);
        for (final Map.Entry<String, String> entry : values.entrySet()) {
            postMethod.addParameter(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        client.executeMethod(postMethod);
        return new InputStreamReader(postMethod.getResponseBodyAsStream(), UTF_8);
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
            final ConfigurationService configurationService =
                SubscriptionServiceRegistry.getInstance().getService(ConfigurationService.class);
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

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection and put into given {@link Contact contact container}.
     *
     * @param contact The contact container to fill
     * @param url The URI parameter's value
     * @throws ConverterException If converting image's data fails
     */
    public static void loadImageFromURL(final Contact contact, final String url) throws OXException {
        try {
            loadImageFromURL(contact, new URL(url));
        } catch (final MalformedURLException e) {
            SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        }
    }

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection and put into given {@link Contact contact container}.
     *
     * @param contact The contact container to fill
     * @param url The image URL
     * @throws ConverterException If converting image's data fails
     */
    private static void loadImageFromURL(final Contact contact, final URL url) throws OXException {
        String mimeType = null;
        byte[] bytes = null;
        try {
            final URLConnection urlCon = url.openConnection();
            urlCon.setConnectTimeout(2500);
            urlCon.setReadTimeout(2500);
            urlCon.connect();
            mimeType = urlCon.getContentType();
            InputStream in = null;
            try {
                in = urlCon.getInputStream();
                bytes = Streams.stream2bytes(in);
                // In case the configuration file was not read (yet) the default value is given here
                final long maxSize = ContactConfig.getInstance().getMaxImageSize();
                if (maxSize > 0 && bytes.length > maxSize) {
                    final ConverterException e = new ConverterException("Contact image is " + bytes.length + " bytes large and limit is " + maxSize + " bytes. Image is therefore ignored.");
                    org.slf4j.LoggerFactory.getLogger(HTTPToolkit.class).warn("", e);
                    bytes = null;
                }
            } finally {
                Streams.close(in);
            }
        } catch (final SocketTimeoutException e) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } catch (final IOException e) {
            throw SubscriptionErrorMessage.IO_ERROR.create("IO problem while reading \"" + url.toString() + "\"", e);
        }
        if (mimeType == null) {
            mimeType = ImageTypeDetector.getMimeType(bytes);
            if ("application/octet-stream".equals(mimeType)) {
                mimeType = getMimeType(url.toString());
            }
        }
        if (bytes != null && isValidImage(bytes)) {
            // Mime type should be of image type. Otherwise web server send some error page instead of 404 error code.
            contact.setImage1(bytes);
            contact.setImageContentType(mimeType);
        }
    }

    private static boolean isValidImage(final byte[] data) {
        InputStream inputStream = null;
        java.awt.image.BufferedImage bimg = null;
        try {
            inputStream = Streams.newByteArrayInputStream(data);
            bimg = javax.imageio.ImageIO.read(inputStream);
        } catch (final Exception e) {
            return false;
        } finally {
            Streams.close(inputStream);
        }
        return (bimg != null);
    }

    private static String getMimeType(final String filename) {
        return FileTypeMap.getDefaultFileTypeMap().getContentType(filename);
    }

}
