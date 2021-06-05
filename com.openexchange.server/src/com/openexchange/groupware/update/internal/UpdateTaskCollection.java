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
        ImmutableList.Builder<UpdateTaskV2> blocking = null;
        ImmutableList.Builder<UpdateTaskV2> background = null;
        for (UpdateTaskV2 toExecute : tasks) {
            switch (toExecute.getAttributes().getConcurrency()) {
                case BLOCKING:
                    if (blocking == null) {
                        blocking = ImmutableList.builder();
                    }
                    blocking.add(toExecute);
                    break;
                case BACKGROUND:
                    if (background == null) {
                        background = ImmutableList.builder();
                    }
                    background.add(toExecute);
                    break;
                default:
                    OXException e = UpdateExceptionCodes.UNKNOWN_CONCURRENCY.create(toExecute.getClass().getName());
                    LOG.error("", e);
                    if (blocking == null) {
                        blocking = ImmutableList.builder();
                    }
                    blocking.add(toExecute);
            }
        }
        return new SeparatedTasksImpl(blocking == null ? Collections.emptyList() : blocking.build(), background == null ? Collections.emptyList() : background.build());
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
            if (tasks.hasBlocking()) {
                throw UpdateExceptionCodes.BLOCKING_FIRST.create(Strings.join(tasks.getBlocking(), ","), Strings.join(tasks.getBackground(), ","));
            }
            retval.addAll(tasks.getBackground());
        }
        // And sort them. Sorting this way prerequisites that every blocking task can be executed before any background task is scheduled.
        // Said in other words: Blocking tasks can not depend on background tasks.
        retval = new UpdateTaskSorter().sort(schema.getExecuted(), retval);
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
                            for (Iterator<UpdateTaskV2> it = fullSet.iterator(); it.hasNext();) {
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
                            for (Iterator<UpdateTaskV2> it = fullSet.iterator(); it.hasNext();) {
                                Class<? extends UpdateTaskV2> clazz = it.next().getClass();
                                if (tasksToExclude.contains(clazz.getName())) {
                                    // Excluded by task name
                                    it.remove();
                                }
                            }
                        }
                    } else {
                        if (hasNamespacesToExclude) {
                            for (Iterator<UpdateTaskV2> it = fullSet.iterator(); it.hasNext();) {
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

    private static class SeparatedTasksImpl implements SeparatedTasks {

        private final List<UpdateTaskV2> blocking;
        private final boolean hasBlocking;
        private final List<UpdateTaskV2> background;
        private final boolean hasBackground;

        SeparatedTasksImpl(List<UpdateTaskV2> blocking, List<UpdateTaskV2> background) {
            super();
            this.blocking = blocking;
            hasBlocking = !blocking.isEmpty();
            this.background = background;
            hasBackground = !background.isEmpty();
        }

        @Override
        public List<UpdateTaskV2> getBlocking() {
            return blocking;
        }

        @Override
        public List<UpdateTaskV2> getBackground() {
            return background;
        }

        @Override
        public boolean hasBlocking() {
            return hasBlocking;
        }

        @Override
        public boolean hasBackground() {
            return hasBackground;
        }
    }

}
