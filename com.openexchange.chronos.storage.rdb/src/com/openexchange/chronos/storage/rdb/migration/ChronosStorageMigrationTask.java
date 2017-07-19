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

package com.openexchange.chronos.storage.rdb.migration;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.groupware.update.UpdateConcurrency.BLOCKING;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
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
        return new String[] { com.openexchange.chronos.storage.rdb.groupware.ChronosCreateTableTask.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BLOCKING, SCHEMA);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        MigrationConfig config = new MigrationConfig(services);
        DatabaseService dbService = services.getService(DatabaseService.class);
        int[] contextIds = dbService.getContextsInSameSchema(params.getContextId());
        ProgressState progressState = params.getProgressState();
        progressState.setTotal(contextIds.length);
        Connection connection = dbService.getForUpdateTask(params.getContextId());
        boolean committed = false;
        try {
            connection.setAutoCommit(false);
            DBProvider dbProvider = new SimpleDBProvider(connection, connection);
            for (int i = 0; i < contextIds.length; i++) {
                progressState.setState(i);
                migrate(connection, contextIds[i], config, dbProvider);
            }
            if (config.isUncommitted()) {
                LOG.warn("Skipping commit phase as migration is configured in 'uncommited' mode.");
            } else {
                connection.commit();
                committed = true;
            }
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (false == committed) {
                rollback(connection);
                autocommit(connection);
                dbService.backForUpdateTaskAfterReading(params.getContextId(), connection);
            } else {
                autocommit(connection);
                dbService.backForUpdateTask(params.getContextId(), connection);
            }
        }
    }

    private void migrate(Connection connection, int contextId, MigrationConfig config, DBProvider dbProvider) throws OXException {
        /*
         * initialize source- & destination storage & perform context migration
         */
        Context context = services.getService(ContextService.class).loadContext(contextId);
        EntityResolver entityResolver = optEntityResolver(services, contextId);
        CalendarStorage sourceStorage = new com.openexchange.chronos.storage.rdb.legacy.RdbCalendarStorage(
            context, entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        CalendarStorage destinationStorage = new com.openexchange.chronos.storage.rdb.RdbCalendarStorage(
            context, 0, entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        migrate(contextId, config, sourceStorage, destinationStorage);
    }

    private void migrate(int contextId, MigrationConfig config, CalendarStorage sourceStorage, CalendarStorage destinationStorage) throws OXException {
        HashMap<String, String> contextAttributes = new HashMap<String, String>();
        contextAttributes.put("config/com.openexchange.chronos.useLegacyStorage", "false");
        contextAttributes.put("config/com.openexchange.chronos.replayToLegacyStorage", "true");
        /*
         * prepare & perform migration tasks
         */
        List<MigrationTask> tasks = Arrays.<MigrationTask>asList(
            new CopyCalendarDataTask(config, contextId, sourceStorage, destinationStorage),
            new CopyTombstoneDataTask(config, contextId, sourceStorage, destinationStorage),
            new MarkContextTask(config, contextId, sourceStorage, destinationStorage, contextAttributes)
        );
        perform(tasks);
    }

    private void perform(List<MigrationTask> tasks) throws OXException {
        for (MigrationTask migrationTask : tasks) {
            try {
                migrationTask.call();
            } catch (Exception e) {
                throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    private static EntityResolver optEntityResolver(ServiceLookup services, int contextId) {
        CalendarUtilities calendarUtilities = services.getOptionalService(CalendarUtilities.class);
        if (null != calendarUtilities) {
            try {
                return calendarUtilities.getEntityResolver(contextId);
            } catch (OXException e) {
                LOG.warn("Error getting entity resolver for context {}: {}", I(contextId), e.getMessage(), e);
            }
        }
        return null;
    }

}
