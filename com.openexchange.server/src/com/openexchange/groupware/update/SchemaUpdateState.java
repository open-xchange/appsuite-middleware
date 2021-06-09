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

package com.openexchange.groupware.update;

import java.util.Date;

/**
 * {@link SchemaUpdateState}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface SchemaUpdateState extends Schema {

    /**
     * Adds an additional update task to the update state.
     *
     * @param taskName The name of the update task to add
     * @param success <code>true</code> if the update task was executed successfully, <code>false</code>, otherwise
     */
    void addExecutedTask(String taskName, boolean success);

    /**
     * Checks if the task associated with given name has been executed.
     *
     * @param taskName The name of the task to check
     * @return <code>true</code> if executed; otherwise <code>false</code> if pending
     */
    boolean isExecuted(String taskName);

    /**
     * Gets a value indicating whether a specific update task has been executed successfully or not.
     *
     * @param taskName The name of the update task to check
     * @return <code>true</code> if the update task was executed successfully, <code>false</code>, otherwise
     */
    boolean isExecutedSuccessfully(String taskName);

    /**
     * Gets the names of all updates tasks that have been executed on the schema.
     *
     * @return The executed update tasks on the schema, or an empty array if there are none
     */
    String[] getExecutedList();

    /**
     * Gets the names of all updates tasks that have been executed on the schema.
     *
     * @param successfulOnly <code>true</code> to only include those tasks that were executed successfully, <code>false</code> to include all executed tasks
     * @return The executed update tasks on the schema, or an empty array if there are none
     */
    String[] getExecutedList(boolean successfulOnly);

    /**
     * Gets the names of all updates tasks that have been executed on the schema.
     *
     * @return The result for executed update tasks on the schema
     */
    NamesOfExecutedTasks getExecuted();

    /**
     * Checks if the background marker is set and therefore background update tasks are running.
     *
     * @return <code>true</code> if background update tasks are running; otherwise <code>false</code>
     */
    boolean backgroundUpdatesRunning();

    /**
     * Gets the time stamp of the background marker.
     *
     * @return The time stamp or <code>null</code> if this instance advertises <code>false</code> for {@link #backgroundUpdatesRunning()}
     */
    Date backgroundUpdatesRunningSince();

    /**
     * Gets the time stamp of the blocking lock.
     *
     * @return The time stamp or <code>null</code> if this instance advertises <code>false</code> for {@link #isLocked()}
     */
    Date blockingUpdatesRunningSince();

}
