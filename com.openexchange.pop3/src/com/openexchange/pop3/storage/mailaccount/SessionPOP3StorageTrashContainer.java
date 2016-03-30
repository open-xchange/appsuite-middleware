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

package com.openexchange.pop3.storage.mailaccount;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.storage.POP3StorageTrashContainer;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link SessionPOP3StorageTrashContainer} - Session-backed implementation of {@link POP3StorageTrashContainer}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionPOP3StorageTrashContainer implements POP3StorageTrashContainer {

    private static final Object PRESENT = new Object();

    /**
     * Gets the trash container bound to specified POP3 access.
     *
     * @param pop3Access The POP3 access
     * @return The trash container bound to specified POP3 access
     * @throws OXException If instance cannot be returned
     */
    public static SessionPOP3StorageTrashContainer getInstance(final POP3Access pop3Access) throws OXException {
        final Session session = pop3Access.getSession();
        final String key = SessionParameterNames.getTrashContainer(pop3Access.getAccountId());
        SessionPOP3StorageTrashContainer cached;
        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (null == lock) {
            lock = Session.EMPTY_LOCK;
        }
        lock.lock();
        try {
            try {
                cached = (SessionPOP3StorageTrashContainer) session.getParameter(key);
            } catch (final ClassCastException e) {
                cached = null;
            }
            if (null == cached) {
                cached = new SessionPOP3StorageTrashContainer(new RdbPOP3StorageTrashContainer(pop3Access), session, key);
                session.setParameter(key, cached);
            }
        } finally {
            lock.unlock();
        }
        return cached;
    }

    /*-
     * Member section
     */

    private final ReadWriteLock rwLock;

    private final int[] mode;

    private final POP3StorageTrashContainer delegatee;

    private final Map<String, Object> set;

    private SessionPOP3StorageTrashContainer(final POP3StorageTrashContainer delegatee, final Session session, final String key) throws OXException {
        super();
        rwLock = new ReentrantReadWriteLock();
        this.delegatee = delegatee;
        set = new ConcurrentHashMap<String, Object>();
        mode = new int[] { 1 };
        final CleanSetRunnable csr = new CleanSetRunnable(session, key, set, rwLock, mode);
        final ScheduledTimerTask timerTask = POP3ServiceRegistry.getServiceRegistry().getService(TimerService.class).scheduleWithFixedDelay(
            csr,
            SessionCacheProperties.SCHEDULED_TASK_INITIAL_DELAY,
            SessionCacheProperties.SCHEDULED_TASK_DELAY);
        csr.setTimerTask(timerTask);
        init();
    }

    private void init() throws OXException {
        if (1 == mode[0]) {
            final Set<String> tmp = delegatee.getUIDLs();
            for (final String uidl : tmp) {
                set.put(uidl, PRESENT);
            }
            mode[0] = 0;
        }
    }

    private void checkInit(final Lock obtainedReadLock) throws OXException {
        final int m = mode[0];
        if (-1 == m) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create("Error mode. Try again.");
        }
        if (1 == m) {
            /*
             * Upgrade lock: unlock first to acquire write lock
             */
            obtainedReadLock.unlock();
            final Lock writeLock = rwLock.writeLock();
            writeLock.lock();
            try {
                init();
            } finally {
                /*
                 * Downgrade lock: reacquire read without giving up write lock and...
                 */
                obtainedReadLock.lock();
                /*
                 * ... unlock write.
                 */
                writeLock.unlock();
            }
        }
    }

    @Override
    public void addUIDL(final String uidl) throws OXException {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            checkInit(readLock);
            delegatee.addUIDL(uidl);
            set.put(uidl, PRESENT);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() throws OXException {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            checkInit(readLock);
            delegatee.clear();
            set.clear();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<String> getUIDLs() throws OXException {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            checkInit(readLock);
            final Set<String> tmp = new HashSet<String>();
            tmp.addAll(set.keySet());
            return tmp;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void removeUIDL(final String uidl) throws OXException {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            checkInit(readLock);
            delegatee.removeUIDL(uidl);
            set.remove(uidl);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void addAllUIDL(final Collection<? extends String> uidls) throws OXException {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            checkInit(readLock);
            delegatee.addAllUIDL(uidls);
            for (final String uidl : uidls) {
                set.put(uidl, PRESENT);
            }
        } finally {
            readLock.unlock();
        }
    }

    private static final class CleanSetRunnable implements Runnable {

        private final Session tsession;

        private final String tkey;

        private final Map<String, Object> tset;

        private final ReadWriteLock trwLock;

        private final int[] tmode;

        private ScheduledTimerTask timerTask;

        private int countEmptyRuns;

        public CleanSetRunnable(final Session tsession, final String tkey, final Map<String, Object> tset, final ReadWriteLock trwLock, final int[] tmode) {
            super();
            this.tsession = tsession;
            this.tkey = tkey;
            this.tset = tset;
            this.trwLock = trwLock;
            this.tmode = tmode;
        }

        @Override
        public void run() {
            final Lock writeLock = trwLock.writeLock();
            writeLock.lock();
            try {
                if (tset.isEmpty()) {
                    if (countEmptyRuns >= SessionCacheProperties.SCHEDULED_TASK_ALLOWED_EMPTY_RUNS) {
                        // Destroy!
                        timerTask.cancel();
                        synchronized (tsession) {
                            tsession.setParameter(tkey, null);
                            tmode[0] = -1;
                        }
                    }
                    countEmptyRuns++;
                    return;
                }
                countEmptyRuns = 0;
                tset.clear();
                tmode[0] = 1;
            } finally {
                writeLock.unlock();
            }
        }

        public void setTimerTask(final ScheduledTimerTask timerTask) {
            this.timerTask = timerTask;
        }

    }

}
