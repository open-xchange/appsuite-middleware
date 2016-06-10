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

    private volatile Limiter limiter;

    /**
     * Initializes a new {@link BoundaryAwareIMAPStoreContainer}.
     */
    public BoundaryAwareIMAPStoreContainer(int accountId, Session session, String server, int port, boolean propagateClientIp, boolean checkConnectivityIfPolled) {
        super(accountId, session, server, port, propagateClientIp, checkConnectivityIfPolled);
    }

    /**
     * Gets the limiter
     *
     * @return The limiter
     */
    private Limiter getLimiter(final int max) {
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
    public IMAPStore getStore(javax.mail.Session imapSession, String login, String pw, Session session) throws MessagingException, InterruptedException {
        final int maxNumAuthenticated = PropUtil.getIntSessionProperty(imapSession, "mail.imap.maxNumAuthenticated", 0);
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
        synchronized (limiter) {
            int count = maxRetryCount;
            while (count-- > 0 && !limiter.acquire()) {
                LOG.debug("BoundaryAwareIMAPStoreContainer.getStore(): W A I T I N G -- {}", limiter);
                limiter.wait(2000);
            }
            if (count <= 0) {
                // Timed out -- So what...?
                LOG.debug("BoundaryAwareIMAPStoreContainer.getStore(): T I M E D   O U T -- {}", limiter);
                // /final String message = "Max. number of connections exceeded. Try again later.";
                // /throw new MessagingException(message, new com.sun.mail.iap.ConnectQuotaExceededException(message));
            }
        }
        LOG.debug("BoundaryAwareIMAPStoreContainer.getStore(): Acquired -- {}", limiter);
        return super.getStore(imapSession, login, pw, session);
    }

    @Override
    public void backStore(IMAPStore imapStore) {
        try {
            super.backStore(imapStore);
        } finally {
            // Do not forget to release previously acquired permit
            final Limiter tmp = limiter;
            if (null != tmp) {
                synchronized (tmp) {
                    tmp.release();
                    tmp.notifyAll();
                    LOG.debug("BoundaryAwareIMAPStoreContainer.backStore(): Released -- {}", tmp);
                }
            }
        }
    }

}
