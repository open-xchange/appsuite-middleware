/*
 * Copyright (c) 2009-2011 Xing, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.openexchange.xing.session;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.TokenIterator;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.httpclient.EasySSLSocketFactory;

/**
 * Keeps track of a logged in user and contains configuration options for the
 * {@link XingAPI}. This is a base class to use for creating your own
 * {@link Session}s.
 */
public abstract class AbstractSession implements Session {

    private static final String API_SERVER = "api.xing.com";
    private static final String CONTENT_SERVER = "api.xing.com";
    private static final String WEB_SERVER = "www.xing.com";

    /** How long connections are kept alive. */
    private static final int KEEP_ALIVE_DURATION_SECS = 20;

    /** How often the monitoring thread checks for connections to close. */
    private static final int KEEP_ALIVE_MONITOR_INTERVAL_SECS = 5;

    /** The default timeout for client connections. */
    private static final int DEFAULT_TIMEOUT_MILLIS = 30000; // 30 seconds

    private final AppKeyPair appKeyPair;
    private AccessTokenPair accessTokenPair = null;

    protected final AtomicReference<HttpClient> client = new AtomicReference<HttpClient>();

    /**
     * Creates a new session with the given app key and secret, and access
     * type. The session will not be linked because it has no access token pair.
     */
    public AbstractSession(AppKeyPair appKeyPair) {
        this(appKeyPair, null);
    }

    /**
     * Creates a new session with the given app key and secret, and access
     * type. The session will be linked to the account corresponding to the
     * given access token pair.
     */
    public AbstractSession(AppKeyPair appKeyPair, AccessTokenPair accessTokenPair) {
        if (appKeyPair == null) {
            throw new IllegalArgumentException("'appKeyPair' must be non-null");
        }
        this.appKeyPair = appKeyPair;
        this.accessTokenPair = accessTokenPair;
    }

    /**
     * Links the session with the given access token and secret.
     */
    public void setAccessTokenPair(AccessTokenPair accessTokenPair) {
        if (accessTokenPair == null) {
            throw new IllegalArgumentException("'accessTokenPair' must be non-null");
        }
        this.accessTokenPair = accessTokenPair;
    }

    @Override
    public AppKeyPair getAppKeyPair() {
        return appKeyPair;
    }

    @Override
    public AccessTokenPair getAccessTokenPair() {
        return accessTokenPair;
    }

