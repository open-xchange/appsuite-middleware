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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.mail.URLName;
import com.sun.mail.iap.ConnectQuotaExceededException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;

/**
 * {@link JavaIMAPProtocol}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JavaIMAPProtocol extends IMAPProtocol {

    /** Mapping for the authenticate semaphores */
    private static final ConcurrentMap<URLName, Semaphore> semaphores = new ConcurrentHashMap<URLName, Semaphore>(16);

    private static Semaphore initAuthSemaphore(final URLName url, final int permits, final MailLogger logger) {
        if (permits <= 0) {
            return null;
        }
        Semaphore s = semaphores.get(url);
        if (null == s) {
            final Semaphore ns = new Semaphore(permits);
            s = semaphores.putIfAbsent(url, ns);
            if (null == s) {
                s = ns;
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("JavaIMAPProtocol.initAuthSemaphore: New semaphore for \"" + url + "\": " + s.toString());
                }
            }
        }
        return s;
    }

    // --------------------------------------------------------------------------------- //

    /** The max. number of concurrently authenticated protocols */
    private final int maxNumAuthenticated;

    /** The authenticate semaphore */
    private volatile Semaphore semaphore;

    /** The auth timeout millis */
    private final long authTimeoutMillis;

    /** The account identifier */
    private final int accountId;

    private final AtomicInteger permitCount;

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
    public JavaIMAPProtocol(final String name, final String host, final int port, final Properties props, final boolean isSSL, final MailLogger logger) throws IOException, ProtocolException {
        super(name, host, port, props, isSSL, logger);

        permitCount = new AtomicInteger();

        final int maxNumAuthenticated = PropUtil.getIntProperty(props, "mail.imap.maxNumAuthenticated", 0);
        this.maxNumAuthenticated = maxNumAuthenticated;

        if (maxNumAuthenticated <= 0) {
            authTimeoutMillis = -1L;
            accountId = -1;
        } else {
            final boolean noWait = PropUtil.getBooleanProperty(props, "mail.imap.authNoWait", false);
            if (noWait) {
                authTimeoutMillis = 0L;
            } else {
                final boolean await = PropUtil.getBooleanProperty(props, "mail.imap.authAwait", false);
                if (await) {
                    authTimeoutMillis = -1L;
                } else {
                    final int defaultTimeout = 10000;
                    final int millis = PropUtil.getIntProperty(props, "mail.imap.authTimeoutMillis", defaultTimeout);
                    authTimeoutMillis = millis <= 0 ? defaultTimeout : millis;
                }
            }
            accountId = PropUtil.getIntProperty(props, "mail.imap.accountId", 0);
        }
    }

    @Override
    protected void authenticatedStatusChanging(final boolean authenticate, final String u, final String p) throws ProtocolException {
        final int maxNumAuthenticated = this.maxNumAuthenticated;
        if (maxNumAuthenticated <= 0) {
            return;
        }

        if (authenticate) {
            acquirePermit(u, p, maxNumAuthenticated);
        } else {
            releasePermits();
        }

    }

    private void acquirePermit(final String u, final String p, final int maxNumAuthenticated) throws ConnectQuotaExceededException, ProtocolException {
        final boolean debug = (logger.isLoggable(Level.FINE));

        Semaphore semaphore = this.semaphore;
        if (null == semaphore) {
            semaphore = initAuthSemaphore(new URLName("imap", host, port, /*Integer.toString(accountId)*/null, u, p), maxNumAuthenticated, logger);
            this.semaphore = semaphore;
        } else {
            if (debug) {
                logger.fine("JavaIMAPProtocol.authenticated: semaphore already applied. -- protocol's permit count " + permitCount.get());
            }
        }

        if (null != semaphore) {
            try {

                final long start = debug ? System.currentTimeMillis() : 0L;

                final long timeoutMillis = this.authTimeoutMillis;
                if (0 == timeoutMillis) {
                    if (debug) {
                        logger.fine("JavaIMAPProtocol.authenticated: performing limited login. no wait -- " + semaphore);
                    }
                    if (!semaphore.tryAcquire()) {
                        // No permit available at the moment
                        throw new ConnectQuotaExceededException("Max. number of connections exceeded. Try again later.");
                    }
                } else if (timeoutMillis > 0) {
                    // Await released permit only for specified amount of milliseconds
                    if (debug) {
                        logger.fine("JavaIMAPProtocol.authenticated: performing limited login. max wait time: " + timeoutMillis + " -- " + semaphore);
                    }
                    if (!semaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
                        // No permit acquired in time
                        throw new ConnectQuotaExceededException("Max. number of connections exceeded. Try again later.");
                    }
                } else {
                    // Await until released permit available
                    if (debug) {
                        logger.fine("JavaIMAPProtocol.authenticated: performing limited login. awaiting until a used connection gets closed -- " + semaphore);
                    }
                    semaphore.acquire();
                }
                // Permit obtained
                permitCount.incrementAndGet();
                if (debug) {
                    final long dur = System.currentTimeMillis() - start;
                    logger.fine("JavaIMAPProtocol.authenticated: login permitted ("+dur+"msec) -- " + semaphore + " -- protocol's permit count " + permitCount.get());
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ProtocolException("Interrupted", e);
            }
        }
    }

    private void releasePermits() {
        final Semaphore semaphore = this.semaphore;
        if (null != semaphore) {
            final int permits = permitCount.getAndSet(0);
            if (permits > 0) {
                semaphore.release(permits);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("JavaIMAPProtocol.logout: released login semaphore -- " + semaphore);
                }
            }
            this.semaphore = null;
        }
    }

}
