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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import com.openexchange.mail.MailException;
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
     * @throws MailException If instance cannot be returned
     */
    public static SessionPOP3StorageProperties getInstance(final POP3Access pop3Access) throws MailException {
        final Session session = pop3Access.getSession();
        final String key = SessionParameterNames.getStorageProperties(pop3Access.getAccountId());
        SessionPOP3StorageProperties cached;
        try {
            cached = (SessionPOP3StorageProperties) session.getParameter(key);
        } catch (final ClassCastException e) {
            cached = null;
        }
        if (null == cached) {
            cached = new SessionPOP3StorageProperties(new RdbPOP3StorageProperties(pop3Access), session, key);
            session.setParameter(key, cached);
        }
        return cached;
    }

    /*-
     * Member section
     */

    private final Map<String, String> map;

    private final POP3StorageProperties delegatee;

    private final ReadWriteLock rwLock;

    /**
     * Initializes a new {@link SessionPOP3StorageProperties}.
     */
    private SessionPOP3StorageProperties(final POP3StorageProperties delegatee, final Session session, final String key) {
        super();
        rwLock = new ReentrantReadWriteLock();
        this.delegatee = delegatee;
        map = new ConcurrentHashMap<String, String>();
        final CleanMapRunnable cmr = new CleanMapRunnable(session, key, map, rwLock);
        final ScheduledTimerTask timerTask = POP3ServiceRegistry.getServiceRegistry().getService(TimerService.class).scheduleWithFixedDelay(
            cmr,
            1000,
            300000);
        cmr.setTimerTask(timerTask);
    }

    public void addProperty(final String propertyName, final String propertyValue) throws MailException {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            map.put(propertyName, propertyValue);
            delegatee.addProperty(propertyName, propertyValue);
        } finally {
            readLock.unlock();
        }
    }

    public String getProperty(final String propertyName) throws MailException {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
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

    public void removeProperty(final String propertyName) throws MailException {
        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            map.remove(propertyName);
            delegatee.removeProperty(propertyName);
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

        private int countEmptyRuns;

        public CleanMapRunnable(final Session tsession, final String tkey, final Map<String, String> tmap, final ReadWriteLock trwLock) {
            super();
            this.tsession = tsession;
            this.tkey = tkey;
            this.tmap = tmap;
            this.trwLock = trwLock;
        }

        public void run() {
            if (countEmptyRuns >= 2) {
                // Destroy!
                timerTask.cancel();
                tsession.setParameter(tkey, null);
            }
            if (tmap.isEmpty()) {
                countEmptyRuns++;
                return;
            }
            final Lock writeLock = trwLock.writeLock();
            writeLock.lock();
            try {
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
