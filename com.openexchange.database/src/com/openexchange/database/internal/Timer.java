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

package com.openexchange.database.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.internal.Configuration.Property;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * Responsible to register tasks of database component within {@link TimerService} dynamically.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Timer {

    private static final Logger LOG = LoggerFactory.getLogger(Timer.class);

    private final Lock waitingLock = new ReentrantLock();
    private final List<Runnable> waiting = new ArrayList<Runnable>();
    private final List<Runnable> onceWaiting = new ArrayList<Runnable>();
    private final Lock runningLock = new ReentrantLock();
    private final Map<Runnable, ScheduledTimerTask> running = new HashMap<Runnable, ScheduledTimerTask>();

    private long interval = 10000;
    private TimerService timer;

    public Timer() {
        super();
    }

    void addTask(Runnable task) {
        if (null == timer) {
            addWaiting(task);
        } else {
            start(task);
        }
    }

    void addOnceTask(Runnable task) {
        if (null == timer) {
            addFireOnce(task);
        } else {
            fireOnce(task);
        }
    }

    void configure(Configuration configuration) {
        interval = configuration.getLong(Property.CLEANER_INTERVAL, interval);
    }

    void removeTask(Runnable task) {
        if (isWaiting(task)) {
            removeWaiting(task);
        } else {
            stop(task);
        }
    }

    public void setTimerService(TimerService timer) {
        this.timer = timer;
        startAll();
    }

    public void removeTimerService() {
        stopAll();
        this.timer = null;
    }

    private void addWaiting(Runnable task) {
        waitingLock.lock();
        try {
            waiting.add(task);
        } finally {
            waitingLock.unlock();
        }
    }

    private void addFireOnce(Runnable task) {
        waitingLock.lock();
        try {
            onceWaiting.add(task);
        } finally {
            waitingLock.unlock();
        }
    }

    private boolean isWaiting(Runnable task) {
        waitingLock.lock();
        try {
            return waiting.contains(task);
        } finally {
            waitingLock.unlock();
        }
    }

    private boolean removeWaiting(Runnable task) {
        waitingLock.lock();
        try {
            return waiting.remove(task);
        } finally {
            waitingLock.unlock();
        }
    }

    private Runnable getFirstWaiting() {
        waitingLock.lock();
        try {
            return waiting.isEmpty() ? null : waiting.remove(0);
        } finally {
            waitingLock.unlock();
        }
    }

    private Runnable getFireOnce() {
        waitingLock.lock();
        try {
            return onceWaiting.isEmpty() ? null : onceWaiting.remove(0);
        } finally {
            waitingLock.unlock();
        }
    }

    private void start(Runnable task) {
        ScheduledTimerTask scheduled = timer.scheduleAtFixedRate(task, interval, interval);
        final ScheduledTimerTask alreadyRunning;
        runningLock.lock();
        try {
            alreadyRunning = running.put(task, scheduled);
        } finally {
            runningLock.unlock();
        }
        if (alreadyRunning != null) {
            LOG.error("Duplicate start of cleaner task.");
            if (!alreadyRunning.cancel()) {
                LOG.error("Can not stop already running cleaner task.");
            }
        }
    }

    private void fireOnce(Runnable task) {
        timer.schedule(task, 0);
    }

    private void stop(Runnable task) {
        final ScheduledTimerTask scheduled;
        runningLock.lock();
        try {
            scheduled = running.remove(task);
        } finally {
            runningLock.unlock();
        }
        if (null == scheduled) {
            LOG.error("Unknown task to remove.");
            return;
        }
        if (!scheduled.cancel()) {
            LOG.error("Can not stop running cleaner task.");
        }
    }

    private Runnable stopSomeRunning() {
        final Runnable task;
        final ScheduledTimerTask scheduled;
        runningLock.lock();
        try {
            Iterator<Entry<Runnable, ScheduledTimerTask>> iter = running.entrySet().iterator();
            if (iter.hasNext()) {
                Entry<Runnable, ScheduledTimerTask> entry = iter.next();
                task = entry.getKey();
                scheduled = entry.getValue();
                iter.remove();
            } else {
                task = null;
                scheduled = null;
            }
        } finally {
            runningLock.unlock();
        }
        if (null == scheduled) {
            return null;
        }
        if (!scheduled.cancel()) {
            LOG.error("Can not stop running cleaner task.");
        }
        return task;
    }

    private void startAll() {
        Runnable task;
        while ((task = getFirstWaiting()) != null) {
            start(task);
        }
        while (null != (task = getFireOnce())) {
            fireOnce(task);
        }
    }

    private void stopAll() {
        Runnable task;
        while ((task = stopSomeRunning()) != null) {
            addWaiting(task);
        }
    }
}
