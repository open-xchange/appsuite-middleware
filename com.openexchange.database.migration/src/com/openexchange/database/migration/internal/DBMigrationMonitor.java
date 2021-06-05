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

package com.openexchange.database.migration.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.database.migration.DBMigrationMonitorService;

/**
 * Monitors the currently running migration tasks within this JVM. This for instance helps to hinder the server to shut down in case the
 * migration is running on this machine.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationMonitor implements DBMigrationMonitorService {

    /**
     * SingletonHolder is loaded on the first execution of DBMigrationMonitor.getInstance() or the first access to SingletonHolder.INSTANCE,
     * not before.
     */
    private static class SingletonHolder {
        static final DBMigrationMonitor INSTANCE = new DBMigrationMonitor();
    }

    /**
     * Gets the singleton database migration monitor instance.
     *
     * @return The instance
     */
    public static DBMigrationMonitor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<String, Thread> statesBySchema;

    /**
     * Initializes a new {@link DBMigrationMonitor}.
     */
    DBMigrationMonitor() {
        super();
        statesBySchema = new ConcurrentHashMap<String, Thread>();
    }

    /**
     * Adds a new liquibase changelog file name desired to execute to this monitor. This indicates that one or more update tasks for this
     * schema have been scheduled and are going to be executed by the same thread that performs this call.
     *
     * @param fileName The name of the liquibase changelog file to execute
     * @return Whether the state was added or not. If the same thread already added a state, <code>false</code> is returned and the state is
     *         not added.
     */
    public boolean addFile(String fileName) {
        return statesBySchema.putIfAbsent(fileName, Thread.currentThread()) == null;
    }

    /**
     * Removes the given executed liquibase changelog file name if it has been added by this thread.
     *
     * @param fileName The name of the liquibase changelog file to remove
     * @return Whether a state has been removed or not (i.e. wasn't added before).
     */
    public boolean removeFile(String fileName) {
        return statesBySchema.remove(fileName, Thread.currentThread());
    }

    @Override
    public Collection<String> getScheduledFiles() {
        return new ArrayList<String>(statesBySchema.keySet());
    }

}
