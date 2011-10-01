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

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import com.openexchange.exception.OXException;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link IMAPStorePool} - The IMAP store pool.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPStorePool {

    private static volatile IMAPStorePool instance;

    /**
     * Gets the instance.
     * 
     * @return The instance.
     */
    public static IMAPStorePool getInstance() {
        IMAPStorePool tmp = instance;
        if (null == tmp) {
            synchronized (IMAPStorePool.class) {
                tmp = instance;
                if (null == tmp) {
                    tmp = instance = new IMAPStorePool();
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the instance.
     */
    public static void releaseInstance() {
        if (null != instance) {
            synchronized (IMAPStorePool.class) {
                if (null != instance) {
                    for (final ConcurrentMap<Integer, AccountPool> accountMap : instance.map.values()) {
                        shutDownAccountMap(accountMap);
                    }
                    instance = null;
                }
            }
        }
    }

    private static void shutDownAccountMap(final ConcurrentMap<Integer, AccountPool> accountMap) {
        if (null == accountMap) {
            return;
        }
        for (final Iterator<AccountPool> iterator = accountMap.values().iterator(); iterator.hasNext();) {
            final AccountPool accountPool = iterator.next();
            accountPool.shutDown();
            iterator.remove();
        }
        accountMap.clear();
    }

    /*-
     * --------------------------------- MEMBER STUFF ---------------------------------
     */

    private final ConcurrentMap<Key, ConcurrentMap<Integer, AccountPool>> map;

    /**
     * Initializes a new {@link IMAPStorePool}.
     */
    private IMAPStorePool() {
        super();
        map = new ConcurrentHashMap<Key, ConcurrentMap<Integer, AccountPool>>();
    }

    /**
     * Drops all user-associated IMAP stores if no more active sessions exist.
     * 
     * @param session The dropped user session
     */
    public void dropLast(final Session session) {
        shutDownAccountMap(map.remove(keyFor(session)));
    }

    /**
     * Gets the account pool for specified IMAP access.
     * 
     * @param imapAccess The IMAP access
     * @return The account pool
     * @throws OXException If account pool cannot be returned
     */
    public AccountPool getAccountPool(final IMAPAccess imapAccess) throws OXException {
        final Key key = keyFor(imapAccess.getSession());
        ConcurrentMap<Integer, AccountPool> accountMap = map.get(key);
        if (null == accountMap) {
            final ConcurrentMap<Integer, AccountPool> newAccountMap = new ConcurrentHashMap<Integer, AccountPool>(8);
            accountMap = map.putIfAbsent(key, newAccountMap);
            if (null == accountMap) {
                accountMap = newAccountMap;
            }
        }
        final int accountId = imapAccess.getAccountId();
        final Integer acc = Integer.valueOf(accountId);
        AccountPool accountPool = accountMap.get(acc);
        if (null == accountPool) {
            final IMAPProtocol imapProtocol = IMAPProtocol.getInstance();
            final int maxCount = imapProtocol.getMaxCount(imapAccess.getIMAPConfig().getServer(), MailAccount.DEFAULT_ID == accountId);
            final AccountPool newAccountPool = new AccountPool(maxCount);
            accountPool = accountMap.putIfAbsent(acc, newAccountPool);
            if (null == accountPool) {
                accountPool = newAccountPool;
            }
        }
        return accountPool;
    }

    /**
     * Gets the account pool (if any) for specified IMAP access.
     * 
     * @param imapAccess The IMAP access
     * @return The account pool or <code>null</code>
     */
    public AccountPool optAccountPool(final IMAPAccess imapAccess) {
        final ConcurrentMap<Integer, AccountPool> accountMap = map.get(keyFor(imapAccess.getSession()));
        if (null == accountMap) {
            return null;
        }
        return accountMap.get(Integer.valueOf(imapAccess.getAccountId()));
    }

    /*-
     * ------------------------------ HELPER CLASSES ----------------------------------
     */

    public static final class AccountPool {

        private static final int SHRINKER_MILLIS =
            (MailProperties.getInstance().getMailAccessCacheShrinkerSeconds() <= 0 ? 3 : MailProperties.getInstance().getMailAccessCacheShrinkerSeconds()) * 1000;

        private static final int IDLE_MILLIS = MailProperties.getInstance().getMailAccessCacheIdleSeconds() * 1000;

        private final Object mutex;

        private final BlockingQueue<AccessedIMAPStore> queue;

        private final int capacity;

        private final AtomicInteger numActive;

        private final ScheduledTimerTask timerTask;

        /**
         * Initializes a new {@link AccountPool}.
         * 
         * @throws OXException If time service is absent
         */
        protected AccountPool(final int capacity) throws OXException {
            super();
            mutex = new Object();
            this.capacity = capacity;
            numActive = new AtomicInteger(0);
            if (capacity <= 0) {
                queue = new LinkedBlockingQueue<AccessedIMAPStore>();
            } else {
                queue = new ArrayBlockingQueue<AccessedIMAPStore>(capacity);
            }
            /*
             * Create task
             */
            final int idleMillis = IDLE_MILLIS;
            final Runnable task = new Runnable() {

                @Override
                public void run() {
                    closeElapsed(idleMillis);
                }
            };
            /*
             * Schedule task
             */
            final TimerService service = IMAPServiceRegistry.getService(TimerService.class, true);
            final int shrinkerMillis = SHRINKER_MILLIS;
            timerTask = service.scheduleWithFixedDelay(task, shrinkerMillis, shrinkerMillis);
        }

        /**
         * Applies a connected IMAP store to specified <tt>IMAPAccess</tt> instance.
         * 
         * @param imapAccess The IMAP access
         * @throws MailException
         */
        public void connectedIMAPStoreFor(final IMAPAccess imapAccess) throws OXException {
            AccessedIMAPStore imapStore = borrowIMAPStoreFor0(imapAccess);
            while (null == imapStore) {
                imapStore = borrowIMAPStoreFor0(imapAccess);
            }
        }

        private AccessedIMAPStore borrowIMAPStoreFor0(final IMAPAccess imapAccess) throws OXException {
            final AccessedIMAPStore imapStore = getPooled();
            if (null != imapStore) {
                IMAPAccess.applyStoreTo(imapStore, imapAccess);
                
                System.out.println("Applied pooled IMAP store...");
                System.out.println("NumActive:" + numActive.get() + ", QueueSize:" + queue.size());
                
                return imapStore;
            }
            /*
             * Establish a new one
             */
            if (capacity <= 0) {
                IMAPAccess.connect(imapAccess);
                return imapAccess.getIMAPStore();
            }
            int count;
            do {
                count = numActive.get();
                if (count >= capacity) {
                    // Limit reached
                    synchronized (mutex) {
                        try {
                            
                            System.out.println("---------> Awaiting free connection slot <----------");
                            final long st = System.currentTimeMillis();
                            
                            mutex.wait();
                            
                            System.out.println("---------> Waited " + (System.currentTimeMillis() - st) + "msec <---------");
                        } catch (final InterruptedException e) {
                            // Interrupted
                            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
                        }
                        // Retry...
                        return null;
                    }
                }
            } while (!numActive.compareAndSet(count, count + 1));
            
            System.out.println("Established new IMAP store...");
            System.out.println("NumActive:" + numActive.get() + ", QueueSize:" + queue.size());
            
            IMAPAccess.connect(imapAccess);
            return imapAccess.getIMAPStore();
        }

        private AccessedIMAPStore getPooled() {
            for (final AccessedIMAPStore current : queue) {
                final Lock lock = current.getLock();
                lock.lock();
                try {
                    if (current.getMarker().markInUse()) {
                        return current;
                    }
                } finally {
                    lock.unlock();
                }
            }
            return null;
        }

        /**
         * Returns a previously borrowed IMAP store back to pool.
         * 
         * @param imapStore The IMAP store to return
         */
        public void returnIMAPStore(final AccessedIMAPStore imapStore) {
            if (null == imapStore) {
                return;
            }
            final Lock lock = imapStore.getLock();
            lock.lock();
            try {
                final IMAPStoreMarker marker = imapStore.getMarker();
                if (marker.isPooled()) {
                    marker.setStamp(System.currentTimeMillis());
                    marker.unmarkInUse();
                    synchronized (mutex) {
                        mutex.notifyAll();
                    }
                } else if (queue.offer(imapStore)) {
                    marker.setStamp(System.currentTimeMillis());
                    marker.setPooled();
                    synchronized (mutex) {
                        mutex.notifyAll();
                    }
                } else {
                    IMAPAccess.closeSafely(imapStore.getImapAccess());
                }
                
                System.out.println("NumActive:" + numActive.get() + ", QueueSize:" + queue.size());
            } finally {
                lock.unlock();
            }
        }

        /**
         * Shuts-down this pool.
         */
        protected void shutDown() {
            if (null != timerTask) {
                timerTask.cancel(false);
            }
            while (!queue.isEmpty()) {
                final AccessedIMAPStore imapStore = queue.poll();
                if (null != imapStore) {
                    IMAPAccess.closeSafely(imapStore.getImapAccess());
                }
            }
            numActive.set(0);
        }

        /**
         * Closes pooled IMAP stores that are elapsed.
         * 
         * @param ttl The time-to-live
         */
        protected void closeElapsed(final int ttl) {
            final long now = System.currentTimeMillis();
            boolean notify = false;
            for (final Iterator<AccessedIMAPStore> it = queue.iterator(); it.hasNext();) {
                final AccessedIMAPStore current = it.next();
                final Lock lock = current.getLock();
                lock.lock();
                try {
                    final IMAPStoreMarker marker = current.getMarker();
                    if (!marker.isInUse() && (now - marker.getStamp()) > ttl) {
                        it.remove();
                        IMAPAccess.closeSafely(current.getImapAccess());
                        if (capacity > 0) {
                            numActive.decrementAndGet();
                        }
                        notify = true;
                    }
                } finally {
                    lock.unlock();
                }
            }
            if (notify) {
                synchronized (mutex) {
                    mutex.notifyAll();
                }
                System.out.println("NumActive:" + numActive.get() + ", QueueSize:" + queue.size());
            }
        }

    }

    private static Key keyFor(final Session session) {
        return new Key(session.getUserId(), session.getContextId());
    }

    private static final class Key {

        private final int cid;

        private final int user;

        private final int hash;

        protected Key(final int user, final int cid) {
            super();
            this.user = user;
            this.cid = cid;
            final int prime = 31;
            int result = 1;
            result = prime * result + cid;
            result = prime * result + user;
            hash = result;
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
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (cid != other.cid) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            return true;
        }

    }
}
