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

package com.openexchange.processing.internal;

/**
 * {@link TaskManager} - A task manager responsible for collecting/managing tasks associated with a certain executer key.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface TaskManager {

    /** The special poison element to abort execution */
    public static final TaskManager POISON = new TaskManager() {

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Runnable remove() {
            return null;
        }

        @Override
        public void add(Runnable task) {
            // Nothing
        }

        @Override
        public Object getExecuterKey() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }
    };

    // --------------------------------------------------------------------------------------------------

    /**
     * Gets the number of tasks currently held by this task manager.
     *
     * @return The number of tasks
     */
    int size();

    /**
     * Checks if this task manager contains no tasks.
     *
     * @return <tt>true</tt> if this task manager contains no tasks; otherwise <code>false</code>
     */
    boolean isEmpty();

    /**
     * Removes the next available task from this executer
     *
     * @return The next task or <code>null</code>
     */
    Runnable remove();

    /**
     * Adds given task to this executer.
     *
     * @param task The task to add
     */
    void add(Runnable task);

    /**
     * Gets the key object
     *
     * @return The key object
     */
    Object getExecuterKey();
}