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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.java.Strings;

/**
 * {@link DynamicSet} - Registry for {@link UpdateTask update tasks}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DynamicSet implements UpdateTaskSet<UpdateTaskV2> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DynamicSet.class);

    private static final DynamicSet SINGLETON = new DynamicSet();

    /**
     * Gets the singleton instance of {@link DynamicSet}.
     *
     * @return The singleton instance
     */
    public static DynamicSet getInstance() {
        return SINGLETON;
    }

    /*-
     * -------------------------------- Member section --------------------------------
     */

    private final ConcurrentMap<String, UpdateTaskV2> taskRegistry = new ConcurrentHashMap<String, UpdateTaskV2>();

    /**
     * Initializes a new {@link DynamicSet}.
     */
    private DynamicSet() {
        super();
    }

    /**
     * Adds the specified update task to this registry.
     *
     * @param updateTask The {@link UpdateTaskV2} task to add
     * @return <code>true</code> if the task was successfully registered; <code>false</code>
     *         if the same task was previously registered.
     */
    public boolean addUpdateTask(final UpdateTaskV2 updateTask) {
        if (null == taskRegistry.putIfAbsent(getUpdateTaskName(updateTask), updateTask)) {
            UpdateTaskCollection.getInstance().dirtyVersion();
            return true;
        }

        LOG.error("Update task \"{}\" is already registered.", updateTask.getClass().getName());
        return false;
    }

    /**
     * Returns the name of the update task. If the {@link UpdateTaskV2} is implemented
     * as a local or anonymous class, then its name is being compiled by the {@link Package}
     * information and the class's name. If the {@link Package} is not available, then
     * the name falls back to a 'orphanedUpdateTask.t[timestamp].ClassName' format.
     *
     * @param updateTask The update task's name that shall be returned
     * @return the update task's name
     */
    private String getUpdateTaskName(UpdateTaskV2 updateTask) {
        String canonicalName = updateTask.getClass().getCanonicalName();
        if (Strings.isNotEmpty(canonicalName)) {
            return canonicalName;
        }
        Package pkg = updateTask.getClass().getPackage();
        if (pkg == null) {
            return "orphanedUpdateTask.t" + System.currentTimeMillis() + "." + updateTask.getClass().getName();
        }
        return pkg.getName() + updateTask.getClass().getName();
    }

    /**
     * Removes specified update task from this registry.
     *
     * @param updateTask The update task
     */
    public void removeUpdateTask(final UpdateTaskV2 updateTask) {
        if (null == taskRegistry.remove(getUpdateTaskName(updateTask))) {
            LOG.error("Update task \"{}\" is unknown and could not be deregistered.", updateTask.getClass().getName());
        } else {
            UpdateTaskCollection.getInstance().dirtyVersion();
        }
    }

    @Override
    public Set<UpdateTaskV2> getTaskSet() {
        return new HashSet<UpdateTaskV2>(taskRegistry.values());
    }

}
