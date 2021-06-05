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
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.security.auth.Subject;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.PropUtil;

/**
 * {@link JavaIMAPStore} - Extends {@link IMAPStore} by improved support for Kerberos authentication.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JavaIMAPStore extends IMAPStore {

    /** Whether SASL is enabled. */
    private final boolean enableSASL;

    /** The Kerberos subject. */
    private transient Subject kerberosSubject;

    /** The SASL mechansims */
    protected String[] saslMechanisms;

    /** The SASL realm */
    protected String m_saslRealm;

    /** Authorization ID */
    private String m_authorizationID;

    /** Proxy auth user */
    private String m_proxyAuthUser;

    /** The flag whether to count */
    private final boolean count;

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
        Properties properties = session.getProperties();
        enableSASL = PropUtil.getBooleanProperty(properties, "mail.imap.sasl.enable", false);
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
                m_saslRealm = s;
            }
            // Check if an authorization ID has been specified
            s = session.getProperty("mail." + name + ".sasl.authorizationid");
            if (s != null) {
                m_authorizationID = s;
            }
            // Check if we should do a PROXYAUTH login
            s = session.getProperty("mail." + name + ".proxyauth.user");
            if (s != null) {
                m_proxyAuthUser = s;
            }
        }
        // Whether to count or not
        count = PropUtil.getBooleanProperty(properties, "mail.imap.count.enable", false);
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
    protected IMAPProtocol newIMAPProtocol(String host, int port, String user, String password) throws IOException, ProtocolException {
        if (count) {
            return new CountingIMAPProtocol(name, host, port, user, session.getProperties(), isSSL, logger);
        }
        return super.newIMAPProtocol(host, port, user, password);
    }

    @Override
    protected void login(final IMAPProtocol p, final String u, final String pw) throws ProtocolException {
        if (p.isAuthenticated()) {
            super.login(p, u, pw);
            return;
        }
        // Check for regular or Kerberos authentication
        if (!enableSASL || null == kerberosSubject) {
            // Do regular authentication
            super.login(p, u, pw);
            return;
        }
        // Do Kerberos authentication
        final String authzid;
        if (m_authorizationID != null) {
            authzid = m_authorizationID;
        } else if (m_proxyAuthUser != null) {
            authzid = m_proxyAuthUser;
        } else {
            authzid = null;
        }
        try {
            Subject.doAs(kerberosSubject, new PrivilegedExceptionAction<Object>() {

                @Override
                public Object run() throws Exception {
                    p.sasllogin(saslMechanisms, m_saslRealm, authzid, u, pw);
                    if (!p.isAuthenticated()) {
                        throw new CommandFailedException(
                                    "SASL authentication failed");
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            handlePrivilegedActionException(e);
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
