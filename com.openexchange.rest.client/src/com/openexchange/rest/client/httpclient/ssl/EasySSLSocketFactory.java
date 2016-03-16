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

package com.openexchange.rest.client.httpclient.ssl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.SchemeLayeredSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link EasySSLSocketFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class EasySSLSocketFactory implements SchemeLayeredSocketFactory {

    private static final EasySSLSocketFactory INSTANCE = new EasySSLSocketFactory();

    private volatile SSLContext sslcontext = null;

    /**
     * Initializes a new {@link EasySSLSocketFactory}.
     */
    public EasySSLSocketFactory() {
        super();
    }

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static EasySSLSocketFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public Socket createSocket(HttpParams params) throws IOException {
        return getSSLContext().getSocketFactory().createSocket();
    }

    @Override
    public Socket connectSocket(Socket sock, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
        final int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
        final int soTimeout = HttpConnectionParams.getSoTimeout(params);

        final SSLSocket sslsock = (SSLSocket) (sock != null ? sock : createSocket(params));
        sslsock.bind(localAddress);
        sslsock.connect(remoteAddress, connTimeout);
        sslsock.setSoTimeout(soTimeout);

        return sslsock;
    }

    @Override
    public boolean isSecure(Socket sock) throws IllegalArgumentException {
        return true;
    }

    private SSLContext createEasySSLContext() throws IOException {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new TrivialTrustManager() }, null);
            return context;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private SSLContext getSSLContext() throws IOException {
        SSLContext sslcontext = this.sslcontext;
        if (null == sslcontext) {
            synchronized (this) {
                sslcontext = this.sslcontext;
                if (null == sslcontext) {
                    sslcontext = createEasySSLContext();
                    this.sslcontext = sslcontext;
                }
            }
        }
        return sslcontext;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj != null && obj.getClass().equals(EasySSLSocketFactory.class);
    }

    @Override
    public int hashCode() {
        return EasySSLSocketFactory.class.hashCode();
    }

    @Override
    public Socket createLayeredSocket(Socket socket, String host, int port, HttpParams params) throws IOException, UnknownHostException {
        SSLSocket sslSocket = (SSLSocket) TrustAllSSLSocketFactory.getDefault().createSocket(socket, host, port, true);
        if (!sslSocket.isConnected()) {
            int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
            InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
            sslSocket.connect(remoteAddress, connTimeout);
        }

        int soTimeout = HttpConnectionParams.getSoTimeout(params);
        sslSocket.setSoTimeout(soTimeout);

        return sslSocket;

        /*
        Socket sock = socket != null ? socket : createSocket(params);
        if (sock instanceof SSLSocket) {
            final SSLSocket sslsock = (SSLSocket) sock;
            sslsock.connect(remoteAddress, connTimeout);
            sslsock.setSoTimeout(soTimeout);
            sock = sslsock;
        } else {
            if (!sock.isConnected()) {
                sock.connect(remoteAddress, connTimeout);
                sock.setSoTimeout(soTimeout);
            }
        }
        */

    }
}
