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

package com.openexchange.imap.storecache;

import static com.openexchange.imap.IMAPAccess.doIMAPConnect;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPClientParameters;
import com.openexchange.java.Streams;
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
     * @param session The Open-Xchange session
     * @return The newly created & connected {@link IMAPStore} instance
     * @throws MessagingException If operation fails
     */
    protected IMAPStore newStore(String server, int port, String login, String pw, javax.mail.Session imapSession, Session session) throws MessagingException {
        /*
         * Get new store...
         */
        IMAPStore imapStore = (IMAPStore) imapSession.getStore(name);
        boolean error = true;
        try {
            if (propagateClientIp) {
                imapStore.setPropagateClientIpAddress(session.getLocalIp());
            }
            try {
                IMAPClientParameters.setDefaultClientParameters(imapStore, session);
            } catch (OXException e) {
                throw new MessagingException(e.getMessage(), e);
            }
            /*
             * ... and connect it
             */
            doIMAPConnect(imapSession, imapStore, server, port, login, pw, accountId, session, false);
            error = false;
            return imapStore;
        } finally {
            if (error) {
                Streams.close(imapStore);
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

        protected IMAPStoreWrapper(IMAPStore imapStore) {
            super();
            this.imapStore = imapStore;
            lastAccessed = System.currentTimeMillis();
            int prime = 31;
            int result = 1;
            result = prime * result + ((imapStore == null) ? 0 : imapStore.hashCode());
            hash = result;
        }

        @Override
        public int compareTo(IMAPStoreWrapper other) {
            long thisVal = this.lastAccessed;
            long anotherVal = other.lastAccessed;
            return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof IMAPStoreWrapper)) {
                return false;
            }
            IMAPStoreWrapper other = (IMAPStoreWrapper) obj;
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
