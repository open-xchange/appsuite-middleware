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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
            } catch (final ClassCastException e) {
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

    private final ReadWriteLock rwLock;

    private final boolean[] invalid;

    /**
     * Initializes a new {@link SessionPOP3StorageProperties}.
     */
    private SessionPOP3StorageProperties(final POP3StorageProperties delegatee, final Session session, final String key) {
        super();
        rwLock = new ReentrantReadWriteLock();
        invalid = new boolean[] { false };
        this.delegatee = delegatee;
        map = new ConcurrentHashMap<String, String>();
        final CleanMapRunnable cmr = new CleanMapRunnable(session, key, map, rwLock, invalid);
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
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            checkValid();
            delegatee.addProperty(propertyName, propertyValue);
            map.put(propertyName, propertyValue);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getProperty(final String propertyName) throws OXException {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
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
            readLock.unlock();
        }
    }

    @Override
    public void removeProperty(final String propertyName) throws OXException {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            checkValid();
            delegatee.removeProperty(propertyName);
            map.remove(propertyName);
        } finally {
            readLock.unlock();
        }
    }

    private static final class CleanMapRunnable implements Runnable {

        private final Session tsession;

        private final String tkey;

        private final Map<String, String> tmap;

        private final ReadWriteLock trwLock;

        private ScheduledTimerTask timerTask;

        private final boolean[] tinvalid;

        private int countEmptyRuns;

        public CleanMapRunnable(final Session tsession, final String tkey, final Map<String, String> tmap, final ReadWriteLock trwLock, final boolean[] tinvalid) {
            super();
            this.tsession = tsession;
            this.tkey = tkey;
            this.tmap = tmap;
            this.trwLock = trwLock;
            this.tinvalid = tinvalid;
        }

        @Override
        public void run() {
            final Lock writeLock = trwLock.writeLock();
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
