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

package com.openexchange.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import com.openexchange.java.Strings;

/**
 * {@link TrustedSSLSocketFactory}
 * 
 * This implementation has to be placed within an exported package as it will be accessed per reflection.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class TrustedSSLSocketFactory extends SSLSocketFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrustedSSLSocketFactory.class);

    private SSLSocketFactory socketFactory;

    /**
     * Required to obtain factory by reflection
     * 
     * @return
     */
    public static SocketFactory getDefault() {
        return new TrustedSSLSocketFactory();
    }

    protected TrustedSSLSocketFactory() {
        try {
            this.socketFactory = SSLContext.getDefault().getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        try {

            Socket createSocket = this.socketFactory.createSocket(s, host, port, autoClose);
            logProtocols(createSocket);
            return createSocket;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // TODO: handle exception
        }
        return null;
    }

    private static void logProtocols(Socket socket) {
        if (socket instanceof SSLSocket) {
            final SSLSocket sslSocket = (SSLSocket) socket;
            LOG.debug("Supported protocols for socket {}: {}", sslSocket.getInetAddress().toString(), Strings.join(sslSocket.getEnabledProtocols(), ", "));
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return this.socketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.socketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return this.socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return this.socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return this.socketFactory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localHost, int localPort) throws IOException {
        return this.socketFactory.createSocket(address, port, localHost, localPort);
    }
}
