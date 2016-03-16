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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link FolderDefaultValuesForDelTablesTasks} - Adds default values to the 'del_oxfolder_tree', and 'virtualBackupTree' tables.
 * <p>
 * <ul>
 * <li>del_oxfolder_tree<ul><li>fname</ul>
 * <li>virtualBackupTree<ul><li>name</ul>
 * </ul>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderDefaultValuesForDelTablesTasks extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link FolderDefaultValuesForDelTablesTasks}.
     */
    public FolderDefaultValuesForDelTablesTasks() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int contextID = params.getContextId();
        final DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);

        final Connection connection = databaseService.getForUpdateTask(contextID);
        boolean rollback = false;
        try {
            connection.setAutoCommit(false);
            rollback = true;

            if (!com.openexchange.tools.update.Tools.hasDefaultValue(connection, "del_oxfolder_tree", "fname")) {
                PreparedStatement stmt = null;
                try {
                    stmt = connection.prepareStatement("ALTER TABLE del_oxfolder_tree ALTER COLUMN fname SET DEFAULT ''");
                    stmt.executeUpdate();
                } finally {
                    DBUtils.closeSQLStuff(stmt);
                }
            }

            if (!com.openexchange.tools.update.Tools.hasDefaultValue(connection, "virtualBackupTree", "name")) {
                PreparedStatement stmt = null;
                try {
                    stmt = connection.prepareStatement("ALTER TABLE virtualBackupTree ALTER COLUMN name SET DEFAULT ''");
                    stmt.executeUpdate();
                } finally {
                    DBUtils.closeSQLStuff(stmt);
                }
            }

            connection.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(connection);
            }
            autocommit(connection);
            Database.backNoTimeout(contextID, true, connection);
        }
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(com.openexchange.groupware.update.UpdateConcurrency.BLOCKING);
    }

}
