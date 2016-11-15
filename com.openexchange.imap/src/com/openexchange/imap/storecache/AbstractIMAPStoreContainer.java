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

package com.openexchange.imap.storecache;

import static com.openexchange.imap.IMAPAccess.doIMAPConnect;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import com.openexchange.imap.IMAPClientParameters;
import com.openexchange.log.LogProperties;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link AbstractIMAPStoreContainer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractIMAPStoreContainer implements IMAPStoreContainer {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractIMAPStoreContainer.class);

    protected final int accountId;
    protected final String name;
    protected final boolean propagateClientIp;
    protected final Session session;

    /**
     * Initializes a new {@link AbstractIMAPStoreContainer}.
     */
    protected AbstractIMAPStoreContainer(int accountId, Session session, boolean propagateClientIp) {
        super();
        this.accountId = accountId;
        this.session = session;
        this.propagateClientIp = propagateClientIp;
        name = PROTOCOL_NAME;
    }

    /**
     * Gets a newly created & connected {@link IMAPStore} instance.
     *
     * @param server The host name
     * @param port The port
     * @param login The login
     * @param pw The password
     * @param imapSession The IMAP session
     * @param session The Groupware session
     * @return The newly created & connected {@link IMAPStore} instance
     * @throws MessagingException If operation fails
     */
    protected IMAPStore newStore(String server, int port, String login, String pw, javax.mail.Session imapSession, Session session) throws MessagingException {
        /*
         * Get new store...
         */
        IMAPStore imapStore = (IMAPStore) imapSession.getStore(name);
        if (propagateClientIp) {
            imapStore.setPropagateClientIpAddress(session.getLocalIp());
        }
        IMAPClientParameters.setDefaultClientParameters(imapStore, session);
        /*
         * ... and connect it
         */
        try {
            doIMAPConnect(imapSession, imapStore, server, port, login, pw, accountId, session, false);
        } catch (final AuthenticationFailedException e) {
            /*
             * Retry connect with AUTH=PLAIN disabled
             */
            imapSession.getProperties().put("mail.imap.auth.login.disable", "true");
            imapStore = (IMAPStore) imapSession.getStore(name);
            doIMAPConnect(imapSession, imapStore, server, port, login, pw, accountId, session, false);
        }

        String sessionInformation = imapStore.getClientParameter(IMAPClientParameters.SESSION_ID.getParamName());
        if (null != sessionInformation) {
            LogProperties.put(LogProperties.Name.MAIL_SESSION, sessionInformation);
        }

        return imapStore;
    }

    /**
     * Safely closes specified IMAP store.
     *
     * @param imapStore The IMAP store
     */
    protected static void closeSafe(final IMAPStore imapStore) {
        if (null != imapStore) {
            try {
                imapStore.close();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Tiny wrapper class
     */
    protected static class IMAPStoreWrapper implements Comparable<IMAPStoreWrapper> {

        protected final IMAPStore imapStore;
        protected final long lastAccessed;
        private final int hash;

        protected IMAPStoreWrapper(final IMAPStore imapStore) {
            super();
            this.imapStore = imapStore;
            lastAccessed = System.currentTimeMillis();
            final int prime = 31;
            int result = 1;
            result = prime * result + ((imapStore == null) ? 0 : imapStore.hashCode());
            hash = result;
        }

        @Override
        public int compareTo(final IMAPStoreWrapper other) {
            final long thisVal = this.lastAccessed;
            final long anotherVal = other.lastAccessed;
            return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof IMAPStoreWrapper)) {
                return false;
            }
            final IMAPStoreWrapper other = (IMAPStoreWrapper) obj;
            if (imapStore == null) {
                if (other.imapStore != null) {
                    return false;
                }
            } else if (imapStore != other.imapStore) {
                return false;
            }
            return true;
        }
    }

}
