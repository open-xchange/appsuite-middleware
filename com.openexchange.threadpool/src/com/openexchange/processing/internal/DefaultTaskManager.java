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

import java.util.LinkedList;

/**
 * {@link DefaultTaskManager} - The default task manager implementation.
 * <p>
 * This implementation is <b>not</b> thread safe!
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DefaultTaskManager implements TaskManager {

    private final LinkedList<Runnable> tasks;
    private final Object taskKey;

    /**
     * Initializes a new {@link DefaultTaskManager}.
     *
     * @param task The initial task
     * @param key The key associated with this task manager
     */
    public DefaultTaskManager(Runnable task, Object key) {
        super();
        taskKey = key;
        tasks = new LinkedList<Runnable>();
        tasks.offer(task);
    }

    @Override
    public int size() {
        return tasks.size();
    }

    @Override
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    @Override
    public Object getExecuterKey() {
        return taskKey;
    }

    @Override
    public Runnable remove() { // Gets only called when holding lock
        return tasks.poll();
    }

    @Override
    public void add(Runnable task) { // Gets only called when holding lock
        tasks.offer(task);
    }
}
