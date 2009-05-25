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

package com.openexchange.groupware.update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link UpdateTaskCollection} - Collection for update tasks.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UpdateTaskCollection {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(UpdateTaskCollection.class);

    private UpdateTaskCollection() {
        super();
    }

    private static final AtomicInteger version = new AtomicInteger(-1);

    private static List<UpdateTask> staticUpdateTaskList;

    private static volatile BlockingQueue<UpdateTask> updateTaskQueue;

    /**
     * Sets statically loaded update tasks and initializes working queue.
     * 
     * @param updateTaskList The statically loaded update tasks
     */
    static void setUpdateTaskList(final List<UpdateTask> updateTaskList) {
        UpdateTaskCollection.staticUpdateTaskList = updateTaskList;
        updateTaskQueue = new LinkedBlockingQueue<UpdateTask>();
        // Initially prefill working queue
        updateTaskQueue.addAll(staticUpdateTaskList);
    }

    /**
     * Drops statically loaded update tasks and working queue as well.
     */
    static void dropUpdateTaskList() {
        UpdateTaskCollection.staticUpdateTaskList = null;
        updateTaskQueue = null;
    }

    /**
     * Adds specified update task to initial working queue.
     * 
     * @param updateTask The update task to add
     * @return <code>true</code> if update task was successfully added to initial working queue; otherwise <code>false</code>
     */
    static boolean addLookedUpUpdateTask(final UpdateTask updateTask) {
        final BlockingQueue<UpdateTask> queue = updateTaskQueue;
        if (null == queue) {
            return false;
        }
        return queue.offer(updateTask);
    }

    /**
     * Removes specified update task from initial working queue.
     * 
     * @param updateTask The update task to remove
     */
    static void removeLookedUpUpdateTask(final UpdateTask updateTask) {
        final BlockingQueue<UpdateTask> queue = updateTaskQueue;
        if (null == queue || queue.isEmpty()) {
            return;
        }
        queue.remove(updateTask);
    }

    /**
     * Creates a list of <code>UpdateTask</code> instances that apply to current database version
     * 
     * @param dbVersion - current database version
     * @return list of <code>UpdateTask</code> instances
     */
    public static final List<UpdateTask> getFilteredAndSortedUpdateTasks(final int dbVersion) {
        final List<UpdateTask> retval = generateList();
        /*
         * Filter
         */
        final int size = retval.size();
        final Iterator<UpdateTask> iter = retval.iterator();
        for (int i = 0; i < size; i++) {
            final UpdateTask ut = iter.next();
            if (ut.addedWithVersion() <= dbVersion) {
                iter.remove();
            }
        }
        /*
         * Sort
         */
        Collections.sort(retval, UPDATE_TASK_COMPARATOR);
        return retval;
    }

    /**
     * Iterates all implementations of <code>UpdateTask</code> and determines the highest version number indicated through method
     * <code>UpdateTask.addedWithVersion()</code>.
     * 
     * @return the highest version number
     */
    public static final int getHighestVersion() {
        if (version.get() == -1) {
            synchronized (version) {
                /*
                 * Check again
                 */
                if (version.get() == -1) {
                    final List<UpdateTask> tasks = generateList();
                    final int size = tasks.size();
                    final Iterator<UpdateTask> iter = tasks.iterator();
                    int vers = 0;
                    for (int i = 0; i < size; i++) {
                        vers = Math.max(vers, iter.next().addedWithVersion());
                    }
                    version.set(vers);
                }
            }
        }
        return version.get();
    }

    private static List<UpdateTask> generateList() {
        BlockingQueue<UpdateTask> queue = updateTaskQueue;
        if (null != queue) {
            synchronized (UpdateTaskCollection.class) {
                queue = updateTaskQueue;
                if (null != queue) {
                    UpdateTask task = queue.poll();
                    /*
                     * First touch, wait for proper initialization
                     */
                    final List<UpdateTask> retval = new ArrayList<UpdateTask>();
                    try {
                        do {
                            retval.add(task);
                            // Wait for 2sec for possibly looked-up update task
                        } while ((task = queue.poll(2000L, TimeUnit.MILLISECONDS)) != null);
                        // The specified waiting time elapses before an element was present
                    } catch (final InterruptedException e) {
                        // Keep interrupted status
                        LOG.error(e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    } finally {
                        updateTaskQueue = null;
                    }
                    return retval;
                }
            }
        }
        /*
         * Working queue already processed
         */
        final List<UpdateTask> retval = new ArrayList<UpdateTask>(staticUpdateTaskList);
        retval.addAll(UpdateTaskRegistry.getInstance().asSet());
        return retval;
    }

    /**
     * Sorts instances of <code>UpdateTask</code> by their version in first order and by their priority in second order
     */
    private static final Comparator<UpdateTask> UPDATE_TASK_COMPARATOR = new Comparator<UpdateTask>() {

        public int compare(final UpdateTask o1, final UpdateTask o2) {
            if (o1.addedWithVersion() > o2.addedWithVersion()) {
                return 1;
            } else if (o1.addedWithVersion() < o2.addedWithVersion()) {
                return -1;
            } else if (o1.getPriority() > o2.getPriority()) {
                return 1;
            } else if (o1.getPriority() < o2.getPriority()) {
                return -1;
            }
            return 0;
        }
    };

}
