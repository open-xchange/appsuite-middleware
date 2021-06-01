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

package com.openexchange.subscribe.helpers;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.activation.FileTypeMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
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

    private static class ClientClosingInputStream extends FilterInputStream {

        private final CloseableHttpClient client;

        ClientClosingInputStream(InputStream in, CloseableHttpClient client) {
            super(in);
            this.client = client;
        }

        @Override
        public void close() throws IOException {
            super.close();
            Streams.close(client);
        }
    }

    private static final String UTF_8 = "UTF-8";

    public static InputStream grabStream(CloseableHttpClient client, final String site) throws IOException, HttpException, URISyntaxException {
        return grabStream(client, site, true);
    }

    public static InputStream grabStream(CloseableHttpClient client, final String site, boolean check) throws IOException, HttpException, URISyntaxException {
        final int timeout = 5000;
        final java.net.URL javaURL = new URIBuilder(site).build().toURL();

        if (check) {
            checkContentAndLength(javaURL, timeout);
        }

        HttpGet method = new HttpGet(javaURL.toURI());
        HttpResponse resp = client.execute(method);
        ClientClosingInputStream inputStream = streamFrom(resp, client);
        client = null; // Avoid premature closing
        return inputStream;
    }

    public static Reader grab(CloseableHttpClient client, final String site) throws HttpException, IOException, URISyntaxException {
        return new InputStreamReader(grabStream(client, site), UTF_8);
    }

    public static Reader post(CloseableHttpClient client, final String site, final Map<String, String> values) throws HttpException, IOException, URISyntaxException {
        final int timeout = 5000;
        final java.net.URL javaURL = new java.net.URL(site);

        checkContentAndLength(javaURL, timeout);

        HttpPost method = new HttpPost(javaURL.toURI());
        List<NameValuePair> postParameters = new ArrayList<>();
        for (final Map.Entry<String, String> entry : values.entrySet()) {
            postParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        method.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        HttpResponse resp = client.execute(method);
        HttpEntity respEntity = resp.getEntity();
        Charset charSet = ContentType.getOrDefault(respEntity).getCharset();
        Reader reader = new InputStreamReader(streamFrom(respEntity, client), charSet);
        client = null;
        return reader;
    }

    private static ClientClosingInputStream streamFrom(HttpResponse httpResponse, CloseableHttpClient client) throws UnsupportedOperationException, IOException {
        return streamFrom(httpResponse.getEntity(), client);
    }

    private static ClientClosingInputStream streamFrom(HttpEntity httpEntity, CloseableHttpClient client) throws UnsupportedOperationException, IOException {
        InputStream content = null;
        try {
            content = httpEntity.getContent();
            ClientClosingInputStream closingInputStream = new ClientClosingInputStream(content, client);
            content = null;
            return closingInputStream;
        } finally {
            Streams.close(content);
        }
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
                throw new HttpException(new StringBuilder("Content-Length ").append(length).append(" is too large").toString());
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
        } catch (MalformedURLException e) {
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
        } catch (SocketTimeoutException e) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } catch (IOException e) {
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
        } catch (@SuppressWarnings("unused") Exception e) {
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
