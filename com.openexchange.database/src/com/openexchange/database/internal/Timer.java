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

    /**
     * Gets the timer
     *
     * @return The timer
     */
    public TimerService getTimer() {
        return timer;
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