    /**
     * {@inheritDoc}
     * <br/><br/>
     * The default implementation always returns {@code Locale.ENLISH}, but you
     * are highly encouraged to localize your application and return the system
     * locale instead. Note: as of the time this was written, XING supports
     * the de, en, es, fr, and ja locales - if you use a locale other than
     * these, messages will be returned in English. However, it is good
     * practice to pass along the correct locale as we will add more languages
     * in the future.
     */
    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
    }

    @Override
    public boolean isLinked() {
        return accessTokenPair != null;
    }

    @Override
    public void unlink() {
        accessTokenPair = null;
    }

    /**
     * Signs the request by using's OAuth's HTTP header authorization scheme
     * and the PLAINTEXT signature method.  As such, this should only be used
     * over secure connections (i.e. HTTPS).  Using this over regular HTTP
     * connections is completely insecure.
     *
     * @see Session#sign
     */
    @Override
    public void sign(final HttpRequestBase request) throws XingException {
        try {
            final CommonsHttpOAuthConsumer signer = new CommonsHttpOAuthConsumer(appKeyPair.key, appKeyPair.secret);
            signer.setTokenWithSecret(accessTokenPair.key, accessTokenPair.secret);
            signer.sign(request);
        } catch (OAuthCommunicationException e) {
            throw new XingException(e);
        } catch (OAuthMessageSignerException e) {
            throw new XingException(e);
        } catch (OAuthExpectationFailedException e) {
            throw new XingException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <br/><br/>
     * The default implementation always returns null.
     */
    @Override
    public synchronized ProxyInfo getProxyInfo() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <br/><br/>
     * The default implementation does all of this and more, including using
     * a connection pool and killing connections after a timeout to use less
     * battery power on mobile devices. It's unlikely that you'll want to
     * change this behavior.
     */
    @Override
    public HttpClient getHttpClient() {
        HttpClient client = this.client.get();
        if (client == null) {
            synchronized (this) {
                client = this.client.get();
                if (client == null) {
                    // Set up default connection params. There are two routes to
                    // XING - api server and content server.
                    final HttpParams connParams = new BasicHttpParams();
                    ConnManagerParams.setMaxConnectionsPerRoute(connParams, new ConnPerRoute() {

                        @Override
                        public int getMaxForRoute(final HttpRoute route) {
                            return 10;
                        }
                    });
                    ConnManagerParams.setMaxTotalConnections(connParams, 20);

                    // Set up scheme registry.
                    final SchemeRegistry schemeRegistry = new SchemeRegistry();
                    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                    schemeRegistry.register(new Scheme("https", EasySSLSocketFactory.getInstance(), 443));

                    final DBClientConnManager cm = new DBClientConnManager(connParams, schemeRegistry);

                    // Set up client params.
                    final HttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_TIMEOUT_MILLIS);
                    HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_TIMEOUT_MILLIS);
                    HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
                    HttpProtocolParams.setUserAgent(httpParams, "Open-Xchange-XING/1.0.0");

                    final DefaultHttpClient c = new DefaultHttpClient(cm, httpParams) {

                        @Override
                        protected ConnectionKeepAliveStrategy createConnectionKeepAliveStrategy() {
                            return new DBKeepAliveStrategy();
                        }

                        @Override
                        protected ConnectionReuseStrategy createConnectionReuseStrategy() {
                            return new DBConnectionReuseStrategy();
                        }
                    };

                    c.addRequestInterceptor(new HttpRequestInterceptor() {

                        @Override
                        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                            if (!request.containsHeader("Accept-Encoding")) {
                                request.addHeader("Accept-Encoding", "gzip");
                            }
                        }
                    });

                    c.addResponseInterceptor(new HttpResponseInterceptor() {

                        @Override
                        public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                            final HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                final Header ceheader = entity.getContentEncoding();
                                if (ceheader != null) {
                                    final HeaderElement[] codecs = ceheader.getElements();
                                    for (final HeaderElement codec : codecs) {
                                        if (codec.getName().equalsIgnoreCase("gzip")) {
                                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    });

                    client = c;
                    this.client.set(c);
                }
            }
        }
        return client;
    }

    /**
     * {@inheritDoc}
     * <br/><br/>
     * The default implementation always sets a 30 second timeout.
     */
    @Override
    public void setRequestTimeout(HttpUriRequest request) {
        HttpParams reqParams = request.getParams();
        HttpConnectionParams.setSoTimeout(reqParams, DEFAULT_TIMEOUT_MILLIS);
        HttpConnectionParams.setConnectionTimeout(reqParams, DEFAULT_TIMEOUT_MILLIS);
    }

    @Override
    public String getAPIServer() {
        return API_SERVER;
    }

    @Override
    public String getContentServer() {
        return CONTENT_SERVER;
    }

    @Override
    public String getWebServer() {
        return WEB_SERVER;
    }

    private static final class DBKeepAliveStrategy implements ConnectionKeepAliveStrategy {
        
        DBKeepAliveStrategy() {
            super();
        }

        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            // Keep-alive for the shorter of 20 seconds or what the server specifies.
            long timeout = KEEP_ALIVE_DURATION_SECS * 1000;

            HeaderElementIterator i = new BasicHeaderElementIterator(
                    response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (i.hasNext()) {
                HeaderElement element = i.nextElement();
                String name = element.getName();
                String value = element.getValue();
                if (value != null && name.equalsIgnoreCase("timeout")) {
                    try {
                        timeout = Math.min(timeout, Long.parseLong(value) * 1000);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }

            return timeout;
        }
    } // End of DBKeepAliveStrategy class

    private static final class DBConnectionReuseStrategy extends DefaultConnectionReuseStrategy {

        DBConnectionReuseStrategy() {
            super();
        }

        /**
         * Implements a patch out in 4.1.x and 4.2 that isn't available in 4.0.x which
         * fixes a bug where connections aren't reused when the response is gzipped.
         * See https://issues.apache.org/jira/browse/HTTPCORE-257 for info about the
         * issue, and http://svn.apache.org/viewvc?view=revision&revision=1124215 for
         * the patch which is copied here.
         */
        @Override
        public boolean keepAlive(final HttpResponse response, final HttpContext context) {
            if (response == null) {
                throw new IllegalArgumentException("HTTP response may not be null.");
            }
            if (context == null) {
                throw new IllegalArgumentException("HTTP context may not be null.");
            }

            // Check for a self-terminating entity. If the end of the entity
            // will
            // be indicated by closing the connection, there is no keep-alive.
            final ProtocolVersion ver = response.getStatusLine().getProtocolVersion();
            final Header teh = response.getFirstHeader(HTTP.TRANSFER_ENCODING);
            if (teh != null) {
                if (!HTTP.CHUNK_CODING.equalsIgnoreCase(teh.getValue())) {
                    return false;
                }
            } else {
                final Header[] clhs = response.getHeaders(HTTP.CONTENT_LEN);
                // Do not reuse if not properly content-length delimited
                if (clhs == null || clhs.length != 1) {
                    return false;
                }
                final Header clh = clhs[0];
                try {
                    final int contentLen = Integer.parseInt(clh.getValue());
                    if (contentLen < 0) {
                        return false;
                    }
                } catch (final NumberFormatException ex) {
                    return false;
                }
            }

            // Check for the "Connection" header. If that is absent, check for
            // the "Proxy-Connection" header. The latter is an unspecified and
            // broken but unfortunately common extension of HTTP.
            HeaderIterator hit = response.headerIterator(HTTP.CONN_DIRECTIVE);
            if (!hit.hasNext()) {
                hit = response.headerIterator("Proxy-Connection");
            }

            // Experimental usage of the "Connection" header in HTTP/1.0 is
            // documented in RFC 2068, section 19.7.1. A token "keep-alive" is
            // used to indicate that the connection should be persistent.
            // Note that the final specification of HTTP/1.1 in RFC 2616 does
            // not
            // include this information. Neither is the "Connection" header
            // mentioned in RFC 1945, which informally describes HTTP/1.0.
            //
            // RFC 2616 specifies "close" as the only connection token with a
            // specific meaning: it disables persistent connections.
            //
            // The "Proxy-Connection" header is not formally specified anywhere,
            // but is commonly used to carry one token, "close" or "keep-alive".
            // The "Connection" header, on the other hand, is defined as a
            // sequence of tokens, where each token is a header name, and the
            // token "close" has the above-mentioned additional meaning.
            //
            // To get through this mess, we treat the "Proxy-Connection" header
            // in exactly the same way as the "Connection" header, but only if
            // the latter is missing. We scan the sequence of tokens for both
            // "close" and "keep-alive". As "close" is specified by RFC 2068,
            // it takes precedence and indicates a non-persistent connection.
            // If there is no "close" but a "keep-alive", we take the hint.

            if (hit.hasNext()) {
                try {
                    final TokenIterator ti = createTokenIterator(hit);
                    boolean keepalive = false;
                    while (ti.hasNext()) {
                        final String token = ti.nextToken();
                        if (HTTP.CONN_CLOSE.equalsIgnoreCase(token)) {
                            return false;
                        } else if (HTTP.CONN_KEEP_ALIVE.equalsIgnoreCase(token)) {
                            // continue the loop, there may be a "close"
                            // afterwards
                            keepalive = true;
                        }
                    }
                    if (keepalive) {
                        return true;
                        // neither "close" nor "keep-alive", use default policy
                    }

                } catch (final ParseException px) {
                    // invalid connection header means no persistent connection
                    // we don't have logging in HttpCore, so the exception is
                    // lost
                    return false;
                }
            }

            // default since HTTP/1.1 is persistent, before it was
            // non-persistent
            return !ver.lessEquals(HttpVersion.HTTP_1_0);
        }
    } // End of DBConnectionReuseStrategy class

    private static class DBClientConnManager extends ThreadSafeClientConnManager {
        public DBClientConnManager(HttpParams params, SchemeRegistry schreg) {
            super(params, schreg);
        }

        @Override
        public ClientConnectionRequest requestConnection(HttpRoute route,
                Object state) {
            IdleConnectionCloserThread.ensureRunning(this, KEEP_ALIVE_DURATION_SECS, KEEP_ALIVE_MONITOR_INTERVAL_SECS);
            return super.requestConnection(route, state);
        }
    } // End of DBClientConnManager class

    private static class IdleConnectionCloserThread extends Thread {
        private final DBClientConnManager manager;
        private final int idleTimeoutSeconds;
        private final int checkIntervalMs;
        private static IdleConnectionCloserThread thread = null;

        public IdleConnectionCloserThread(DBClientConnManager manager,
                int idleTimeoutSeconds, int checkIntervalSeconds) {
            super();
            this.manager = manager;
            this.idleTimeoutSeconds = idleTimeoutSeconds;
            this.checkIntervalMs = checkIntervalSeconds * 1000;
        }

        public static synchronized void ensureRunning(
                DBClientConnManager manager, int idleTimeoutSeconds,
                int checkIntervalSeconds) {
            if (thread == null) {
                thread = new IdleConnectionCloserThread(manager,
                        idleTimeoutSeconds, checkIntervalSeconds);
                thread.start();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    synchronized (this) {
                        wait(checkIntervalMs);
                    }
                    manager.closeExpiredConnections();
                    manager.closeIdleConnections(idleTimeoutSeconds, TimeUnit.SECONDS);
                    synchronized (IdleConnectionCloserThread.class) {
                        if (manager.getConnectionsInPool() == 0) {
                            thread = null;
                            return;
                        }
                    }
                }
            } catch (InterruptedException e) {
                thread = null;
            }
        }
    } // End of IdleConnectionCloserThread class

    private static class GzipDecompressingEntity extends HttpEntityWrapper {

        /*
         * From Apache HttpClient Examples.
         *
         * ====================================================================
         * Licensed to the Apache Software Foundation (ASF) under one
         * or more contributor license agreements.  See the NOTICE file
         * distributed with this work for additional information
         * regarding copyright ownership.  The ASF licenses this file
         * to you under the Apache License, Version 2.0 (the
         * "License"); you may not use this file except in compliance
         * with the License.  You may obtain a copy of the License at
         *
         *   http://www.apache.org/licenses/LICENSE-2.0
         *
         * Unless required by applicable law or agreed to in writing,
         * software distributed under the License is distributed on an
         * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
         * KIND, either express or implied.  See the License for the
         * specific language governing permissions and limitations
         * under the License.
         * ====================================================================
         *
         * This software consists of voluntary contributions made by many
         * individuals on behalf of the Apache Software Foundation.  For more
         * information on the Apache Software Foundation, please see
         * <http://www.apache.org/>.
         *
         */

        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent()
            throws IOException, IllegalStateException {

            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();

            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }

    }
}
