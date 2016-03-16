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

package com.openexchange.filestore.impl.groupware;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.impl.osgi.Services;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link AddUserColumnToFilestoreUsageTable} - Extends "filestore_usage" table by the column <code>`user`</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AddUserColumnToFilestoreUsageTable extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link AddUserColumnToFilestoreUsageTable}.
     */
    public AddUserColumnToFilestoreUsageTable() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(AddUserColumnToFilestoreUsageTable.class);
        log.info("Performing update task {}", AddUserColumnToFilestoreUsageTable.class.getSimpleName());

        DatabaseService databaseService = Services.requireService(DatabaseService.class);

        int ctxId = params.getContextId();
        Connection con = databaseService.getForUpdateTask(ctxId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            // Add the new column
            Column[] columns;
            {
                List<Column> l = new LinkedList<Column>();
                l.add(new Column("user", "INT4 unsigned NOT NULL DEFAULT 0"));
                columns = l.toArray(new Column[l.size()]);
            }
            Tools.checkAndAddColumns(con, "filestore_usage", columns);

            // Change PRIMARY_KEY
            Tools.createPrimaryKeyIfAbsent(con, "filestore_usage", new String[] { "cid", "user" });

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            databaseService.backForUpdateTask(ctxId, con);
        }

        log.info("{} successfully performed.", AddUserColumnToFilestoreUsageTable.class.getSimpleName());
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AddFilestoreColumnsToUserTable.class.getName() };
    }
}
