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

package com.openexchange.groupware.update;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;


/**
 * A {@link ChangeColumnTypeUpdateTask} is a utility class for writing update tasks that want to modify a column type.
 * The UpdateTask checks the current column type against the given sqlType (according to java.sql.Types) and, if the
 * type does not match, issues an alter table statement to change the column type.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class ChangeColumnTypeUpdateTask implements UpdateTaskV2 {

    private final DatabaseService dbService;
    private final String tableName;
    private final Column column;

    /**
     *
     * Initializes a new {@link ChangeColumnTypeUpdateTask}.
     * @param tableName The name of the table that may have to be modified
     * @param columnName The name of the column that may have to be modified
     * @param newType The column definition
     * @param sqlType The type the column should be changed into according to java.sql.Types
     * @param getDatabaseService() A database service to access the database
     */
    public ChangeColumnTypeUpdateTask(DatabaseService dbService, String tableName, String columnName, String newType){
        this.column = new Column(columnName, newType);
        this.tableName = tableName;
        this.dbService = dbService;
    }


    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextId = params.getContextId();
        Connection con = null;
        try {
            con = getDatabaseService().getForUpdateTask(contextId);
            if ( ! correctType(con) ) {
                before(con);
                changeType(con);
                after(con);
            }
        } catch (SQLException x) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(x.getMessage(), x);
        } finally {
            if(con != null) {
                getDatabaseService().backForUpdateTask(contextId, con);
            }
        }
    }

    public DatabaseService getDatabaseService() {
        return dbService;
    }

    protected void before(Connection con) throws SQLException {
        // May be overridden
    }

    protected void after(Connection con) throws SQLException {
        // May be overridden
    }

    protected void changeType(Connection con) throws SQLException {
        Tools.modifyColumns(con, tableName, modifyColumn(column));
    }

    protected Column modifyColumn(Column c) {
        return c;
    }


    protected boolean correctType(Connection con) throws OXException, SQLException {
        String name = Tools.getColumnTypeName(con, tableName, column.getName());
        if(name == null) {
            throw UpdateExceptionCodes.COLUMN_NOT_FOUND.create(column.getName());
        }
        return name.equalsIgnoreCase(column.getDefinition());
    }


}
