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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.storage.POP3StorageProperties;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link SessionPOP3StorageProperties} - Session-backed implementation of {@link POP3StorageProperties}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionPOP3StorageProperties implements POP3StorageProperties {

    /**
     * Gets the storage properties bound to specified POP3 access.
     *
     * @param pop3Access The POP3 access
     * @return The storage properties bound to specified POP3 access
     */
    public static SessionPOP3StorageProperties getInstance(final POP3Access pop3Access) {
        final Session session = pop3Access.getSession();
        final String key = SessionParameterNames.getStorageProperties(pop3Access.getAccountId());
        SessionPOP3StorageProperties cached;
        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (null == lock) {
            lock = Session.EMPTY_LOCK;
        }
        lock.lock();
        try {
            try {
                cached = (SessionPOP3StorageProperties) session.getParameter(key);
            } catch (ClassCastException e) {
                cached = null;
            }
            if (null == cached) {
                cached = new SessionPOP3StorageProperties(new RdbPOP3StorageProperties(pop3Access), session, key);
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

    private final Map<String, String> map;

    private final POP3StorageProperties delegatee;

    private final Lock lock;

    private final boolean[] invalid;

    /**
     * Initializes a new {@link SessionPOP3StorageProperties}.
     */
    private SessionPOP3StorageProperties(final POP3StorageProperties delegatee, final Session session, final String key) {
        super();
        lock = new ReentrantLock();
        invalid = new boolean[] { false };
        this.delegatee = delegatee;
        map = new ConcurrentHashMap<String, String>();
        final CleanMapRunnable cmr = new CleanMapRunnable(session, key, map, lock, invalid);
        final ScheduledTimerTask timerTask = POP3ServiceRegistry.getServiceRegistry().getService(TimerService.class).scheduleWithFixedDelay(
            cmr,
            SessionCacheProperties.SCHEDULED_TASK_INITIAL_DELAY,
            SessionCacheProperties.SCHEDULED_TASK_DELAY);
        cmr.setTimerTask(timerTask);
    }

    private void checkValid() throws OXException {
        if (invalid[0]) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create("Error mode. Try again.");
        }
    }

    @Override
    public void addProperty(final String propertyName, final String propertyValue) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkValid();
            delegatee.addProperty(propertyName, propertyValue);
            map.put(propertyName, propertyValue);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getProperty(final String propertyName) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkValid();
            if (map.containsKey(propertyName)) {
                return map.get(propertyName);
            }
            final String value = delegatee.getProperty(propertyName);
            if (null != value) {
                map.put(propertyName, value);
            }
            return value;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeProperty(final String propertyName) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkValid();
            delegatee.removeProperty(propertyName);
            map.remove(propertyName);
        } finally {
            lock.unlock();
        }
    }

    private static final class CleanMapRunnable implements Runnable {

        private final Session tsession;

        private final String tkey;

        private final Map<String, String> tmap;

        private final Lock tLock;

        private ScheduledTimerTask timerTask;

        private final boolean[] tinvalid;

        private int countEmptyRuns;

        public CleanMapRunnable(final Session tsession, final String tkey, final Map<String, String> tmap, final Lock tLock, final boolean[] tinvalid) {
            super();
            this.tsession = tsession;
            this.tkey = tkey;
            this.tmap = tmap;
            this.tLock = tLock;
            this.tinvalid = tinvalid;
        }

        @Override
        public void run() {
            final Lock writeLock = tLock;
            writeLock.lock();
            try {
                if (tmap.isEmpty()) {
                    if (countEmptyRuns >= SessionCacheProperties.SCHEDULED_TASK_ALLOWED_EMPTY_RUNS) {
                        // Destroy!
                        timerTask.cancel();
                        synchronized (tsession) {
                            tsession.setParameter(tkey, null);
                            tinvalid[0] = true;
                        }
                    }
                    countEmptyRuns++;
                    return;
                }
                countEmptyRuns = 0;
                tmap.clear();
            } finally {
                writeLock.unlock();
            }
        }

        public void setTimerTask(final ScheduledTimerTask timerTask) {
            this.timerTask = timerTask;
        }
    }
}
