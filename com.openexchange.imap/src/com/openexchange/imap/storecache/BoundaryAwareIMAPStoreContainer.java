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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import javax.mail.MessagingException;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.PropUtil;


/**
 * {@link BoundaryAwareIMAPStoreContainer} - Honors <code>"mail.imap.maxNumAuthenticated"</code> setting in {@link Session IMAP session}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoundaryAwareIMAPStoreContainer extends UnboundedIMAPStoreContainer {

    private final int maxRetryCount;
    private volatile Limiter limiter;

    /**
     * Initializes a new {@link BoundaryAwareIMAPStoreContainer}.
     */
    public BoundaryAwareIMAPStoreContainer(int accountId, Session session, String server, int port, boolean propagateClientIp, boolean checkConnectivityIfPolled, IMAPStoreCache.Key key) {
        super(accountId, session, server, port, propagateClientIp, checkConnectivityIfPolled, key);
        maxRetryCount = 10;
    }

    /**
     * Gets the limiter
     *
     * @return The limiter
     */
    private Limiter getLimiter(int max) {
        Limiter tmp = limiter;
        if (null == tmp) {
            synchronized (this) {
                tmp = limiter;
                if (null == tmp) {
                    tmp = new Limiter(max);
                    limiter = tmp;
                }
            }
        }
        return tmp;
    }

    @Override
    public IMAPStore getStore(javax.mail.Session imapSession, String login, String pw, Session session) throws IMAPStoreContainerInvalidException, MessagingException, InterruptedException {
        final int maxNumAuthenticated = PropUtil.getIntProperty(imapSession.getProperties(), "mail.imap.maxNumAuthenticated", 0);
        if (maxNumAuthenticated <= 0) {
            return super.getStore(imapSession, login, pw, session);
        }

        // Try acquire a permit
        final Limiter limiter = getLimiter(maxNumAuthenticated);
        if (limiter.acquire()) {
            LOG.debug("BoundaryAwareIMAPStoreContainer.getStore(): Acquired -- {}", limiter);
            return super.getStore(imapSession, login, pw, session);
        }

        // Await until permit is available
        boolean acquired = false;
        synchronized (limiter) {
            int retryCount = 0;
            do {
                LOG.debug("BoundaryAwareIMAPStoreContainer.getStore(): W A I T I N G -- {}", limiter);
                exponentialBackoffWait(++retryCount, 1000L); // Exponential back-off
                acquired = limiter.acquire();
            } while (retryCount < maxRetryCount && !acquired);
        }
        
        // Check if acquired
        if (acquired) {
            LOG.debug("BoundaryAwareIMAPStoreContainer.getStore(): Acquired -- {}", limiter);
        } else {
            // Timed out -- So what...?
            LOG.debug("BoundaryAwareIMAPStoreContainer.getStore(): T I M E D   O U T -- {}", limiter);
            // /final String message = "Max. number of connections exceeded. Try again later.";
            // /throw new MessagingException(message, new com.sun.mail.iap.ConnectQuotaExceededException(message));
        }
        return super.getStore(imapSession, login, pw, session);
    }

    /**
     * Performs a wait according to exponential back-off strategy.
     * <pre>
     * (retry-count * base-millis) + random-millis
     * </pre>
     *
     * @param retryCount The current number of retries
     * @param baseMillis The base milliseconds
     */
    private static void exponentialBackoffWait(int retryCount, long baseMillis) {
        long nanosToWait = TimeUnit.NANOSECONDS.convert((retryCount * baseMillis) + ((long) (Math.random() * baseMillis)), TimeUnit.MILLISECONDS);
        LockSupport.parkNanos(nanosToWait);
    }

    @Override
    public void backStore(IMAPStore imapStore) {
        try {
            super.backStore(imapStore);
        } finally {
            // Do not forget to release previously acquired permit
            final Limiter tmp = limiter;
            if (null != tmp) {
                tmp.release();
                synchronized (tmp) {
                    tmp.notifyAll();
                    LOG.debug("BoundaryAwareIMAPStoreContainer.backStore(): Released -- {}", tmp);
                }
            }
        }
    }

}
