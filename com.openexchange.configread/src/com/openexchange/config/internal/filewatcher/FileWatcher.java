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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.config.internal.filewatcher;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;

/**
 * {@link FileWatcher}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileWatcher {

    static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FileWatcher.class));

    private final class FileWatcherTimerTask extends TimerTask {

        FileWatcherTimerTask() {
            super();
        }

        @Override
        public void run() {
            try {
                if (!file.exists()) {
                    notifyListeners(true);
                }
                final long newTimeStamp = file.lastModified();
                if (timeStamp != newTimeStamp) {
                    timeStamp = newTimeStamp;
                    notifyListeners(false);
                }
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private static final ConcurrentMap<File, FileWatcher> fileWatchers = new ConcurrentHashMap<File, FileWatcher>();

    private static volatile Timer fileWatcherTimer;

    /**
     * Gets a file watcher bound to given file. If no file watcher has been bound to specified file, yet, a new one is created and returned.
     *
     * @param file The file
     * @return The file watcher
     */
    public static FileWatcher getFileWatcher(final File file) {
        FileWatcher fw = fileWatchers.get(file);
        if (null == fw) {
            final FileWatcher newfw = new FileWatcher(file);
            fw = fileWatchers.putIfAbsent(file, newfw);
            if (null == fw) {
                fw = newfw;
            }
        }
        return fw;
    }

    private static void initTimer() {
        Timer timer = fileWatcherTimer;
        if (null == timer) {
            synchronized (FileWatcher.class) {
                timer = fileWatcherTimer;
                if (null == timer) {
                    timer = new Timer("FileWatcherTimer");
                    fileWatcherTimer = timer;
                }
            }
        }
    }

    /**
     * Drops the associated timer thread.
     */
    public static void dropTimer() {
        Timer timer = fileWatcherTimer;
        if (null != timer) {
            synchronized (FileWatcher.class) {
                timer = fileWatcherTimer;
                if (null != timer) {
                    timer.cancel();
                    fileWatcherTimer = null;
                }
            }
        }
    }

    final File file;

    private final ConcurrentMap<Class<? extends FileListener>, FileListener> listeners;

    private final AtomicBoolean started;

    private TimerTask timerTask;

    volatile long timeStamp;

    /**
     * Initializes a new file watcher
     *
     * @param file The file to watch
     */
    private FileWatcher(final File file) {
        super();
        started = new AtomicBoolean();
        listeners = new ConcurrentHashMap<Class<? extends FileListener>, FileListener>();
        this.file = file;
        timeStamp = file.lastModified();
    }

    /**
     * Adds an instance of {@link FileListener} to this watcher's listeners that is going to be notified on change or delete events.
     *
     * @param listener The listener to add
     */
    public void addFileListener(final FileListener listener) {
        listeners.putIfAbsent(listener.getClass(), listener);
    }

    void notifyListeners(final boolean onDelete) {
        if (onDelete) {
            for (final FileListener fileListener : listeners.values()) {
                fileListener.onDelete();
            }
        } else {
            for (final FileListener fileListener : listeners.values()) {
                fileListener.onChange(file);
            }
        }
    }

    /**
     * Start this file watcher
     *
     * @param period The time in milliseconds between successive file watcher executions.
     */
    public void startFileWatcher(final long period) {
        if (!started.get()) {
            synchronized (this) {
                if (!started.get()) {
                    timerTask = new FileWatcherTimerTask();
                    initTimer();
                    fileWatcherTimer.schedule(timerTask, 1000, period);
                    started.set(true);
                }
            }
        }
    }

    /**
     * Stop this file watcher
     */
    public void stopFileWatcher() {
        if (started.get()) {
            synchronized (this) {
                if (started.get()) {
                    timerTask.cancel();
                    fileWatcherTimer.purge();
                    started.set(false);
                }
            }
        }
    }

}
