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

package com.openexchange.snippet.rdb.groupware;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.snippet.db.Tables;
import com.openexchange.snippet.rdb.Services;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbSnippetCreateTableTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbSnippetCreateTableTask extends AbstractCreateTableImpl implements UpdateTaskV2 {

    /**
     * Initializes a new {@link RdbSnippetCreateTableTask}.
     */
    public RdbSnippetCreateTableTask() {
        super();
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { Tables.getSnippetTable(), RdbSnippetTables.getSnippetContentTable(), RdbSnippetTables.getSnippetAttachmentTable(), RdbSnippetTables.getSnippetMiscTable() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    public String[] getDependencies() {
        return NO_TABLES;
    }

    @Override
    public void perform(final PerformParameters params) throws com.openexchange.exception.OXException {
        final int contextId = params.getContextId();
        final DatabaseService ds = getService(DatabaseService.class);
        final Connection writeCon = ds.getForUpdateTask(contextId);
        try {
            createTable(Tables.getSnippetName(), Tables.getSnippetTable(), writeCon);
            createTable(RdbSnippetTables.getSnippetContentName(), RdbSnippetTables.getSnippetContentTable(), writeCon);
            createTable(RdbSnippetTables.getSnippetAttachmentName(), RdbSnippetTables.getSnippetAttachmentTable(), writeCon);
            createTable(RdbSnippetTables.getSnippetMiscName(), RdbSnippetTables.getSnippetMiscTable(), writeCon);
        } finally {
            ds.backForUpdateTask(contextId, writeCon);
        }
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RdbSnippetCreateTableTask.class);
        logger.info("UpdateTask ''{}'' successfully performed!", RdbSnippetCreateTableTask.class.getSimpleName());
    }

    private void createTable(final String tablename, final String sqlCreate, final Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        try {
            if (tableExists(writeCon, tablename)) {
                return;
            }
            stmt = writeCon.prepareStatement(sqlCreate);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private boolean tableExists(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            retval = (rs.next() && rs.getString("TABLE_NAME").equals(table));
        } finally {
            DBUtils.closeSQLStuff(rs);
        }
        return retval;
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { Tables.getSnippetName(), RdbSnippetTables.getSnippetContentName(), RdbSnippetTables.getSnippetAttachmentName(), RdbSnippetTables.getSnippetMiscName() };
    }

    private <S> S getService(final Class<? extends S> clazz) throws OXException {
        try {
            return Services.getService(clazz);
        } catch (final RuntimeException e) {
            throw new OXException(e);
        }
    }

}
