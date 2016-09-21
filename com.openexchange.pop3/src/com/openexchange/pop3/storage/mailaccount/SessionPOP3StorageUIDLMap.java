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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.exception.OXException;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.storage.FullnameUIDPair;
import com.openexchange.pop3.storage.POP3StorageUIDLMap;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link SessionPOP3StorageUIDLMap} - Session-backed implementation of {@link POP3StorageUIDLMap}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionPOP3StorageUIDLMap implements POP3StorageUIDLMap {

    /**
     * Gets the UIDL map bound to specified POP3 access.
     *
     * @param pop3Access The POP3 access
     * @return The UIDL map bound to specified POP3 access
     * @throws OXException If instance cannot be returned
     */
    public static SessionPOP3StorageUIDLMap getInstance(final POP3Access pop3Access) throws OXException {
        final Session session = pop3Access.getSession();
        final String key = SessionParameterNames.getUIDLMap(pop3Access.getAccountId());
        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (null == lock) {
            lock = Session.EMPTY_LOCK;
        }
        SessionPOP3StorageUIDLMap cached;
        lock.lock();
        try {
            try {
                cached = (SessionPOP3StorageUIDLMap) session.getParameter(key);
            } catch (final ClassCastException e) {
                cached = null;
            }
            if (null == cached) {
                cached = new SessionPOP3StorageUIDLMap(new RdbPOP3StorageUIDLMap(pop3Access));
                session.setParameter(key, cached);
            }
        } finally {
            lock.unlock();
        }
        return cached;
    }

    static enum Mode {
        NONE,RE_INIT,START_TIMER;
    }

    /*-
     * Member section
     */

    private final Map<String, FullnameUIDPair> uidl2pair;
    private final Map<FullnameUIDPair, String> pair2uidl;
    private final POP3StorageUIDLMap delegatee;
    private final Lock lock;
    private final AtomicReference<Mode> mode;

    private SessionPOP3StorageUIDLMap(final POP3StorageUIDLMap delegatee) throws OXException {
        super();
        lock = new ReentrantLock();
        this.delegatee = delegatee;
        pair2uidl = new ConcurrentHashMap<FullnameUIDPair, String>();
        uidl2pair = new ConcurrentHashMap<String, FullnameUIDPair>();
        mode = new AtomicReference<Mode>(Mode.RE_INIT);
        initTimerTask();
        init();
    }

    private void initTimerTask() {
        final ClearMapsRunnable cmr = new ClearMapsRunnable(uidl2pair, pair2uidl, lock, mode);
        final TimerService timerService = POP3ServiceRegistry.getServiceRegistry().getService(TimerService.class);
        final ScheduledTimerTask timerTask =
            timerService.scheduleWithFixedDelay(
                cmr,
                SessionCacheProperties.SCHEDULED_TASK_DELAY,
                SessionCacheProperties.SCHEDULED_TASK_DELAY);
        cmr.setTimerTask(timerTask);
        mode.set(Mode.RE_INIT);
    }

    private void init() throws OXException {
        if (Mode.RE_INIT == mode.get()) {
            final Map<String, FullnameUIDPair> all = delegatee.getAllUIDLs();
            final int size = all.size();
            final Iterator<Entry<String, FullnameUIDPair>> iter = all.entrySet().iterator();
            for (int i = 0; i < size; i++) {
                final Entry<String, FullnameUIDPair> entry = iter.next();
                pair2uidl.put(entry.getValue(), entry.getKey());
                uidl2pair.put(entry.getKey(), entry.getValue());
            }
            mode.set(Mode.NONE);
        }
    }

    private void checkInit() throws OXException {
        final Mode m = mode.get();
        if (Mode.START_TIMER == m) {
            initTimerTask();
        }
        if (Mode.RE_INIT == m) {
            init();
        }
    }

    @Override
    public void addMappings(final String[] uidls, final FullnameUIDPair[] fullnameUIDPairs) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            delegatee.addMappings(uidls, fullnameUIDPairs);
            for (int i = 0; i < fullnameUIDPairs.length; i++) {
                final String uidl = uidls[i];
                if (null != uidl) {
                    final FullnameUIDPair pair = fullnameUIDPairs[i];
                    pair2uidl.put(pair, uidl);
                    uidl2pair.put(uidl, pair);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public FullnameUIDPair getFullnameUIDPair(final String uidl) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            return uidl2pair.get(uidl);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public FullnameUIDPair[] getFullnameUIDPairs(final String[] uidls) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            final FullnameUIDPair[] pairs = new FullnameUIDPair[uidls.length];
            for (int i = 0; i < pairs.length; i++) {
                pairs[i] = getFullnameUIDPair(uidls[i]);
            }
            return pairs;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getUIDL(final FullnameUIDPair fullnameUIDPair) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            return pair2uidl.get(fullnameUIDPair);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String[] getUIDLs(final FullnameUIDPair[] fullnameUIDPairs) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            final String[] uidls = new String[fullnameUIDPairs.length];
            for (int i = 0; i < uidls.length; i++) {
                uidls[i] = getUIDL(fullnameUIDPairs[i]);
            }
            return uidls;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<String, FullnameUIDPair> getAllUIDLs() throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            final Map<String, FullnameUIDPair> copy = new HashMap<String, FullnameUIDPair>();
            copy.putAll(uidl2pair);
            return copy;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void deleteFullnameUIDPairMappings(final FullnameUIDPair[] fullnameUIDPairs) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            delegatee.deleteFullnameUIDPairMappings(fullnameUIDPairs);
            for (int i = 0; i < fullnameUIDPairs.length; i++) {
                final String uidl = pair2uidl.remove(fullnameUIDPairs[i]);
                if (null != uidl) {
                    uidl2pair.remove(uidl);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void deleteUIDLMappings(final String[] uidls) throws OXException {
        Lock lock = this.lock;
        lock.lock();
        try {
            checkInit();
            delegatee.deleteUIDLMappings(uidls);
            for (int i = 0; i < uidls.length; i++) {
                final FullnameUIDPair pair = uidl2pair.remove(uidls[i]);
                if (null != pair) {
                    pair2uidl.remove(pair);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    private static final class ClearMapsRunnable implements Runnable {

        private final Map<String, FullnameUIDPair> tuidl2pair;

        private final Map<FullnameUIDPair, String> tpair2uidl;

        private final Lock tLock;

        private final AtomicReference<Mode> tmode;

        private volatile ScheduledTimerTask timerTask;

        private int countEmptyRuns;

        public ClearMapsRunnable(final Map<String, FullnameUIDPair> tuidl2pair, final Map<FullnameUIDPair, String> tpair2uidl, final Lock tLock, final AtomicReference<Mode> tmode) {
            super();
            this.tLock = tLock;
            this.tuidl2pair = tuidl2pair;
            this.tpair2uidl = tpair2uidl;
            this.tmode = tmode;
        }

        @Override
        public void run() {
            final Lock writeLock = tLock;
            writeLock.lock();
            try {
                if (tuidl2pair.isEmpty() && tpair2uidl.isEmpty()) {
                    if (countEmptyRuns >= SessionCacheProperties.SCHEDULED_TASK_ALLOWED_EMPTY_RUNS) {
                        // Destroy!
                        final ScheduledTimerTask timerTask = this.timerTask;
                        if (null != timerTask) {
                            timerTask.cancel();
                        }
                        tmode.set(Mode.START_TIMER);
                        // synchronized (tsession) {
                        // tsession.setParameter(tkey, null);
                        // tmode[0] = -1;
                        // }
                    }
                    countEmptyRuns++;
                    return;
                }
                countEmptyRuns = 0;
                tuidl2pair.clear();
                tpair2uidl.clear();
                tmode.set(Mode.RE_INIT);
            } finally {
                writeLock.unlock();
            }
        }

        public void setTimerTask(final ScheduledTimerTask timerTask) {
            this.timerTask = timerTask;
        }

    }

}
