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

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link AllowNullValuesForStandardFolderNamesUpdateTask}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class AllowNullValuesForStandardFolderNamesUpdateTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link AllowNullValuesForStandardFolderNamesUpdateTask}.
     */
    public AllowNullValuesForStandardFolderNamesUpdateTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextId = params.getContextId();
        Connection con = null;
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        try {
            con = dbService.getForUpdateTask(contextId);
            con.setAutoCommit(false);
            Column[] columns = new Column[14];
            columns[0] = new Column("trash", "varchar(64)");
            columns[1] = new Column("sent", "varchar(64)");
            columns[2] = new Column("drafts", "varchar(64)");
            columns[3] = new Column("spam", "varchar(64)");
            columns[4] = new Column("confirmed_spam", "varchar(64)");
            columns[5] = new Column("confirmed_ham", "varchar(64)");
            columns[6] = new Column("archive", "varchar(64)");
            columns[7] = new Column("trash_fullname", "varchar(256)");
            columns[8] = new Column("sent_fullname", "varchar(256)");
            columns[9] = new Column("drafts_fullname", "varchar(256)");
            columns[10] = new Column("spam_fullname", "varchar(256)");
            columns[11] = new Column("confirmed_spam_fullname", "varchar(256)");
            columns[12] = new Column("confirmed_ham_fullname", "varchar(256)");
            columns[13] = new Column("archive_fullname", "varchar(256)");
            Tools.modifyColumns(con, "user_mail_account", false, columns);
            con.commit();
        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            dbService.backForUpdateTask(contextId, con);
        }

    }

    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.AddStartTLSColumnForMailAccountTablesTask.class.getName() };
    }


}
