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
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.context.ContextService;
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
        return new String[] {
            com.openexchange.chronos.storage.rdb.groupware.ChronosCreateTableTask.class.getName(),
            "com.openexchange.groupware.update.tasks.CalendarAddIndex2DatesMembersV2"
        };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BLOCKING, SCHEMA);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        MigrationConfig config = new MigrationConfig(services);
        ContextService contextService = services.getService(ContextService.class);
        int[] contextIds = params.getContextsInSameSchema();
        MigrationProgress progress = new MigrationProgress(params.getProgressState(), contextIds.length);
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;
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
