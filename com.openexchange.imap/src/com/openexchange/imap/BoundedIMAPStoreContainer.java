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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.imap;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.mail.MessagingException;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link BoundedIMAPStoreContainer} - The unbounded {@link IMAPStoreContainer}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoundedIMAPStoreContainer extends UnboundedIMAPStoreContainer {

    private final AtomicInteger counter;

    private final int max;

    private final Lock lock;

    private final Condition condition;

    /**
     * Initializes a new {@link BoundedIMAPStoreContainer}.
     */
    public BoundedIMAPStoreContainer(final String name, final String server, final int port, final String login, final String pw, final int maxCount) {
        super(name, server, port, login, pw);
        max = maxCount;
        counter = new AtomicInteger();
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    private void check() throws InterruptedException {
        int cur;
        do {
            cur = counter.get();
            while (cur >= max) {
                // Await
                lock.lock();
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(new StringBuilder(64).append("Awaiting free IMAP store for: imap://").append(login).append('@').append(
                            server).append(':').append(port).toString());
                    }
                    condition.await();
                } finally {
                    lock.unlock();
                }
                cur = counter.get();
            }
        } while (!counter.compareAndSet(cur, cur + 1));
    }

    @Override
    public IMAPStore getStore(final javax.mail.Session imapSession) throws MessagingException, InterruptedException {
        check();
        return super.getStore(imapSession);
    }

    @Override
    public void backStore(final IMAPStore imapStore) {
        super.backStore(imapStore);
        counter.decrementAndGet();
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

}
