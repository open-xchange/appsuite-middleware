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

package com.openexchange.pop3.storage.mailaccount;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
            } catch (ClassCastException e) {
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

    private final Lock lock;

    private final int[] mode;

    private final POP3StorageTrashContainer delegatee;

    private final Map<String, Object> set;

    private SessionPOP3StorageTrashContainer(final POP3StorageTrashContainer delegatee, final Session session, final String key) throws OXException {
        super();
        lock = new ReentrantLock();
        this.delegatee = delegatee;
        set = new ConcurrentHashMap<String, Object>();
        mode = new int[] { 1 };
        final CleanSetRunnable csr = new CleanSetRunnable(session, key, set, lock, mode);
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

    private void checkInit() throws OXException {
        final int m = mode[0];
        if (-1 == m) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create("Error mode. Try again.");
        }
        if (1 == m) {
            init();
        }
    }

    @Override
    public void addUIDL(final String uidl) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            delegatee.addUIDL(uidl);
            set.put(uidl, PRESENT);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            delegatee.clear();
            set.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<String> getUIDLs() throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            final Set<String> tmp = new HashSet<String>();
            tmp.addAll(set.keySet());
            return tmp;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeUIDL(final String uidl) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            delegatee.removeUIDL(uidl);
            set.remove(uidl);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void addAllUIDL(final Collection<? extends String> uidls) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            delegatee.addAllUIDL(uidls);
            for (final String uidl : uidls) {
                set.put(uidl, PRESENT);
            }
        } finally {
            lock.unlock();
        }
    }

    // -------------------------------------------------------------------------------------------------

    private static final class CleanSetRunnable implements Runnable {

        private final Session tsession;

        private final String tkey;

        private final Map<String, Object> tset;

        private final Lock tLock;

        private final int[] tmode;

        private ScheduledTimerTask timerTask;

        private int countEmptyRuns;

        public CleanSetRunnable(final Session tsession, final String tkey, final Map<String, Object> tset, final Lock tLock, final int[] tmode) {
            super();
            this.tsession = tsession;
            this.tkey = tkey;
            this.tset = tset;
            this.tLock = tLock;
            this.tmode = tmode;
        }

        @Override
        public void run() {
            final Lock writeLock = tLock;
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
