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

package com.openexchange.groupware.update.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.SeparatedTasks;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.java.Strings;

/**
 * {@link UpdateTaskCollection} - Collection for update tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class UpdateTaskCollection {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateTaskCollection.class);

    private static final UpdateTaskCollection SINGLETON = new UpdateTaskCollection();

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

    private final List<UpdateTaskV2> getFilteredUpdateTasks(SchemaUpdateState schema) {
        List<UpdateTaskV2> tasks = getListWithoutExcludes();
        // Filter
        Filter filter = new ExecutedFilter();
        List<UpdateTaskV2> filtered = new ArrayList<UpdateTaskV2>();
        for (UpdateTaskV2 task : tasks) {
            if (filter.mustBeExecuted(schema, task)) {
                filtered.add(task);
            }
        }
        return filtered;
    }

    SeparatedTasks getFilteredAndSeparatedTasks(SchemaUpdateState state) {
        return separateTasks(getFilteredUpdateTasks(state));
    }

    SeparatedTasks separateTasks(List<UpdateTaskV2> tasks) {
        final List<UpdateTaskV2> blocking = new ArrayList<UpdateTaskV2>();
        final List<UpdateTaskV2> background = new ArrayList<UpdateTaskV2>();
        for (UpdateTaskV2 toExecute : tasks) {
            switch (toExecute.getAttributes().getConcurrency()) {
                case BLOCKING:
                    blocking.add(toExecute);
                    break;
                case BACKGROUND:
                    background.add(toExecute);
                    break;
                default:
                    OXException e = UpdateExceptionCodes.UNKNOWN_CONCURRENCY.create(toExecute.getClass().getName());
                    LOG.error("", e);
                    blocking.add(toExecute);
            }
        }
        return new SeparatedTasks() {
            @Override
            public List<UpdateTaskV2> getBlocking() {
                return blocking;
            }
            @Override
            public List<UpdateTaskV2> getBackground() {
                return background;
            }
        };
    }

    final List<UpdateTaskV2> getFilteredAndSortedUpdateTasks(SchemaUpdateState schema, boolean blocking) throws OXException {
        SeparatedTasks tasks = getFilteredAndSeparatedTasks(schema);
        List<UpdateTaskV2> retval = new ArrayList<UpdateTaskV2>();
        if (blocking) {
            retval.addAll(tasks.getBlocking());
        } else {
            if (tasks.getBlocking().size() > 0) {
                throw UpdateExceptionCodes.BLOCKING_FIRST.create(Strings.join(tasks.getBlocking(), ","), Strings.join(tasks.getBackground(), ","));
            }
            retval.addAll(tasks.getBackground());
        }
        // And sort them. Sorting this way prerequisites that every blocking task can be executed before any background task is scheduled.
        // Said in other words: Blocking tasks can not depend on background tasks.
        retval = new UpdateTaskSorter().sort(schema.getExecutedList(), retval);
        return retval;
    }

    List<UpdateTaskV2> getListWithoutExcludes() {
        List<UpdateTaskV2> retval = getFullList();
        for (String excluded : ExcludedList.getInstance().getTaskList()) {
            // Matching must be done based on task class name.
            Iterator<UpdateTaskV2> iter = retval.iterator();
            while (iter.hasNext()) {
                if (excluded.equals(iter.next().getClass().getName())) {
                    iter.remove();
                }
            }
        }
        return retval;
    }

    private List<UpdateTaskV2> getFullList() {
        return DynamicList.getInstance().getTaskList();
    }

    void dirtyVersion() {
        versionDirty.set(true);
    }

    boolean needsUpdate(SchemaUpdateState state) {
        List<UpdateTaskV2> tasks = getListWithoutExcludes();
        for (UpdateTaskV2 task : tasks) {
            if (!state.isExecuted(task.getClass().getName())) {
                return true;
            }
        }
        return false;
    }
}
