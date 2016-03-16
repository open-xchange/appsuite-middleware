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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import com.openexchange.database.DatabaseService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.update.Tools;

/**
 * {@link CalendarAddNewPrimaryKeyForConfirmPerOccurrence} - Adapts the keys for those calendar tables that carry confirmation information to new "occurrence" column.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CalendarAddNewPrimaryKeyForConfirmPerOccurrence extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link CalendarAddNewPrimaryKeyForConfirmPerOccurrence}.
     */
    public CalendarAddNewPrimaryKeyForConfirmPerOccurrence() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { PrgDatesMembersPrimaryKeyUpdateTask.class.getName(), DelDatesMembersPrimaryKeyUpdateTask.class.getName(), CalendarAddConfirmPerOccurrenceTask.class.getName() };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int contextID = params.getContextId();
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);

        final Connection connnection = dbService.getForUpdateTask(contextID);
        boolean rollback = false;
        try {
            connnection.setAutoCommit(false);
            rollback = true;

            {
                // Drop & re-create primary key
                final String[] tables = new String[] { "prg_dates_members", "del_dates_members" };
                final String[] columns = new String[] {"cid","object_id","member_uid","pfid","occurrence"};

                final int[] lengths = new int[5];
                Arrays.fill(lengths, 0);
                checkPrimaryKey(columns, lengths, tables, connnection);

                // Drop & re-create unique key
                {
                    final String[] oldCols = new String[] {"cid","member_uid","object_id"};
                    final String[] newCols = new String[] {"cid","member_uid","object_id", "occurrence"};
                    checkUniqueKey(oldCols, newCols, tables, connnection);
                }
            }

            {
                // Drop foreign key: dateExternal(cid, objectId) -> prg_dates(cid, intfield01)
                String foreignKey = Tools.existsForeignKey(connnection, "prg_dates", new String[] {"cid", "intfield01"}, "dateExternal", new String[] {"cid", "objectId"});
                if (null != foreignKey && !foreignKey.equals("")) {
                    Tools.dropForeignKey(connnection, "dateExternal", foreignKey);
                }

                // Drop foreign key: delDateExternal(cid, objectId) -> del_dates(cid, intfield01)
                foreignKey = Tools.existsForeignKey(connnection, "del_dates", new String[] {"cid", "intfield01"}, "delDateExternal", new String[] {"cid", "objectId"});
                if (null != foreignKey && !foreignKey.equals("")) {
                    Tools.dropForeignKey(connnection, "delDateExternal", foreignKey);
                }

                // Drop & re-create primary key
                final String[] tables = new String[] { "dateExternal", "delDateExternal" };
                final String[] columns = new String[] {"cid","objectId","mailAddress","occurrence"};
                final int[] lengths = new int[4];
                Arrays.fill(lengths, 0);
                lengths[2] = 255;
                checkPrimaryKey(columns, lengths, tables, connnection);
            }

            connnection.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(connnection);
            }
            autocommit(connnection);
            Database.backNoTimeout(contextID, true, connnection);
        }
    }

    private void checkPrimaryKey(final String[] columns, final int[] lengths, final String[] tables, final Connection connnection) throws SQLException {
        for (final String table : tables) {
            if (!Tools.existsPrimaryKey(connnection, table, columns)) {
                try {
                    Tools.dropPrimaryKey(connnection, table);
                } catch (final Exception x) {
                    // Ignore failed deletion
                }
                Tools.createPrimaryKey(connnection, table, columns, lengths);
            }
        }
    }

    private void checkUniqueKey(final String[] oldColumns, final String[] newColumns, final String[] tables, final Connection connnection) throws SQLException {
        for (final String table : tables) {
            final String oldIndex = Tools.existsIndex(connnection, table, oldColumns);
            if (null != oldIndex) {
                Tools.dropIndex(connnection, table, oldIndex);
            }

            final String newIndex = Tools.existsIndex(connnection, table, newColumns);
            if (null == newIndex) {
                Tools.createIndex(connnection, table, "member", newColumns, true);
            }
        }
    }

}
