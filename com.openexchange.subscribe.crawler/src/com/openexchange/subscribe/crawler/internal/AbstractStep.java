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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.subscribe.crawler.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.subscribe.crawler.GoogleAPIStep;
import com.openexchange.subscribe.crawler.Workflow;

public abstract class AbstractStep<O,I> implements Step<O,I>{

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractStep.class);

    protected String description;

    protected Exception exception;

    protected boolean executedSuccessfully;

    protected Workflow workflow;

    protected O output;

    protected I input;

    protected boolean debuggingEnabled;

    protected boolean switchUserAgent;

    protected AbstractStep() {
        super();
    }

    @Override
    public boolean executedSuccessfully() {
        return executedSuccessfully;
    }

    @Override
    public Exception getException() {
        return this.exception;
    }

    @Override
    public void setWorkflow (final Workflow workflow){
        this.workflow = workflow;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public abstract void execute(WebClient webClient) throws OXException;

    @Override
    public Class inputType() {
        return input.getClass();
    }

    @Override
    public Class outputType() {
        return output.getClass();
    }

    @Override
    public O getOutput() {
        return output;
    }

    @Override
    public void setInput(final I input) {
        this.input = input;

    }

    @Override
    public I getInput() {
        return input;
    }

    @Override
    public boolean isSwitchUserAgent() {
        return switchUserAgent;
    }


    @Override
    public void setSwitchUserAgent(boolean switchUserAgent) {
        this.switchUserAgent = switchUserAgent;
    }

    // Convenience Methods for Development / Debugging
    @Override
    public boolean isDebuggingEnabled() {
        return debuggingEnabled;
    }


    @Override
    public void setDebuggingEnabled(boolean debuggingEnabled) {
        this.debuggingEnabled = debuggingEnabled;
    }

    // this opens the current page in the developers browser while debugging for a fast overview.
    protected void openPageInBrowser(Page page){
        if (null == page) {
            // Nothing to open
            return;
        }
        File file = new File ("./crawlerTestPage.html");
        Writer output = null;
        try {
          output = new BufferedWriter(new FileWriter(file));
          output.write( page.getWebResponse().getContentAsString() );
          // Check on which Operating System this runs
          if (System.getProperty("os.name").contains("Mac")){
              Runtime.getRuntime().exec("open -a Safari ./crawlerTestPage.html");
          } else if (System.getProperty("os.name").contains("Windows")){
              Runtime.getRuntime().exec("cmd.exe /C start ./crawlerTestPage.html");
          } else {
              // Linux (hopefully)
              Runtime.getRuntime().exec("firefox ./crawlerTestPage.html &");
          }
          //windows: iexplore http://www.example.com
        } catch (IOException e) {
            LOG.error("", e);
        } finally {
            Streams.close(output);
        }
    }

    @Override
    public TypeVariable<?>[] runEmpty(){
        return this.getClass().getTypeParameters();
    }
    // Convenience Methods for Development / Debugging

    private static volatile Field handlerField;
    private static Field handlerField() {
        Field f = handlerField;
        if (null == f) {
            synchronized (GoogleAPIStep.class) {
                f = handlerField;
                if (null == f) {
                    try {
                        f = URL.class.getDeclaredField("handler");
                        f.setAccessible(true);
                        handlerField = f;
                    } catch (final RuntimeException e) {
                        throw e;
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return f;
    }

    private static volatile Method openConnectionMethod;
    static Method openConnectionMethod() {
        Method m = openConnectionMethod;
        if (null == m) {
            synchronized (GoogleAPIStep.class) {
                m = openConnectionMethod;
                if (null == m) {
                    try {
                        m = URLStreamHandler.class.getDeclaredMethod("openConnection", new Class<?>[] { URL.class });
                        m.setAccessible(true);
                        openConnectionMethod = m;
                    } catch (final RuntimeException e) {
                        throw e;
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return m;
    }

    private static volatile Method openConnectionWithProxyMethod;
    static Method openConnectionWithProxyMethod() {
        Method m = openConnectionWithProxyMethod;
        if (null == m) {
            synchronized (GoogleAPIStep.class) {
                m = openConnectionWithProxyMethod;
                if (null == m) {
                    try {
                        m = URLStreamHandler.class.getDeclaredMethod("openConnection", new Class<?>[] { URL.class, Proxy.class });
                        m.setAccessible(true);
                        openConnectionWithProxyMethod = m;
                    } catch (final RuntimeException e) {
                        throw e;
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return m;
    }

    /** A trust manager that does not validate certificate chains */
    static final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
            // Nothing
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
            // Nothing
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    } };

    /**
     * Prepares given {@link URLConnection} instance if an all-trusting trust manager needs to be applied.
     *
     * @param urlCon The URL connection to check
     * @throws NoSuchAlgorithmException If preparation fails
     * @throws KeyManagementException If preparation fails
     */
    static void prepareURLConnection(final URLConnection urlCon) throws NoSuchAlgorithmException, KeyManagementException {
        if (urlCon instanceof HttpsURLConnection) {
            final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlCon;
            SSLSocketFactory sslSocketFactory = httpsURLConnection.getSSLSocketFactory();
            if ((null == sslSocketFactory) || !(sslSocketFactory instanceof com.openexchange.tools.ssl.TrustAllSSLSocketFactory)) {
                // Install the all-trusting trust manager
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                sslSocketFactory = sslContext.getSocketFactory();
                // Tell the url connection object to use our socket factory which bypasses security checks
                httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
            }
        }
    }

    /**
     * Exchanges the {@link URLStreamHandler} instance in specified {@link URL} object.
     * <p>
     * In case given URL is of type <code>https</code>, established URL connections will trust all certificates.
     *
     * @param url The URL object
     */
    protected void exchangeURLStreamHandler(final URL url) {
        try {
            final Field handlerField = handlerField();
            // Get current handler
            final URLStreamHandler delegate = (URLStreamHandler) handlerField.get(url);
            // Instantiate new delege handler
            final URLStreamHandler newHandler = new URLStreamHandler() {

                @Override
                protected URLConnection openConnection(final URL u) throws IOException {
                    try {
                        final URLConnection urlCon = (URLConnection) openConnectionMethod().invoke(delegate, u);
                        prepareURLConnection(urlCon);
                        return urlCon;
                    } catch (final IllegalArgumentException e) {
                        throw new IOException("Failed openConnection(URL) invocation.", e);
                    } catch (final InvocationTargetException e) {
                        final Throwable cause = e.getCause();
                        throw cause instanceof IOException ? (IOException) cause : new IOException("Failed openConnection(URL) invocation.", cause);
                    } catch (final IllegalAccessException e) {
                        throw new IOException("Failed openConnection(URL) invocation.", e);
                    } catch (final NoSuchAlgorithmException e) {
                        throw new IOException("Failed openConnection(URL) invocation.", e);
                    } catch (final KeyManagementException e) {
                        throw new IOException("Failed openConnection(URL) invocation.", e);
                    }
                }

                @Override
                protected URLConnection openConnection(final URL u, final Proxy p) throws IOException {
                    try {
                        final URLConnection urlCon = (URLConnection) openConnectionWithProxyMethod().invoke(delegate, u, p);
                        prepareURLConnection(urlCon);
                        return urlCon;
                    } catch (final IllegalArgumentException e) {
                        throw new IOException("Failed openConnection(URL) invocation.", e);
                    } catch (final InvocationTargetException e) {
                        final Throwable cause = e.getCause();
                        throw cause instanceof IOException ? (IOException) cause : new IOException("Failed openConnection(URL) invocation.", cause);
                    } catch (final IllegalAccessException e) {
                        throw new IOException("Failed openConnection(URL) invocation.", e);
                    } catch (final NoSuchAlgorithmException e) {
                        throw new IOException("Failed openConnection(URL) invocation.", e);
                    } catch (final KeyManagementException e) {
                        throw new IOException("Failed openConnection(URL) invocation.", e);
                    }
                }
            };
            // Set new handler
            handlerField.set(url, newHandler);
        } catch (final Exception e) {
            // Failed to replace URLStreamHandler
            LOG.warn("Failed to replace URLStreamHandler for URL \"{}\"", url.toString(), e);
        }
    }

}
