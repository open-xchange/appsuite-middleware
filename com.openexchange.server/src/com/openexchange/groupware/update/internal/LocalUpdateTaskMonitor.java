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
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * {@link LocalUpdateTaskMonitor}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class LocalUpdateTaskMonitor {

    private static final LocalUpdateTaskMonitor INSTANCE = new LocalUpdateTaskMonitor();

    private final ConcurrentMap<String, Thread> statesBySchema = new ConcurrentHashMap<String, Thread>();

    private LocalUpdateTaskMonitor() {
        super();
    }

    public static LocalUpdateTaskMonitor getInstance() {
        return INSTANCE;
    }

    /**
     * Adds a schema to this monitor. This indicates that one or more update tasks for
     * this schema have been scheduled and are going to be executed by the same thread that
     * performs this call.
     *
     * @param schema The schema
     * @return Whether the schema was added or not. If the same thread already added
     * a schema, <code>false</code> is returned and the schema is not added.
     */
    public boolean addState(String schema) {
        return statesBySchema.putIfAbsent(schema, Thread.currentThread()) == null;
    }

    /**
     * Removes the given schema if it has been added
     * by this thread.
     *
     * @param schema The schema
     * @return Whether a schema has been removed or not (i.e. wasn't added before).
     */
    public boolean removeState(String schema) {
        return statesBySchema.remove(schema, Thread.currentThread());
    }

    /**
     * Returns a list schemas. Every item indicates that one or more update tasks for this schema
     * have been scheduled and are going to be executed or are currently running.
     *
     * @return A list of schemas
     */
    public Collection<String> getScheduledStates() {
        return new ArrayList<String>(statesBySchema.keySet());
    }

}
