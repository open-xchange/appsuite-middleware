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

package com.openexchange.ajax;

import java.io.File;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;

/**
 * {@link DeleteOnExitFileCleaningTracker} - Overrides {@code FileCleaningTracker#track(File, Object, FileDeleteStrategy)} and invokes
 * {@code File#deleteOnExit()} on each tracked file.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class DeleteOnExitFileCleaningTracker extends FileCleaningTracker {

    private static final Object PRESENCE = new Object();

    /**
     * Queue of <code>Tracker</code> instances being watched.
     */
    final ReferenceQueue<Object> q = new ReferenceQueue<Object>();

    /**
     * Collection of <code>Tracker</code> instances in existence.
     */
    final Map<Tracker, Object> trackers = new ConcurrentHashMap<Tracker, Object>();

    /**
     * Collection of File paths that failed to delete.
     */
    final List<String> deleteFailures = new CopyOnWriteArrayList<String>();

    /**
     * Whether to terminate the thread when the tracking is complete.
     */
    volatile boolean exitWhenFinished = false;

    /**
     * The thread that will clean up registered files.
     */
    private Thread reaper;

    /**
     * The delete-on-exit flag.
     */
    private final boolean deleteOnExit;

    /**
     * The shut-down hook thread.
     */
    private volatile Thread shutdownHookThread;

    /**
     * Initializes a new {@link DeleteOnExitFileCleaningTracker}.
     */
    protected DeleteOnExitFileCleaningTracker(final boolean deleteOnExit) {
        super();
        this.deleteOnExit = deleteOnExit;

        final Thread shutdownHookThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                deleteAllTracked0();
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHookThread);
        this.shutdownHookThread = shutdownHookThread;
    }

    //-----------------------------------------------------------------------

    @Override
    public void track(final File file, final Object marker, final FileDeleteStrategy deleteStrategy) {
        super.track(file, marker, deleteStrategy);
        if (deleteOnExit) {
            file.deleteOnExit();
        }
    }

    /**
     * Manually deletes tracked files.
     */
    public void deleteAllTracked() {
        deleteAllTracked0();

        final Thread shutdownHookThread = this.shutdownHookThread;
        if (!shutdownHookThread.isAlive()) {
            // Remove shutdown hook if not running. Otherwise stop() is invoked by the thread itself.
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
            } catch (final IllegalStateException e) {
                // Ignore
            }
        }
        this.shutdownHookThread = null;
    }

    void deleteAllTracked0() {
        // Poll trackers for removal.
        for (Tracker tracker = (Tracker) q.poll(); tracker != null; tracker = (Tracker) q.poll()) {
            trackers.remove(tracker);
            if (!tracker.delete()) {
                deleteFailures.add(tracker.getPath());
            }
            tracker.clear();
        }
        exitWhenFinished();
    }

    /**
     * Track the specified file, using the provided marker, deleting the file
     * when the marker instance is garbage collected.
     * The specified deletion strategy is used.
     *
     * @param path  the full path to the file to be tracked, not null
     * @param marker  the marker object used to track the file, not null
     * @param deleteStrategy  the strategy to delete the file, null means normal
     * @throws NullPointerException if the path is null
     */
    @Override
    public void track(final String path, final Object marker, final FileDeleteStrategy deleteStrategy) {
        if (path == null) {
            throw new NullPointerException("The path must not be null");
        }
        addTracker(path, marker, deleteStrategy);
    }

    /**
     * Adds a tracker to the list of trackers.
     * 
     * @param path  the full path to the file to be tracked, not null
     * @param marker  the marker object used to track the file, not null
     * @param deleteStrategy  the strategy to delete the file, null means normal
     */
    private synchronized void addTracker(final String path, final Object marker, final FileDeleteStrategy deleteStrategy) {
        // synchronized block protects reaper
        if (exitWhenFinished) {
            throw new IllegalStateException("No new trackers can be added once exitWhenFinished() is called");
        }
        if (reaper == null) {
            reaper = new Reaper();
            reaper.start();
        }
        trackers.put(new Tracker(path, deleteStrategy, marker, q), PRESENCE);
    }

    //-----------------------------------------------------------------------
    /**
     * Retrieve the number of files currently being tracked, and therefore
     * awaiting deletion.
     *
     * @return the number of files being tracked
     */
    @Override
    public int getTrackCount() {
        return trackers.size();
    }

    /**
     * Return the file paths that failed to delete.
     *
     * @return the file paths that failed to delete
     */
    @Override
    public List<String> getDeleteFailures() {
        return deleteFailures;
    }

    /**
     * Call this method to cause the file cleaner thread to terminate when
     * there are no more objects being tracked for deletion.
     * <p>
     * In a simple environment, you don't need this method as the file cleaner
     * thread will simply exit when the JVM exits. In a more complex environment,
     * with multiple class loaders (such as an application server), you should be
     * aware that the file cleaner thread will continue running even if the class
     * loader it was started from terminates. This can consitute a memory leak.
     * <p>
     * For example, suppose that you have developed a web application, which
     * contains the commons-io jar file in your WEB-INF/lib directory. In other
     * words, the FileCleaner class is loaded through the class loader of your
     * web application. If the web application is terminated, but the servlet
     * container is still running, then the file cleaner thread will still exist,
     * posing a memory leak.
     * <p>
     * This method allows the thread to be terminated. Simply call this method
     * in the resource cleanup code, such as {@link javax.servlet.ServletContextListener#contextDestroyed}.
     * Once called, no new objects can be tracked by the file cleaner.
     */
    @Override
    public synchronized void exitWhenFinished() {
        // synchronized block protects reaper
        exitWhenFinished = true;
        if (reaper != null) {
            synchronized (reaper) {
                reaper.interrupt();
            }
        }
    }

    //-----------------------------------------------------------------------
    /**
     * The reaper thread.
     */
    private final class Reaper extends Thread {
        /** Construct a new Reaper */
        Reaper() {
            super("File Reaper");
            setPriority(Thread.MAX_PRIORITY);
            setDaemon(true);
        }

        /**
         * Run the reaper thread that will delete files as their associated
         * marker objects are reclaimed by the garbage collector.
         */
        @Override
        public void run() {
            // thread exits when exitWhenFinished is true and there are no more tracked objects
            while (!exitWhenFinished || trackers.size() > 0) {
                try {
                    // Wait for a tracker to remove.
                    final Tracker tracker = (Tracker) q.remove(); // cannot return null
                    trackers.remove(tracker);
                    if (!tracker.delete()) {
                        deleteFailures.add(tracker.getPath());
                    }
                    tracker.clear();
                } catch (final InterruptedException e) {
                    continue;
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Inner class which acts as the reference for a file pending deletion.
     */
    private static final class Tracker extends PhantomReference<Object> {

        /**
         * The full path to the file being tracked.
         */
        private final String path;
        /**
         * The strategy for deleting files.
         */
        private final FileDeleteStrategy deleteStrategy;

        /**
         * Constructs an instance of this class from the supplied parameters.
         *
         * @param path  the full path to the file to be tracked, not null
         * @param deleteStrategy  the strategy to delete the file, null means normal
         * @param marker  the marker object used to track the file, not null
         * @param queue  the queue on to which the tracker will be pushed, not null
         */
        Tracker(final String path, final FileDeleteStrategy deleteStrategy, final Object marker, final ReferenceQueue<? super Object> queue) {
            super(marker, queue);
            this.path = path;
            this.deleteStrategy = deleteStrategy == null ? FileDeleteStrategy.NORMAL : deleteStrategy;
        }

        /**
         * Return the path.
         *
         * @return the path
         */
        public String getPath() {
            return path;
        }

        /**
         * Deletes the file associated with this tracker instance.
         *
         * @return {@code true} if the file was deleted successfully;
         *         {@code false} otherwise.
         */
        public boolean delete() {
            return deleteStrategy.deleteQuietly(new File(path));
        }
    }

}
