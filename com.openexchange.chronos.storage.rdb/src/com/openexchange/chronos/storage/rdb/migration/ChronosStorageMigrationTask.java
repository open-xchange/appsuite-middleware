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

package com.openexchange.chronos.storage.rdb.migration;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.groupware.update.UpdateConcurrency.BLOCKING;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.context.ContextService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ChronosStorageMigrationTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ChronosStorageMigrationTask extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ChronosStorageMigrationTask.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ChronosStorageMigrationTask}.
     *
     * @param services A service lookup reference
     */
    public ChronosStorageMigrationTask(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String[] getDependencies() {
        return new String[] {
            "com.openexchange.groupware.update.tasks.AddUserSaltColumnTask",
            com.openexchange.chronos.storage.rdb.groupware.ChronosCreateTableTask.class.getName(),
            "com.openexchange.groupware.update.tasks.CalendarAddIndex2DatesMembersV2",
            com.openexchange.chronos.storage.rdb.groupware.CalendarEventAddRDateColumnTask.class.getName(),
            com.openexchange.chronos.storage.rdb.groupware.CalendarEventAddSeriesIndexTask.class.getName(),
            com.openexchange.chronos.storage.rdb.groupware.CalendarAlarmAddTimestampColumnTask.class.getName(),
            com.openexchange.chronos.storage.rdb.groupware.CalendarEventAdjustRecurrenceColumnTask.class.getName()
        };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BLOCKING, SCHEMA);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        /*
         * check if migration is actually needed in schema
         */
        if (false == needsMigration(params.getConnection())) {
            return;
        }
        ContextService contextService = services.getService(ContextService.class);
        int[] contextIds = params.getContextsInSameSchema();
        MigrationProgress progress = new MigrationProgress(params.getProgressState(), contextIds.length);
        MigrationConfig config = new MigrationConfig(services);
        if (config.isIntermediateCommits()) {
            /*
             * calendar migration will be performed using an individual database connection for each batch
             * (as configured through "com.openexchange.calendar.migration.batchSize")
             */
            migrateCalendarData(contextIds, config, progress, contextService, null);
        } else {
            /*
             * calendar migration will be performed using a single database connection
             */
            Connection connection = params.getConnection();
            int rollback = 0;
            try {
                connection.setAutoCommit(false);
                rollback = 1;

                migrateCalendarData(contextIds, config, progress, contextService, connection);

                connection.commit();
                rollback = 2;
            } catch (SQLException e) {
                throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            } finally {
                if (rollback > 0) {
                    if (rollback == 1) {
                        rollback(connection);
                    }
                    autocommit(connection);
                }
            }
        }
    }

    private void migrateCalendarData(int[] contextIds, MigrationConfig config, MigrationProgress progress, ContextService contextService, Connection optConnection) throws OXException {
        /*
         * migrate calendar data for each context & increment progress
         */
        for (int contextId : contextIds) {
            Context context = tryLoadContext(contextId, contextService);
            if (context != null) {
                try {
                    new CalendarDataMigration(progress, config, context, optConnection).perform();
                } catch (Exception e) {
                    throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, "Error performing calendar migration in context " + contextId);
                }
            }
            progress.nextContext();
        }
        if (config.isUncommitted()) {
            LOG.warn("Skipping commit phase as migration is configured in 'uncommited' mode.");
            return;
        }
    }

    private Context tryLoadContext(int contextId, ContextService contextService) throws OXException {
        try {
            return contextService.loadContext(contextId);
        } catch (OXException e) {
            if (e.equalsCode(1, "CTX")) {
                // Mailadmin for a context is missing; see com.openexchange.groupware.contexts.impl.ContextExceptionCodes.NO_MAILADMIN
                LOG.error("Unable to load context {}, skipping migration.", I(contextId), e);
                return null;
            }
            throw e;
        }
    }

    /**
     * Gets a value indicating whether a calendar data migration is necessary for the underlying database schema or not, based on
     * the presence of processable data in the <i>source</i> or <i>destination</i> calendar storage.
     * 
     * @param connection The database connection to use
     * @return <code>true</code> if the database schema needs to be migrated, <code>false</code> if migration can be skipped for the schema
     */
    private static boolean needsMigration(Connection connection) throws OXException {
        try {
            /*
             * check if source storage exists
             */
            if (false == Databases.tableExists(connection, "prg_dates")) {
                LOG.info("Source calendar storage in schema {} not found, migration is not needed.", connection.getCatalog());
                return false;
            }
            /*
             * check for any data in the source storage
             */
            try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 FROM prg_dates LIMIT 1;")) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        LOG.info("Source calendar storage in schema {} is non-empty, migration is needed.", connection.getCatalog());
                        return true;
                    }
                }
            }
            /*
             * also check for potential stale data in the destination storage
             */
            try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 FROM calendar_event WHERE account=0 LIMIT 1;")) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        LOG.info("Destination calendar storage in schema {} is non-empty, migration is needed.", connection.getCatalog());
                        return true;
                    }
                }
            }
            LOG.info("Calendar storage in schema {} is empty, migration is not needed.", connection.getCatalog());
            return false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
    }

}
