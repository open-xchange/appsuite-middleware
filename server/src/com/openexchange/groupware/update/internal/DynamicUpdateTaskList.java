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

package com.openexchange.groupware.update.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.UpdateTaskCollection;

/**
 * {@link DynamicUpdateTaskList} - Registry for {@link UpdateTask update tasks}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DynamicUpdateTaskList implements UpdateTaskList {

    private static final Log LOG = LogFactory.getLog(DynamicUpdateTaskList.class);

    private static final DynamicUpdateTaskList SINGLETON = new DynamicUpdateTaskList();

    private final ConcurrentMap<Class<? extends UpdateTask>, UpdateTask> taskList = new ConcurrentHashMap<Class<? extends UpdateTask>, UpdateTask>();

    public DynamicUpdateTaskList() {
        super();
    }

    public static DynamicUpdateTaskList getInstance() {
        return SINGLETON;
    }

    public boolean addUpdateTask(UpdateTask updateTask) {
        final boolean added = (null == taskList.putIfAbsent(updateTask.getClass(), updateTask));
        if (added && !UpdateTaskCollection.addDiscoveredUpdateTask(updateTask)) {
            LOG.info("Update task \"" + updateTask.getClass().getName() + "\" could not be added either due to static update task setup or because update process has already ran.");
        }
        return added;
    }

    /**
     * Removes specified update task from this registry.
     * 
     * @param updateTask The update task
     */
    public void removeUpdateTask(final UpdateTask updateTask) {
        final UpdateTask removed = taskList.remove(updateTask.getClass());
        if (null != removed) {
            UpdateTaskCollection.removeDiscoveredUpdateTask(removed);
        }
    }

    /**
     * Clears this registry.
     */
    public void clear() {
        taskList.clear();
    }

    /**
     * Gets this registry's update tasks.
     * 
     * @return This registry's update tasks
     */
    public Iterator<UpdateTask> getUpdateTasks() {
        return unmodifiableIterator(taskList.values().iterator());
    }

    /**
     * Returns a {@link Set set} view if this registry's update tasks.
     * 
     * @return A {@link Set set} containing this registry's update tasks.
     */
    public Set<UpdateTask> asSet() {
        if (taskList.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<UpdateTask>(taskList.values());
    }

    public List<UpdateTask> getTaskList() {
        List<UpdateTask> retval = new ArrayList<UpdateTask>(taskList.size());
        retval.addAll(taskList.values());
        return retval;
    }

    /**
     * Strips the <tt>remove()</tt> functionality from an existing iterator.
     * <p>
     * Wraps the supplied iterator into a new one that will always throw an <tt>UnsupportedOperationException</tt> if its <tt>remove()</tt>
     * method is called.
     * 
     * @param iterator The iterator to turn into an unmodifiable iterator.
     * @return An iterator with no remove functionality.
     */
    private static <T> Iterator<T> unmodifiableIterator(final Iterator<T> iterator) {
        if (iterator == null) {
            throw new NullPointerException();
        }

        return new Iterator<T>() {

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public T next() {
                return iterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
