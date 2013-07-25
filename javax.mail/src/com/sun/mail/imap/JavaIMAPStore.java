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

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.security.auth.Subject;
import com.sun.mail.iap.ConnectQuotaExceededException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.PropUtil;

/**
 * {@link JavaIMAPStore}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JavaIMAPStore extends IMAPStore {

    private static final int DEFAULT_SEMAPHORE_TIMEOUT_MILLIS = 20000;

    /** The login semaphore */
    private static final ConcurrentMap<URLName, Semaphore> loginSemaphores = new ConcurrentHashMap<URLName, Semaphore>(16);

    private static Semaphore initLoginSemaphore(final URLName url, final int permits) {
        if (permits <= 0) {
            return null;
        }
        Semaphore s = loginSemaphores.get(url);
        if (null == s) {
            final Semaphore ns = new Semaphore(permits);
            s = loginSemaphores.putIfAbsent(url, ns);
            if (null == s) {
                s = ns;
            }
        }
        return s;
    }

    /** Whether SASL is enabled. */
    private final boolean enableSASL;

    /** The Kerberos subject. */
    private transient Subject kerberosSubject;

    /** The SASL mechansims */
    protected String[] saslMechanisms;

    /** The SASL realm */
    protected String saslRealm;

    /** Authorization ID */
    private String authorizationID;

    /** Proxy auth user */
    private String proxyAuthUser;

    /** The login timeout */
    private final long loginSemaphoreTimeoutMillis;

    /** The max. number of concurrent connected stores */
    private final int maxNumConnections;

    /** The login semaphore */
    private volatile Semaphore semaphore;

    /** The account identifier */
    private int accountId;


    /**
     * Initializes a new {@link JavaIMAPStore}.
     *
     * @param session The session
     * @param url The URL
     * @param name The store's name
     * @param isSSL Whether to perform SSL or not
     */
    public JavaIMAPStore(final Session session, final URLName url, final String name, final boolean isSSL) {
        super(session, url, name, isSSL);
        enableSASL = PropUtil.getBooleanSessionProperty(session, "mail.imap.sasl.enable", false);
        if (enableSASL) {
            // Kerberos subject
            kerberosSubject = (Subject) session.getProperties().get("mail.imap.sasl.kerberosSubject");
            // SASL mechansims
            String s = session.getProperty("mail.imap.sasl.mechanisms");
            if (s != null && s.length() > 0) {
                List<String> v = new ArrayList<String>(5);
                StringTokenizer st = new StringTokenizer(s, " ,");
                while (st.hasMoreTokens()) {
                    String m = st.nextToken();
                    if (m.length() > 0) {
                        v.add(m);
                    }
                }
                saslMechanisms = v.toArray(new String[0]);
            }
            // SASL realm
            s = session.getProperty("mail.imap.sasl.realm");
            if (s != null) {
                saslRealm = s;
            }
            // Check if an authorization ID has been specified
            s = session.getProperty("mail." + name + ".sasl.authorizationid");
            if (s != null) {
                authorizationID = s;
            }
            // Check if we should do a PROXYAUTH login
            s = session.getProperty("mail." + name + ".proxyauth.user");
            if (s != null) {
                proxyAuthUser = s;
            }
        }
        final int maxNumConnections = PropUtil.getIntSessionProperty(session, "mail.imap.maxNumConnections", 0);
        this.maxNumConnections = maxNumConnections;
        if (maxNumConnections <= 0) {
            loginSemaphoreTimeoutMillis = -1L;
            accountId = -1;
        } else {
            final boolean loginSemaphoreAwait = PropUtil.getBooleanSessionProperty(session, "mail.imap.loginSemaphoreAwait", false);
            if (loginSemaphoreAwait) {
                loginSemaphoreTimeoutMillis = -1L;
            } else {
                final int defaultTimeout = DEFAULT_SEMAPHORE_TIMEOUT_MILLIS;
                final int millis = PropUtil.getIntSessionProperty(session, "mail.imap.loginSemaphoreTimeoutMillis", defaultTimeout);
                loginSemaphoreTimeoutMillis = millis <= 0 ? defaultTimeout : millis;
            }
            accountId = PropUtil.getIntSessionProperty(session, "mail.imap.accountId", 0);
        }
    }

    /**
     * Initializes a new {@link JavaIMAPStore}.
     *
     * @param session The session
     * @param url The URL
     */
    public JavaIMAPStore(final Session session, final URLName url) {
        this(session, url, "imap", false);
    }

    @Override
    protected void login(final IMAPProtocol p, final String u, final String pw) throws ProtocolException {
        /*-
         * Get semaphore (if enabled)
         *
         * - Include account identifier for account-wise login tracking
         */
        final Semaphore semaphore = initLoginSemaphore(new URLName("imap", p.getHost(), p.getPort(), /*Integer.toString(accountId)*/null, u, pw), maxNumConnections);
        if (null != semaphore) {
            this.semaphore = semaphore;
            try {
                final long loginSemaphoreTimeoutMillis = this.loginSemaphoreTimeoutMillis;
                if (loginSemaphoreTimeoutMillis > 0) {
                    // Await released permit only for specified amount of milliseconds
                    if (!semaphore.tryAcquire(loginSemaphoreTimeoutMillis, TimeUnit.MILLISECONDS)) {
                        // No permit acquired in time
                        throw new ConnectQuotaExceededException("Max. number of connections exceeded. Try again later.");
                    }
                } else {
                    // Await until released permit available
                    semaphore.acquire();
                }
                logger.fine("JavaIMAPStore login: permitted.");
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ProtocolException("Interrupted", e);
            }
        }
        /*
         * Auth stuff
         */
        if (enableSASL && null != kerberosSubject) {
            // Do Kerberos authentication
            final String authzid;
            if (authorizationID != null) {
                authzid = authorizationID;
            } else if (proxyAuthUser != null) {
                authzid = proxyAuthUser;
            } else {
                authzid = null;
            }
            try {
                Subject.doAs(kerberosSubject, new PrivilegedExceptionAction<Object>() {

                    @Override
                    public Object run() throws Exception {
                        p.sasllogin(saslMechanisms, saslRealm, authzid, u, pw);
                        return null;
                    }
                });
            } catch (final PrivilegedActionException e) {
                handlePrivilegedActionException(e);
            }
        } else {
            // Do regular authentication
            super.login(p, u, pw);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            releaseSemaphore();
        }
    }

    @Override
    protected void logout(final IMAPProtocol protocol) throws ProtocolException {
        try {
            super.logout(protocol);
        } finally {
            releaseSemaphore();
        }
    }

    private void releaseSemaphore() {
        final Semaphore semaphore = this.semaphore;
        if (null != semaphore) {
            semaphore.release();
            this.semaphore = null;
        }
    }

    private static void handlePrivilegedActionException(final PrivilegedActionException e) throws ProtocolException {
        if (null == e) {
            return;
        }
        final Exception cause = e.getException();
        if (null == cause) {
            throw new ProtocolException(e.getMessage(), e);
        }
        if (cause instanceof ProtocolException) {
            throw (ProtocolException) cause;
        }
        if (cause instanceof MessagingException) {
            final MessagingException me = (MessagingException) cause;
            final Exception nextException = me.getNextException();
            if (nextException instanceof ProtocolException) {
                throw (ProtocolException) nextException;
            }
            throw new ProtocolException(me.getMessage(), me);
        }
        throw new ProtocolException(e.getMessage(), cause);
    }
}
