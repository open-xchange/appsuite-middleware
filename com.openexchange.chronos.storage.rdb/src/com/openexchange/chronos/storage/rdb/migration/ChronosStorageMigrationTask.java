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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
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
        ContextService contextService = services.getService(ContextService.class);
        int[] contextIds = dbService.getContextsInSameSchema(params.getContextId());
        MigrationProgress progress = new MigrationProgress(params.getProgressState(), contextIds.length);
        Connection connection = dbService.getForUpdateTask(params.getContextId());
        boolean committed = false;
        try {
            connection.setAutoCommit(false);
            /*
             * migrate calendar data for all contexts & increment progress
             */
            for (int contextId : contextIds) {
                new CalendarDataMigration(progress, config, contextService.loadContext(contextId), connection).perform();
                progress.nextContext();
            }
            if (config.isUncommitted()) {
                LOG.warn("Skipping commit phase as migration is configured in 'uncommited' mode.");
                return;
            }
            /*
             * mark contexts to switch to new storage & commit
             */
            for (int contextId : contextIds) {
                markContextMigrated(contextService, contextId);
            }
            connection.commit();
            committed = true;
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

    /**
     * Marks a context to use the new calendar storage after a successful data migration.
     *
     * @param contextService A reference to the context service
     * @param contextId The identifier of the context to mark
     */
    private void markContextMigrated(ContextService contextService, int contextId) throws OXException {
        HashMap<String, String> contextAttributes = new HashMap<String, String>();
        contextAttributes.put("config/com.openexchange.chronos.useLegacyStorage", "false");
        contextAttributes.put("config/com.openexchange.chronos.replayToLegacyStorage", "true");
        markContext(contextService, contextId, contextAttributes);
    }

    /**
     * Applies one or more config properties for a specific context.
     *
     * @param contextService A reference to the context service
     * @param contextId The identifier of the context to mark
     * @param attributesToSet The attributes to set
     */
    private void markContext(ContextService contextService, int contextId, Map<String, String> attributesToSet) throws OXException {
        for (Entry<String, String> entry : attributesToSet.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            LOG.trace("About to set attribute \"{}\" to \"{}\" in context {}...", name, value, I(contextId));
            contextService.setAttribute(entry.getKey(), entry.getValue(), contextId);
            LOG.trace("Successfully set attribute \"{}\" to \"{}\" in context {}.", name, value, I(contextId));
        }
        LOG.info("Successfully set {} attributes in context {}.", I(attributesToSet.size()), I(contextId));
    }

}
