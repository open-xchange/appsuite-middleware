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
import java.util.logging.Level;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.QueuingIMAPStore.CountingQueue;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.MailLogger;

/**
 * {@link QueuedIMAPProtocol}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class QueuedIMAPProtocol extends IMAPProtocol implements Comparable<QueuedIMAPProtocol> {

    private static final org.slf4j.Logger LOG = QueuingIMAPStore.getLog();

    /** The associated queuing IMAP store */
    protected volatile QueuingIMAPStore store;

    /** The queue */
    private final CountingQueue queue;

    /** The time stamp */
    private volatile long stamp;

    /** The user name for debugging purpose */
    private String user;

    /** The flag whether a decrement was performed or not */
    private boolean decrementPerformed;

    /**
     * Constructor.
     * <p>
     * Opens a connection to the given host at given port.
     */
    public QueuedIMAPProtocol(final String name, final String host, final int port, final Properties props, final boolean isSSL, final MailLogger logger, final CountingQueue q, final QueuingIMAPStore store) throws IOException, ProtocolException {
        super(name, host, port, props, isSSL, logger);
        this.queue = q;
        this.store = store;
    }

    @Override
    public int compareTo(final QueuedIMAPProtocol other) {
        final long thisVal = this.stamp;
        final long anotherVal = other.stamp;
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    /**
     * Checks if this protocol is currently IDLE.
     *
     * @return <code>true</code> if IDLE; otherwise <code>false</code>
     */
    public boolean isIdle() {
        return null != idleTag;
    }

    /**
     * Sets the associated store
     *
     * @param store The store
     * @return This protocol with store applied
     */
    public QueuedIMAPProtocol setStore(final QueuingIMAPStore store) {
        this.store = store;
        return this;
    }

    /**
     * Gets the stamp
     *
     * @return The stamp
     */
    public long getAuthenticatedStamp() {
        return stamp;
    }

    @Override
    protected void authenticatedStatusChanging(final boolean authenticate, final String u, final String p) throws ProtocolException {
        if (authenticate) {
            user = u;
        }
    }

    @Override
    public synchronized void disconnect() {
        try {
            super.disconnect();
        } finally {
            decrementNewCount();
        }
    }

    /**
     * Decrements associated queue's new-count.
     */
    private synchronized void decrementNewCount() {
        if (!decrementPerformed) {
            // Has been disconnected
            final CountingQueue queue = this.queue;
            if (null != queue) {
                queue.decrementNewCount();
                decrementPerformed = true;
                queue.removeTrackingInfo(this);
                if (logger.isLoggable(Level.FINE) || LOG.isDebugEnabled()) {
                    final String msg = "QueuedIMAPProtocol.disconnect(): Decremented new-count for " + toString() + "\n\t(total=" + queue.getNewCount() + ")";
                    logger.fine(msg);
                    LOG.debug(msg);
                }
            }
        }
    }

    @Override
    public synchronized void logout() throws ProtocolException {
        this.stamp = System.currentTimeMillis();
        if (queue.offerIfAbsent(this)) {
            clearHandlers();
        } else {
            super.logout();
            if (logger.isLoggable(Level.FINE) || LOG.isDebugEnabled()) {
                final String msg = "QueuedIMAPProtocol.logout(): Queue is full. LOGOUT for " + toString();
                logger.fine(msg);
                LOG.debug(msg);
            }
        }
        queue.removeTrackingInfo(this);
    }

    /**
     * LOGOUT Command.
     *
     * @see "RFC2060, section 6.1.3"
     */
    public synchronized void realLogout() throws ProtocolException {
        if (logger.isLoggable(Level.FINE) || LOG.isDebugEnabled()) {
            final String msg = "QueuedIMAPProtocol.realLogout(): LOGOUT for " + toString();
            logger.fine(msg);
            LOG.debug(msg);
        }
        super.logout();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(128);
        builder.append(QueuedIMAPProtocol.class.getName()).append('@').append(hashCode());
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
