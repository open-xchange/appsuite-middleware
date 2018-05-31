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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableList;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.NamespaceAwareUpdateTask;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.SeparatedTasks;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.java.Strings;

/**
 * {@link UpdateTaskCollection} - Collection for update tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
class UpdateTaskCollection {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateTaskCollection.class);

    private static final UpdateTaskCollection SINGLETON = new UpdateTaskCollection();

    /**
     * Returns the instance of the {@link UpdateTaskCollection}
     *
     * @return the instance of the {@link UpdateTaskCollection}
     */
    static UpdateTaskCollection getInstance() {
        return SINGLETON;
    }

    // ----------------------------------------------------------------------------------------------------------

    private final AtomicReference<List<UpdateTaskV2>> effectiveTasksRef = new AtomicReference<List<UpdateTaskV2>>(null);

    /**
     * Initialises a new {@link UpdateTaskCollection}.
     */
    private UpdateTaskCollection() {
        super();
    }

    /**
     * Drops statically loaded update tasks and working queue as well.
     */
    void dispose() {
        effectiveTasksRef.set(null);
    }

    /**
     * Filters the update tasks that must be executed
     *
     * @param state The {@link SchemaUpdateState}
     * @return The filtered {@link SeparatedTasks}
     */
    SeparatedTasks getFilteredAndSeparatedTasks(SchemaUpdateState state) {
        return separateTasks(getFilteredUpdateTasks(state));
    }

    /**
     * Separates the {@link UpdateTaskV2} tasks in {@link UpdateConcurrency#BLOCKING}
     * and {@link UpdateConcurrency#BACKGROUND}
     *
     * @param tasks The {@link List} of {@link UpdateTaskV2} tasks
     * @return The {@link SeparatedTasks}
     */
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

    /**
     * Returns a {@link List} with all {@link UpdateTaskV2} tasks filtered and sorted with the {@link UpdateConcurrency#BLOCKING}
     * tasks prior any {@link UpdateConcurrency#BACKGROUND} tasks (controlled by the <code>blocking</code> argument
     *
     * @param schema The {@link SchemaUpdateState}
     * @param blocking Whether the {@link UpdateConcurrency#BLOCKING} tasks will be first on the returned {@link List}
     * @return A {@link List} with the filtered and sorted {@link UpdateTaskV2} tasks
     * @throws OXException if there is no preference in executing the blocking tasks first, but there are blocking tasks to be executed
     */
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

    /**
     * Returns a {@link List} with all the {@link UpdateTaskV2} tasks
     * without the excluded ones.
     *
     * @return a {@link List} with all {@link UpdateTaskV2} without the excluded ones
     */
    List<UpdateTaskV2> getListWithoutExcludes() {
        List<UpdateTaskV2> effectiveTasks = effectiveTasksRef.get();
        if (null == effectiveTasks) {
            synchronized (this) {
                effectiveTasks = effectiveTasksRef.get();
                if (null == effectiveTasks) {
                    Set<UpdateTaskV2> fullSet = DynamicSet.getInstance().getTaskSet();

                    Set<String> tasksToExclude = ExcludedSet.getInstance().getTaskSet();
                    boolean hasTasksToExclude = !tasksToExclude.isEmpty();
                    Set<String> namespacesToExclude = NamespaceAwareExcludedSet.getInstance().getTaskSet();
                    boolean hasNamespacesToExclude = !namespacesToExclude.isEmpty();

                    if (hasTasksToExclude) {
                        if (hasNamespacesToExclude) {
                            for (Iterator<UpdateTaskV2> it = fullSet.iterator(); it.hasNext(); ) {
                                Class<? extends UpdateTaskV2> clazz = it.next().getClass();
                                if (tasksToExclude.contains(clazz.getName())) {
                                    // Excluded by task name
                                    it.remove();
                                } else {
                                    NamespaceAwareUpdateTask annotation = clazz.getAnnotation(NamespaceAwareUpdateTask.class);
                                    if (annotation != null && namespacesToExclude.contains(annotation.namespace())) {
                                        // Excluded by namespace
                                        it.remove();
                                    }
                                }
                            }
                        } else {
                            for (Iterator<UpdateTaskV2> it = fullSet.iterator(); it.hasNext(); ) {
                                Class<? extends UpdateTaskV2> clazz = it.next().getClass();
                                if (tasksToExclude.contains(clazz.getName())) {
                                    // Excluded by task name
                                    it.remove();
                                }
                            }
                        }
                    } else {
                        if (hasNamespacesToExclude) {
                            for (Iterator<UpdateTaskV2> it = fullSet.iterator(); it.hasNext(); ) {
                                Class<? extends UpdateTaskV2> clazz = it.next().getClass();
                                NamespaceAwareUpdateTask annotation = clazz.getAnnotation(NamespaceAwareUpdateTask.class);
                                if (annotation != null && namespacesToExclude.contains(annotation.namespace())) {
                                    // Excluded by namespace
                                    it.remove();
                                }
                            }
                        }
                    }
                    effectiveTasks = ImmutableList.copyOf(fullSet);
                    effectiveTasksRef.set(effectiveTasks);
                }
            }
        }
        return effectiveTasks;
    }

    /**
     * Marks it as dirty
     */
    void dirtyVersion() {
        effectiveTasksRef.set(null);
    }

    /**
     * Checks whether there are any pending update tasks
     *
     * @param state The {@link SchemaUpdateState}
     * @return <code>true</code> if at least one task is pending for execution; <code>false</code> otherwise
     */
    boolean needsUpdate(SchemaUpdateState state) {
        for (UpdateTaskV2 task : getListWithoutExcludes()) {
            if (!state.isExecuted(task.getClass().getName())) {
                return true;
            }
        }
        return false;
    }

    /////////////////////////////////// HELPERS ////////////////////////////////////

    /**
     * Returns a {@link List} with all tasks that must be executed
     *
     * @param schema The {@link SchemaUpdateState}
     * @return A {@link List} with all must-executed update tasks
     */
    private List<UpdateTaskV2> getFilteredUpdateTasks(SchemaUpdateState schema) {
        List<UpdateTaskV2> tasks = getListWithoutExcludes();
        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }

        // Filter
        Filter filter = new ExecutedFilter();
        List<UpdateTaskV2> filtered = null;
        for (UpdateTaskV2 task : tasks) {
            if (filter.mustBeExecuted(schema, task)) {
                if (null == filtered) {
                    filtered = new ArrayList<UpdateTaskV2>(tasks.size());
                }
                filtered.add(task);
            }
        }
        return null == filtered ? Collections.emptyList() : filtered;
    }

}
