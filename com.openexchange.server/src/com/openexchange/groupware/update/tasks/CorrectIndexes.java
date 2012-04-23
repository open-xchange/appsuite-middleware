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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import static com.openexchange.tools.update.Tools.createIndex;
import static com.openexchange.tools.update.Tools.dropIndex;
import static com.openexchange.tools.update.Tools.existsIndex;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CorrectIndexes implements UpdateTask {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CorrectIndexes.class));

    public CorrectIndexes() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addedWithVersion() {
        return 30;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    @Override
    public void perform(final Schema schema, final int contextId)
        throws OXException {
        final Connection con = Database.getNoTimeout(contextId, true);
        try {
            con.setAutoCommit(false);
            correctAppointmentIndexes(con);
            correctGroupIndexes(con);
            correctResourceIndexes(con);
            correctSettingsIndexes(con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    private void correctAppointmentIndexes(final Connection con) {
        final String table = "prg_dates";
        final String[] oldcolumns1 = new String[] { "timestampfield01" };
        final String[] newcolumns1 = new String[] { "cid", "timestampfield01" };
        try {
            final String index2Drop = existsIndex(con, table, oldcolumns1);
            if (null != index2Drop) {
                LOG.info("Dropping old index " + index2Drop
                    + " on table " + table + ".");
                dropIndex(con, table, index2Drop);
            }
            final String index2Create = existsIndex(con, table, newcolumns1);
            if (null == index2Create) {
                LOG.info("Creating new index (cid,timestampfield01) on table "
                    + table + ".");
                createIndex(con, table, newcolumns1);
            }
        } catch (final SQLException e) {
            LOG.error("Problem correcting indexes on table " + table + ".", e);
        }
        final String[] oldcolumns2 = new String[] { "timestampfield02" };
        final String[] newcolumns2 = new String[] { "cid", "timestampfield02" };
        try {
            final String index2Drop = existsIndex(con, table, oldcolumns2);
            if (null != index2Drop) {
                LOG.info("Dropping old index " + index2Drop + " on table "
                    + table + ".");
                dropIndex(con, table, index2Drop);
            }
            final String index2Create = existsIndex(con, table, newcolumns2);
            if (null == index2Create) {
                LOG.info("Creating new index (cid,timestampfield02) on table "
                    + table + ".");
                createIndex(con, table, newcolumns2);
            }
        } catch (final SQLException e) {
            LOG.error("Problem correcting indexes on table " + table + ".", e);
        }
        final String[] newcolumns3 = new String[] { "cid", "intfield02" };
        try {
            final String index2Create = existsIndex(con, table, newcolumns3);
            if (null == index2Create) {
                LOG.info("Creating new index (cid,intfield02) on table "
                    + table + ".");
                createIndex(con, table, newcolumns3);
            }
        } catch (final SQLException e) {
            LOG.error("Problem correcting indexes on table " + table + ".", e);
        }
    }

    private void correctSettingsIndexes(final Connection con) {
        final String table = "user_setting_mail_signature";
        final String[] oldcolumns = new String[] { "cid", "user" };
        try {
            final String index2Drop = existsIndex(con, table, oldcolumns);
            if (null != index2Drop) {
                LOG.info("Dropping old index " + index2Drop
                    + " on table " + table + ".");
                dropIndex(con, table, index2Drop);
            }
        } catch (final SQLException e) {
            LOG.error("Problem correcting indexes on table " + table + ".", e);
        }
    }

    private void correctGroupIndexes(final Connection con) {
        final String table1 = "groups";
        final String[] oldcolumns1 = new String[] { "identifier" };
        try {
            final String index2Drop = existsIndex(con, table1, oldcolumns1);
            if (null != index2Drop) {
                LOG.info("Dropping old index " + index2Drop
                    + " on table " + table1 + ".");
                dropIndex(con, table1, index2Drop);
            }
        } catch (final SQLException e) {
            LOG.error("Problem correcting indexes on table " + table1 + ".", e);
        }
        final String table2 = "groups_member";
        final String[] oldcolumns2 = new String[] { "cid", "id" };
        try {
            final String index2Drop = existsIndex(con, table2, oldcolumns2);
            if (null != index2Drop) {
                LOG.info("Dropping old index " + index2Drop
                    + " on table " + table2 + ".");
                dropIndex(con, table2, index2Drop);
            }
        } catch (final SQLException e) {
            LOG.error("Problem correcting indexes on table " + table2 + ".", e);
        }
    }

    private void correctResourceIndexes(final Connection con) {
        final String table = "resource";
        final String[] oldcolumns = new String[] { "identifier" };
        try {
            final String index2Drop = existsIndex(con, table, oldcolumns);
            if (null != index2Drop) {
                LOG.info("Dropping old index " + index2Drop
                    + " on table " + table + ".");
                dropIndex(con, table, index2Drop);
            }
        } catch (final SQLException e) {
            LOG.error("Problem correcting indexes on table " + table + ".", e);
        }
    }
}
