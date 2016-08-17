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
    public CountingIMAPProtocol(final String name, final String host, final int port, final Properties props, final boolean isSSL, final MailLogger logger) throws IOException, ProtocolException {
        super(name, host, port, props, isSSL, logger);
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
