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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.jsieve.export;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link SocketFetcher} - Utility class to get Sockets.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SocketFetcher {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SocketFetcher.class);

    private static final class PrivilegedActionImpl implements PrivilegedAction<Object> {

        private final org.slf4j.Logger logger;

        public PrivilegedActionImpl(final org.slf4j.Logger logger) {
            super();
            this.logger = logger;
        }

        @Override
        public Object run() {
            ClassLoader cl = null;
            try {
                cl = Thread.currentThread().getContextClassLoader();
            } catch (final SecurityException ex) {
                logger.error("", ex);
            }
            return cl;
        }
    }

    /**
     * Initializes a new {@link SocketFetcher}.
     */
    private SocketFetcher() {
        super();
    }

    /**
     * Start TLS on an existing socket. Supports the "STARTTLS" command in many protocols.
     */
    public static Socket startTLS(final Socket socket, final String host) throws IOException {
        final int port = socket.getPort();
        try {
            // Get SSL socket factory
            final SSLSocketFactory ssf = TrustAllSSLSocketFactory.getDefault();
            // Create new socket layered over an existing socket connected to the named host, at the given port.
            final Socket newSocket = ssf.createSocket(socket, host, port, true);
            configureSSLSocket(newSocket);
            return newSocket;
        } catch (Exception ex) {
            if (ex instanceof InvocationTargetException) {
                final Throwable t = ((InvocationTargetException) ex).getTargetException();
                if (t instanceof Exception) {
                    ex = (Exception) t;
                }
            }
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            final StringBuilder err = new StringBuilder(256);
            err.append("Exception in startTLS using ").append("unknown socket factory").append(": host, port: ");
            err.append(host);
            err.append(", ");
            err.append(port);
            err.append("; Exception: ");
            err.append(ex);
            // wrap anything else before sending it on
            final IOException ioex = new IOException(err.toString());
            ioex.initCause(ex);
            throw ioex;
        }
    }

    /**
     * Gets a socket factory of the specified class.
     *
     * @param sfClass The socket factory class name
     * @return A socket factory of the specified class
     * @throws ClassNotFoundException If class cannot be found
     * @throws NoSuchMethodException If "getDefault()" does not exist in socket factory
     * @throws IllegalAccessException If "getDefault()" is not accessible
     * @throws InvocationTargetException If an error occurs on "getDefault()" invokation
     */
    public static SocketFactory getSocketFactory(final String sfClass) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (sfClass == null || sfClass.length() == 0) {
            return null;
        }

        // dynamically load the class

        final ClassLoader cl = getContextClassLoader();
        Class<?> clsSockFact = null;
        if (cl != null) {
            try {
                clsSockFact = cl.loadClass(sfClass);
            } catch (final ClassNotFoundException cex) {
                LOG.error("", cex);
            }
        }
        if (clsSockFact == null) {
            clsSockFact = Class.forName(sfClass);
        }
        // get & invoke the getDefault() method
        final Method mthGetDefault = clsSockFact.getMethod("getDefault", new Class[] {});
        final SocketFactory sf = (SocketFactory) mthGetDefault.invoke(new Object(), new Object[] {});
        return sf;
    }

    /**
     * Convenience method to get our context class loader. Assert any privileges we might have and then call the
     * Thread.getContextClassLoader method.
     */
    private static ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedActionImpl(LOG));
    }

    /**
     * Configure the SSL options for the socket (if it's an SSL socket).
     */
    private static void configureSSLSocket(final Socket socket) {
        if (!(socket instanceof SSLSocket)) {
            return;
        }
        final SSLSocket sslsocket = (SSLSocket) socket;
        sslsocket.setEnabledProtocols(new String[] { "SSLv3", "TLSv1" });
        // sslsocket.setEnabledProtocols(new String[] { "TLSv1" });
    }

}
