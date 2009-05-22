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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.databaseold.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;

/**
 * This update task fixes all charsets and collations on all tables and on the
 * database itself.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public class CorrectCharsetAndCollationTask implements UpdateTask {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(CorrectIndexes.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(CorrectIndexes.class);

    /**
     * Default constructor.
     */
    public CorrectCharsetAndCollationTask() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public int addedWithVersion() {
        return 31;
    }

    /**
     * {@inheritDoc}
     */
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @OXThrowsMultiple(category = { Category.CODE_ERROR },
        desc = { "" },
        exceptionId = { 1 },
        msg = { "An SQL error occurred: %1$s." }
    )
    public void perform(final Schema schema, final int contextId)
        throws AbstractOXException {
        final Connection con = Database.getNoTimeout(contextId, true);
        try {
            con.setAutoCommit(false);
            correctDatabase(con);
            for (final String table : getTables(con)) {
                correctTable(con, table);
            }
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw EXCEPTION.create(1, e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    private void correctDatabase(final Connection con) throws SQLException {
        LOG.info("Correcting database.");
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute("ALTER DATABASE DEFAULT CHARSET utf8 DEFAULT "
                + "COLLATE utf8_unicode_ci");
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private String[] getTables(final Connection con) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet result = null;
        final List<String> tables = new ArrayList<String>();
        try {
            result = metaData.getTables(null, null, null, null);
            while (result.next()) {
                tables.add(result.getString(3));
            }
        } finally {
            closeSQLStuff(result);
        }
        return tables.toArray(new String[tables.size()]);
    }

    private void correctTable(final Connection con, final String table) {
        LOG.info("Correcting table " + table + ".");
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute("ALTER TABLE `" + table + "` CONVERT TO CHARSET utf8 "
                + "COLLATE utf8_unicode_ci;");
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            closeSQLStuff(stmt);
        }
    }
}
