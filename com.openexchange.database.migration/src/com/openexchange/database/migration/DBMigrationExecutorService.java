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

package com.openexchange.database.migration;

import java.util.List;
import com.openexchange.database.migration.resource.accessor.BundleResourceAccessor;
import com.openexchange.exception.OXException;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.resource.ResourceAccessor;

/**
 * Interface that defines the execution of database migration tasks based on {@link Liquibase}.
 * With 7.6.1 only the configdb can be modified. This interface might be extended in future releases
 * to manage migrations of the context databases.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.1
 */
public interface DBMigrationExecutorService {

    /**
     * Registers a database migration for management via an RMI utility.
     *
     * @param migration The migration to register for monitoring
     * @return <code>true</code> if the migration was added, <code>false</code>, otherwise
     */
    boolean register(DBMigration migration);

    /**
     * Schedules a database migration based on the given file location and {@link ResourceAccessor}.
     * The migration file must be resolvable by its location via the accessor.
     *
     * You probably want to use a XML file contained in your bundle-jar. {@link BundleResourceAccessor}
     * is the right choice then. The files location must then be specified absolute, starting at the
     * bundles root directory.
     *
     * @param migration The database migration
     * @return A {@link DBMigrationState} instance, that can be used to wait for completion.
     */
    DBMigrationState scheduleDBMigration(DBMigration migration);

    /**
     * Schedules a database migration based on the given file location and {@link ResourceAccessor}.
     * The migration file must be resolvable by its location via the accessor.
     *
     * You probably want to use a XML file contained in your bundle-jar. {@link BundleResourceAccessor}
     * is the right choice then. The files location must then be specified absolute, starting at the
     * bundles root directory.
     *
     * @param migration The database migration
     * @param callback A migration callback to get notified on completion, or <code>null</code> if not set
     * @return A {@link DBMigrationState} instance, that can be used to wait for completion.
     */
    DBMigrationState scheduleDBMigration(DBMigration migration, DBMigrationCallback callback);

    /**
     * Schedules a rollback of the database for the given number of change sets
     *
     * @param migration The database migration
     * @param numberOfChangeSets Number of change sets to roll back
     * @return A {@link DBMigrationState} instance, that can be used to wait for completion.
     */
    DBMigrationState scheduleDBRollback(DBMigration migration, int numberOfChangeSets);

    /**
     * Schedules a rollback of the database to a given tag. This will roll back all change sets of the
     * given changelog, that were executed after the given tag was applied.
     *
     * @param migration The database migration
     * @param changeSetTag The tag to roll back to
     * @return A {@link DBMigrationState} instance, that can be used to wait for completion.
     */
    DBMigrationState scheduleDBRollback(DBMigration migration, String changeSet);

    /**
     * Returns a list of the currently not executed change sets of the given changelog for the database.
     *
     * @param migration The database migration
     * @return List<ChangeSet> with the currently not executed liquibase change sets
     */
    List<ChangeSet> listUnrunDBChangeSets(DBMigration migration) throws OXException;

    /**
     * Gets if any database migrations are currently running.
     *
     * @return true, if migrations are running; otherwise false
     */
    boolean migrationsRunning();

}
