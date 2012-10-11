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

package com.openexchange.groupware.update.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.SeparatedTasks;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.java.Strings;

/**
 * {@link UpdateTaskCollection} - Collection for update tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class UpdateTaskCollection {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(UpdateTaskCollection.class));

    private static final UpdateTaskCollection SINGLETON = new UpdateTaskCollection();

    private int version;

    private final AtomicBoolean versionDirty = new AtomicBoolean(true);

    private UpdateTaskCollection() {
        super();
    }

    static UpdateTaskCollection getInstance() {
        return SINGLETON;
    }

    /**
     * Drops statically loaded update tasks and working queue as well.
     */
    void dispose() {
        versionDirty.set(true);
    }

    private final List<UpdateTask> getFilteredUpdateTasks(SchemaUpdateState schema) {
        List<UpdateTask> tasks = getListWithoutExcludes();
        // Simulate executed list based on schema version if necessary.
        final SchemaUpdateState state = addExecutedBasedOnVersion(schema, tasks);
        // Filter
        Filter filter = new ExecutedFilter();
        List<UpdateTask> filtered = new ArrayList<UpdateTask>();
        for (UpdateTask task : tasks) {
            if (filter.mustBeExecuted(state, task)) {
                filtered.add(task);
            }
        }
        return filtered;
    }

    SeparatedTasks getFilteredAndSeparatedTasks(SchemaUpdateState state) {
        return separateTasks(getFilteredUpdateTasks(state));
    }

    SeparatedTasks separateTasks(List<UpdateTask> tasks) {
        final List<UpdateTask> blocking = new ArrayList<UpdateTask>();
        final List<UpdateTaskV2> background = new ArrayList<UpdateTaskV2>();
        for (UpdateTask toExecute : tasks) {
            if (toExecute instanceof UpdateTaskV2) {
                UpdateTaskV2 toExecuteV2 = (UpdateTaskV2) toExecute;
                switch (toExecuteV2.getAttributes().getConcurrency()) {
                case BLOCKING:
                    blocking.add(toExecuteV2);
                    break;
                case BACKGROUND:
                    background.add(toExecuteV2);
                    break;
                default:
                    OXException e = UpdateExceptionCodes.UNKNOWN_CONCURRENCY.create(toExecuteV2.getClass().getName());
                    LOG.error(e.getMessage(), e);
                    blocking.add(toExecuteV2);
                }
            } else {
                blocking.add(toExecute);
            }
        }
        return new SeparatedTasks() {
            @Override
            public List<UpdateTask> getBlocking() {
                return blocking;
            }
            @Override
            public List<UpdateTaskV2> getBackground() {
                return background;
            }
        };
    }

    final List<UpdateTask> getFilteredAndSortedUpdateTasks(SchemaUpdateState schema, boolean blocking) throws OXException {
        SeparatedTasks tasks = getFilteredAndSeparatedTasks(schema);
        List<UpdateTask> retval = new ArrayList<UpdateTask>();
        if (blocking) {
            retval.addAll(tasks.getBlocking());
        } else {
            if (tasks.getBlocking().size() > 0) {
                throw UpdateExceptionCodes.BLOCKING_FIRST.create(Strings.join(tasks.getBlocking(), ","), Strings.join(tasks.getBackground(), ","));
            }
            retval.addAll(tasks.getBackground());
        }
        final SchemaUpdateState simulatedState = addExecutedBasedOnVersion(schema, getListWithoutExcludes());
        // And sort them. Sorting this way prerequisites that every blocking task can be executed before any background task is scheduled.
        // Said in other words: Blocking tasks can not depend on background tasks.
        retval = new UpdateTaskSorter().sort(simulatedState.getExecutedList(), retval);
        return retval;
    }

    private SchemaUpdateState addExecutedBasedOnVersion(SchemaUpdateState schema, List<UpdateTask> tasks) {
        final SchemaUpdateState retval;
        if (Schema.FINAL_VERSION != schema.getDBVersion() && Schema.NO_VERSION != schema.getDBVersion()) {
            retval = new SchemaUpdateStateImpl(schema);
            Filter filter = new VersionFilter();
            for (UpdateTask task : tasks) {
                if (!filter.mustBeExecuted(schema, task)) {
                    retval.addExecutedTask(task.getClass().getName());
                }
            }
        } else {
            retval = schema;
        }
        return retval;
    }

    /**
     * Iterates all implementations of <code>UpdateTask</code> and determines the highest version number indicated through method
     * <code>UpdateTask.addedWithVersion()</code>.
     *
     * @return The highest version number
     */
    final int getHighestVersion() {
        if (versionDirty.get()) {
            List<UpdateTask> tasks = getListWithoutExcludes();
            int vers = 0;
            for (UpdateTask task : tasks) {
                vers = Math.max(vers, task.addedWithVersion());
            }
            version = vers;
            versionDirty.set(true);
        }
        return version;
    }

    List<UpdateTask> getListWithoutExcludes() {
        List<UpdateTask> retval = getFullList();
        for (String excluded : ExcludedList.getInstance().getTaskList()) {
            // Matching must be done based on task class name.
            Iterator<UpdateTask> iter = retval.iterator();
            while (iter.hasNext()) {
                if (excluded.equals(iter.next().getClass().getName())) {
                    iter.remove();
                }
            }
        }
        return retval;
    }

    private List<UpdateTask> getFullList() {
        return DynamicList.getInstance().getTaskList();
    }

    void dirtyVersion() {
        versionDirty.set(true);
    }

    boolean needsUpdate(SchemaUpdateState state) {
        if (getHighestVersion() > state.getDBVersion()) {
            return true;
        }
        List<UpdateTask> tasks = getListWithoutExcludes();
        for (UpdateTask task : tasks) {
            if (!state.isExecuted(task.getClass().getName())) {
                return true;
            }
        }
        return false;
    }
}
