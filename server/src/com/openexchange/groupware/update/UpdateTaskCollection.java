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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.groupware.update.internal.DynamicList;
import com.openexchange.groupware.update.internal.ExcludedList;
import com.openexchange.groupware.update.internal.ExecutedFilter;
import com.openexchange.groupware.update.internal.Filter;
import com.openexchange.groupware.update.internal.SchemaUpdateStateImpl;
import com.openexchange.groupware.update.internal.UpdateTaskSorter;
import com.openexchange.groupware.update.internal.VersionFilter;

/**
 * {@link UpdateTaskCollection} - Collection for update tasks.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UpdateTaskCollection {

    private static final UpdateTaskCollection SINGLETON = new UpdateTaskCollection();

    private int version;

    private AtomicBoolean versionDirty = new AtomicBoolean(true);

    private UpdateTaskCollection() {
        super();
    }

    public static UpdateTaskCollection getInstance() {
        return SINGLETON;
    }

    /**
     * Drops statically loaded update tasks and working queue as well.
     */
    void dispose() {
        versionDirty.set(true);
    }

    /**
     * Creates a list of <code>UpdateTask</code> instances that apply to current database version
     * 
     * @param schema - current database version
     * @return list of <code>UpdateTask</code> instances
     * @throws UpdateException if the order for the update tasks to execute can not be determined.
     */
    public final List<UpdateTask> getFilteredAndSortedUpdateTasks(SchemaUpdateState schema) throws UpdateException {
        List<UpdateTask> retval = generateList();
        SchemaUpdateState state = schema;
        // Calculate version to executed list.
        if (Schema.FINAL_VERSION != schema.getDBVersion() && Schema.NO_VERSION != schema.getDBVersion()) {
            state = addExecutedBasedOnVersion(schema, retval);
        }
        // Filter
        Filter filter = new ExecutedFilter();
        Iterator<UpdateTask> iter = retval.iterator();
        while (iter.hasNext()) {
            UpdateTask task = iter.next();
            if (!filter.mustBeExecuted(state, task)) {
                iter.remove();
            }
        }
        // And sort them
        retval = new UpdateTaskSorter().sort(state.getExecutedList(), retval);
        return retval;
    }

    private SchemaUpdateState addExecutedBasedOnVersion(SchemaUpdateState schema, List<UpdateTask> tasks) {
        SchemaUpdateState retval = new SchemaUpdateStateImpl(schema);
        Filter filter = new VersionFilter();
        for (UpdateTask task : tasks) {
            if (!filter.mustBeExecuted(schema, task)) {
                retval.addExecutedTask(task.getClass().getName());
            }
        }
        return retval;
    }

    /**
     * Iterates all implementations of <code>UpdateTask</code> and determines the highest version number indicated through method
     * <code>UpdateTask.addedWithVersion()</code>.
     * 
     * @return The highest version number
     */
    public final int getHighestVersion() {
        if (versionDirty.get()) {
            List<UpdateTask> tasks = generateList();
            int vers = 0;
            for (UpdateTask task : tasks) {
                vers = Math.max(vers, task.addedWithVersion());
            }
            version = vers;
            versionDirty.set(true);
        }
        return version;
    }

    public final List<UpdateTask> generateList() {
        List<UpdateTask> retval = new ArrayList<UpdateTask>();
        retval.addAll(DynamicList.getInstance().getTaskList());
        for (UpdateTask excluded : ExcludedList.getInstance().getTaskList()) {
            // Matching must be done based on task class name.
            String toExclude = excluded.getClass().getName();
            Iterator<UpdateTask> iter = retval.iterator();
            while (iter.hasNext()) {
                if (toExclude.equals(iter.next().getClass().getName())) {
                    iter.remove();
                }
            }
        }
        return retval;
    }

    public void dirtyVersion() {
        versionDirty.set(true);
    }

    public boolean needsUpdate(SchemaUpdateState state) {
        if (getHighestVersion() > state.getDBVersion()) {
            return true;
        }
        List<UpdateTask> taskList = generateList();
        for (UpdateTask task : taskList) {
            if (!state.isExecuted(task.getClass().getName())) {
                return true;
            }
        }
        return false;
    }
}
