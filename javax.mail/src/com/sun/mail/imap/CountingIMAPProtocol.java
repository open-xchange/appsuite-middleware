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

package com.sun.mail.imap;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.mail.URLName;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.MailLogger;

/**
 * {@link CountingIMAPProtocol}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CountingIMAPProtocol extends IMAPProtocol {

    /** Mapping for the login-permitting semaphores */
    private static final ConcurrentMap<URLName, AtomicInteger> COUNTERS = new ConcurrentHashMap<URLName, AtomicInteger>(16, 0.9f, 1);

    private static AtomicInteger initCounter(final URLName url) {
        AtomicInteger s = COUNTERS.get(url);
        if (null == s) {
            final AtomicInteger ns = new AtomicInteger(0);
            s = COUNTERS.putIfAbsent(url, ns);
            if (null == s) {
                s = ns;
            }
        }
        return s;
    }

    // --------------------------------------------------------------------------------- //

    /** The counter */
    private volatile AtomicInteger counter;

    /** The user identifier */
    private String user;

    /**
     * Constructor.
     * <p>
     * Opens a connection to the given host at given port.
     *
     * @param host The host to connect to
     * @param port The port number to connect to
     * @param props The properties object used by this protocol
     * @param logger The logger
     */
    public CountingIMAPProtocol(String name, String host, int port, String user, Properties props, boolean isSSL, MailLogger logger) throws IOException, ProtocolException {
        super(name, host, port, user, props, isSSL, logger);
    }

    @Override
    protected void authenticatedStatusChanging(final boolean authenticate, final String u, final String p) throws ProtocolException {
        if (authenticate) {
            user = u;
            increment(u, p);
        } else {
            decrement();
        }
    }

    /**
     * Increments counter
     *
     * @param u The user
     * @param p The password
     */
    protected void increment(final String u, final String p) {
        AtomicInteger counter = this.counter;
        if (null == counter) {
            counter = initCounter(new URLName("imap", host, port, /* Integer.toString(accountId) */null, u, p));
            this.counter = counter;
        }

        if (null != counter) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(this + " - " + counter.incrementAndGet());
            }
        }
    }

    /**
     * Releases a previously acquired login permit.
     */
    protected void decrement() {
        final AtomicInteger counter = this.counter;
        if (null != counter) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(this + " - " + counter.decrementAndGet());
            }
            this.counter = null;
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(128);
        builder.append(CountingIMAPProtocol.class.getName()).append('@').append(hashCode());
        builder.append(" [");
        if (getHost() != null) {
            builder.append("host=").append(getHost()).append(", ");
        }
        builder.append("port=").append(getPort());
        if (null != user) {
            builder.append(", ").append("user=").append(user);
        }
        builder.append("]");
        return builder.toString();
    }

}
