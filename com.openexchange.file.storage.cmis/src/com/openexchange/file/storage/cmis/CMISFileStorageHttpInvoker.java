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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.cmis;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;
import org.apache.chemistry.opencmis.client.bindings.impl.ClientVersion;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpInvoker;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils.Response;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;
import org.apache.commons.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import com.openexchange.file.storage.cmis.http.NTLMSchemeFactory;
import com.openexchange.java.Streams;

/**
 * {@link CMISFileStorageHttpInvoker}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CMISFileStorageHttpInvoker implements HttpInvoker {

    private static final Log log = com.openexchange.log.Log.loggerFor(CMISFileStorageHttpInvoker.class);

    /**
     * The HTTPS identifier constant.
     */
    private static final String HTTPS = "https";

    /**
     * The buffer size.
     */
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    /**
     * Initializes a new {@link CMISFileStorageHttpInvoker}.
     */
    public CMISFileStorageHttpInvoker() {
        super();
    }

    @Override
    public Response invoke(UrlBuilder urlBuilder, String method, String contentType, Map<String, String> headers, Output writer, BindingSession session, BigInteger offset, BigInteger length) throws Exception {
        Object ntlm = session.get("org.apache.chemistry.opencmis.binding.auth.ntlm");
        if (null == ntlm || !Boolean.parseBoolean(ntlm.toString().trim()) || !"GET".equalsIgnoreCase(method)) {
            return doCommonInvoke(urlBuilder, method, contentType, headers, writer, session, offset, length);
        }
        /*
         * NTLM <GET> request
         */
        DefaultHttpClient httpClient = null;
        HttpRequestBase httpRequest = null;
        try {
            final String sUrl = urlBuilder.toString();
            final URL url = new URL(sUrl);

            // Create client
            if (HTTPS.equalsIgnoreCase(url.getProtocol())) {
                httpClient = trustEveryoneSslHttpClient(url.getPort()); 
            } else {
                httpClient = new DefaultHttpClient();
            }

            // Register NTLMSchemeFactory with the HttpClient instance
            httpClient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());
            final String user = (String) session.get("org.apache.chemistry.opencmis.binding.auth.user");
            final String pw = (String) session.get("org.apache.chemistry.opencmis.binding.auth.password");
            httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new NTCredentials(user, pw, null, null));

            // GET method
            HttpGet httpGet = new HttpGet(sUrl);
            httpRequest = httpGet;
            
            // Execute HTTP request
            HttpResponse response = httpClient.execute(httpGet);

            // Examine response code
            final StatusLine statusLine = response.getStatusLine();
            final int code = statusLine.getStatusCode();
            
            System.out.println("----------------------------------------");
            System.out.println(statusLine);
            System.out.println("----------------------------------------");

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();

            // If the response does not enclose an entity, there is no need
            // to bother about connection release
            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    instream.read();
                    // do something useful with the response
                } catch (IOException ex) {
                    // In case of an IOException the connection will be released
                    // back to the connection manager automatically
                    throw ex;
                } catch (RuntimeException ex) {
                    // In case of an unexpected exception you may want to abort
                    // the HTTP request in order to shut down the underlying
                    // connection immediately.
                    httpGet.abort();
                    throw ex;
                } finally {
                    // Closing the input stream will trigger connection release
                    Streams.close(instream);
                }
            }
            
            
            
            
            return null;
        } finally {
            if (null != httpRequest) {
                httpRequest.releaseConnection();
            }
            if (null != httpClient) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    private static final AtomicReference<SSLSocketFactory> TRUST_ALL_REFERENCE = new AtomicReference<SSLSocketFactory>();

    private static DefaultHttpClient trustEveryoneSslHttpClient(final int port) {
        try {
            SSLSocketFactory socketFactory = TRUST_ALL_REFERENCE.get();
            if (null == socketFactory) {
                socketFactory = new SSLSocketFactory(new TrustStrategy() {

                    @Override
                    public boolean isTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                        // What else?!
                        return true;
                    }

                }, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                TRUST_ALL_REFERENCE.compareAndSet(null, socketFactory);
            }
            final SchemeRegistry registry = new SchemeRegistry();
            // registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", port <= 0 ? 443 : port, socketFactory));
            final PoolingClientConnectionManager mgr = new PoolingClientConnectionManager(registry);
            final DefaultHttpClient client = new DefaultHttpClient(mgr, new DefaultHttpClient().getParams());
            return client;
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private Response doCommonInvoke(UrlBuilder url, String method, String contentType, Map<String, String> headers, Output writer, BindingSession session, BigInteger offset, BigInteger length) {
        try {
            // log before connect
            if (log.isDebugEnabled()) {
                log.debug(method + " " + url);
            }

            // connect
            final HttpURLConnection conn = (HttpURLConnection) (new URL(url.toString())).openConnection();
            conn.setRequestMethod(method);
            conn.setDoInput(true);
            conn.setDoOutput(writer != null);
            conn.setAllowUserInteraction(false);
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent", ClientVersion.OPENCMIS_CLIENT);

            // timeouts
            int connectTimeout = session.get(SessionParameter.CONNECT_TIMEOUT, -1);
            if (connectTimeout >= 0) {
                conn.setConnectTimeout(connectTimeout);
            }

            int readTimeout = session.get(SessionParameter.READ_TIMEOUT, -1);
            if (readTimeout >= 0) {
                conn.setReadTimeout(readTimeout);
            }

            // set content type
            if (contentType != null) {
                conn.setRequestProperty("Content-Type", contentType);
            }
            // set other headers
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    conn.addRequestProperty(header.getKey(), header.getValue());
                }
            }

            // authenticate
            AuthenticationProvider authProvider = CmisBindingsHelper.getAuthenticationProvider(session);
            if (authProvider != null) {
                Map<String, List<String>> httpHeaders = authProvider.getHTTPHeaders(url.toString());
                if (httpHeaders != null) {
                    for (Map.Entry<String, List<String>> header : httpHeaders.entrySet()) {
                        if (header.getValue() != null) {
                            for (String value : header.getValue()) {
                                conn.addRequestProperty(header.getKey(), value);
                            }
                        }
                    }
                }
            }

            // range
            if ((offset != null) || (length != null)) {
                StringBuilder sb = new StringBuilder("bytes=");

                if ((offset == null) || (offset.signum() == -1)) {
                    offset = BigInteger.ZERO;
                }

                sb.append(offset.toString());
                sb.append("-");

                if ((length != null) && (length.signum() == 1)) {
                    sb.append(offset.add(length.subtract(BigInteger.ONE)).toString());
                }

                conn.setRequestProperty("Range", sb.toString());
            }

            // compression
            Object compression = session.get(SessionParameter.COMPRESSION);
            if ((compression != null) && Boolean.parseBoolean(compression.toString())) {
                conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            }

            // locale
            if (session.get(CmisBindingsHelper.ACCEPT_LANGUAGE) instanceof String) {
                conn.setRequestProperty("Accept-Language", session.get(CmisBindingsHelper.ACCEPT_LANGUAGE).toString());
            }

            // send data
            if (writer != null) {
                conn.setChunkedStreamingMode((64 * 1024) - 1);

                OutputStream connOut = null;

                Object clientCompression = session.get(SessionParameter.CLIENT_COMPRESSION);
                if ((clientCompression != null) && Boolean.parseBoolean(clientCompression.toString())) {
                    conn.setRequestProperty("Content-Encoding", "gzip");
                    connOut = new GZIPOutputStream(conn.getOutputStream(), 4096);
                } else {
                    connOut = conn.getOutputStream();
                }

                OutputStream out = new BufferedOutputStream(connOut, BUFFER_SIZE);
                writer.write(out);
                out.flush();
            }

            // connect
            conn.connect();

            // get stream, if present
            int respCode = conn.getResponseCode();
            InputStream inputStream = null;
            if ((respCode == 200) || (respCode == 201) || (respCode == 203) || (respCode == 206)) {
                inputStream = conn.getInputStream();
            }

            // log after connect
            if (log.isTraceEnabled()) {
                log.trace(method + " " + url + " > Headers: " + conn.getHeaderFields());
            }

            // forward response HTTP headers
            if (authProvider != null) {
                authProvider.putResponseHeaders(url.toString(), respCode, conn.getHeaderFields());
            }

            // get the response
            return new Response(respCode, conn.getResponseMessage(), conn.getHeaderFields(), inputStream, conn.getErrorStream());
        } catch (Exception e) {
            throw new CmisConnectionException("Cannot access " + url + ": " + e.getMessage(), e);
        }
    }

}
