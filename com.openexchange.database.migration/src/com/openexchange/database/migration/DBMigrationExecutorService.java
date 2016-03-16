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

package com.openexchange.database.migration;

import java.util.List;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.resource.ResourceAccessor;
import com.openexchange.database.migration.resource.accessor.BundleResourceAccessor;
import com.openexchange.exception.OXException;

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
     * Registers a database migration for management via an utility MBean.
     *
     * @param migration The migration to register for monitoring
     * @return <code>true</code> if the migration was added, <code>false</code>, otherwise
     */
    boolean registerMBean(DBMigration migration);

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
