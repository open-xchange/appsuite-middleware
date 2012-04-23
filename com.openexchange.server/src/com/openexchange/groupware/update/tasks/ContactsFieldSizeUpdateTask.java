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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;

/**
 * ContactsChangedFromUpdateTask
 *
 * @author <a href="mailto:ben.pahne@open-xchange.com">Ben Pahne</a>
 *
 */
public final class ContactsFieldSizeUpdateTask implements UpdateTask {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactsFieldSizeUpdateTask.class));

    public ContactsFieldSizeUpdateTask() {
        super();
    }

    @Override
    public int addedWithVersion() {
        return 15;
    }

    @Override
    public int getPriority() {
        /*
         * Modification on database: highest priority.
         */
        return UpdateTask.UpdateTaskPriority.HIGHEST.priority;
    }

    private static final String STR_INFO = "Performing update task 'ContactsFieldSizeUpdateTask'";

    @Override
    public void perform(final Schema schema, final int contextId) throws OXException {
        if (LOG.isInfoEnabled()) {
            LOG.info(STR_INFO);
        }
        correctTable("prg_contacts", contextId);
        correctTable("del_contacts", contextId);
    }

    private void correctTable(final String sqltable, final int contextId) throws OXException {
        final Result result = determineResult(sqltable, contextId);
        dropColumns(result.toDelete, sqltable, contextId);
        changeColumns(result.toChange, sqltable, contextId);
    }

    private Result determineResult(final String sqltable, final int contextId) throws OXException {
        /*
         * Create a map containing desired columns' VARCHAR size
         */
        final Map<String, Integer> columnRefer = new HashMap<String, Integer>(100);
        columnRefer.put("field01", Integer.valueOf(320));
        columnRefer.put("field02", Integer.valueOf(128));
        columnRefer.put("field03", Integer.valueOf(128));
        columnRefer.put("field04", Integer.valueOf(128));
        columnRefer.put("field05", Integer.valueOf(64));
        columnRefer.put("field06", Integer.valueOf(64));
        columnRefer.put("field07", Integer.valueOf(256));
        columnRefer.put("field08", Integer.valueOf(64));
        columnRefer.put("field09", Integer.valueOf(64));
        columnRefer.put("field10", Integer.valueOf(64));
        columnRefer.put("field11", Integer.valueOf(64));
        columnRefer.put("field12", Integer.valueOf(64));
        columnRefer.put("field13", Integer.valueOf(64));
        columnRefer.put("field14", Integer.valueOf(64));
        columnRefer.put("field15", Integer.valueOf(64));
        columnRefer.put("field16", Integer.valueOf(64));
        columnRefer.put("field17", Integer.valueOf(5680));
        columnRefer.put("field18", Integer.valueOf(512));
        columnRefer.put("field19", Integer.valueOf(128));
        columnRefer.put("field20", Integer.valueOf(128));
        columnRefer.put("field21", Integer.valueOf(64));
        columnRefer.put("field22", Integer.valueOf(64));
        columnRefer.put("field23", Integer.valueOf(256));
        columnRefer.put("field24", Integer.valueOf(64));
        columnRefer.put("field25", Integer.valueOf(128));
        columnRefer.put("field26", Integer.valueOf(64));
        columnRefer.put("field27", Integer.valueOf(64));
        columnRefer.put("field28", Integer.valueOf(64));
        columnRefer.put("field29", Integer.valueOf(64));
        columnRefer.put("field30", Integer.valueOf(128));
        columnRefer.put("field31", Integer.valueOf(64));
        columnRefer.put("field32", Integer.valueOf(64));
        columnRefer.put("field33", Integer.valueOf(64));
        columnRefer.put("field34", Integer.valueOf(5192));
        columnRefer.put("field35", Integer.valueOf(64));
        columnRefer.put("field36", Integer.valueOf(64));
        columnRefer.put("field37", Integer.valueOf(256));
        columnRefer.put("field38", Integer.valueOf(64));
        columnRefer.put("field39", Integer.valueOf(64));
        columnRefer.put("field40", Integer.valueOf(64));
        columnRefer.put("field41", Integer.valueOf(64));
        columnRefer.put("field42", Integer.valueOf(64));
        columnRefer.put("field43", Integer.valueOf(64));
        columnRefer.put("field44", Integer.valueOf(128));
        columnRefer.put("field45", Integer.valueOf(64));
        columnRefer.put("field46", Integer.valueOf(64));
        columnRefer.put("field47", Integer.valueOf(64));
        columnRefer.put("field48", Integer.valueOf(64));
        columnRefer.put("field49", Integer.valueOf(64));
        columnRefer.put("field50", Integer.valueOf(64));
        columnRefer.put("field51", Integer.valueOf(64));
        columnRefer.put("field52", Integer.valueOf(64));
        columnRefer.put("field53", Integer.valueOf(64));
        columnRefer.put("field54", Integer.valueOf(64));
        columnRefer.put("field55", Integer.valueOf(64));
        columnRefer.put("field56", Integer.valueOf(64));
        columnRefer.put("field57", Integer.valueOf(64));
        columnRefer.put("field58", Integer.valueOf(64));
        columnRefer.put("field59", Integer.valueOf(64));
        columnRefer.put("field60", Integer.valueOf(64));
        columnRefer.put("field61", Integer.valueOf(64));
        columnRefer.put("field62", Integer.valueOf(64));
        columnRefer.put("field63", Integer.valueOf(64));
        columnRefer.put("field64", Integer.valueOf(64));
        columnRefer.put("field65", Integer.valueOf(256));
        columnRefer.put("field66", Integer.valueOf(256));
        columnRefer.put("field67", Integer.valueOf(256));
        columnRefer.put("field68", Integer.valueOf(128));
        columnRefer.put("field69", Integer.valueOf(1024));
        columnRefer.put("field70", Integer.valueOf(64));
        columnRefer.put("field71", Integer.valueOf(64));
        columnRefer.put("field72", Integer.valueOf(64));
        columnRefer.put("field73", Integer.valueOf(64));
        columnRefer.put("field74", Integer.valueOf(64));
        columnRefer.put("field75", Integer.valueOf(64));
        columnRefer.put("field76", Integer.valueOf(64));
        columnRefer.put("field77", Integer.valueOf(64));
        columnRefer.put("field78", Integer.valueOf(64));
        columnRefer.put("field79", Integer.valueOf(64));
        columnRefer.put("field80", Integer.valueOf(64));
        columnRefer.put("field81", Integer.valueOf(64));
        columnRefer.put("field82", Integer.valueOf(64));
        columnRefer.put("field83", Integer.valueOf(64));
        columnRefer.put("field84", Integer.valueOf(64));
        columnRefer.put("field85", Integer.valueOf(64));
        columnRefer.put("field86", Integer.valueOf(64));
        columnRefer.put("field87", Integer.valueOf(64));
        columnRefer.put("field88", Integer.valueOf(64));
        columnRefer.put("field89", Integer.valueOf(64));
        columnRefer.put("field90", Integer.valueOf(320));
        /*
         * Create a set containing the columns to delete
         */
        final Set<String> columnDelete = new HashSet<String>(Arrays.asList("field91", "field92", "field93", "field94",
                "field95", "field96", "field97", "field98", "field99"));
        /*
         * Compare desired VARCHAR size with actual VARCHAR size and track
         * results in a Result object
         */
        final Connection writeCon;
        try {
            writeCon = Database.get(contextId, true);
        } catch (final OXException e) {
            throw new OXException(e);
        }
        ResultSet rs = null;
        try {
            final DatabaseMetaData metadata = writeCon.getMetaData();
            rs = metadata.getColumns(null, null, sqltable, null);
            final Map<String, Integer> toChange = new HashMap<String, Integer>(100);
            final Set<String> toDelete = new HashSet<String>(10);
            while (rs.next()) {
                final String name = rs.getString("COLUMN_NAME");
                if (columnDelete.contains(name)) {
                    /*
                     * A column that shall be dropped
                     */
                    toDelete.add(name);
                } else if (columnRefer.containsKey(name)) {
                    /*
                     * A column whose VARCHAR size shall possibly be changed
                     */
                    final int size = rs.getInt("COLUMN_SIZE");
                    final Integer desiredSize = columnRefer.get(name);
                    if (desiredSize.intValue() == size) {
                        LOG.info("FIELD " + sqltable + '.' + name + " WITH SIZE " + size + " IS CORRECT "
                                + desiredSize);
                    } else {
                        LOG.warn("CHANGE FIELD " + sqltable + '.' + name + " WITH SIZE " + size + " TO NEW SIZE "
                                + desiredSize);
                        toChange.put(name, desiredSize);
                    }
                }
            }
            return new Result(toChange, toDelete);
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs);
            Database.back(contextId, true, writeCon);
        }
    }

    private void dropColumns(final Set<String> toDelete, final String sqltable, final int contextId)
            throws OXException {
        if (toDelete.isEmpty()) {
            /*
             * Nothing to drop
             */
            return;
        }
        /*
         * Compose ALTER command
         */
        final String alterCommand;
        {
            final int size = toDelete.size();
            final StringBuilder alterBuilder = new StringBuilder((size + 1) << 5);
            alterBuilder.append("ALTER TABLE ").append(sqltable).append(' ');
            final Iterator<String> iter = toDelete.iterator();
            alterBuilder.append("DROP COLUMN ").append(iter.next());
            for (int i = 1; i < size; i++) {
                alterBuilder.append(", DROP COLUMN ").append(iter.next());
            }
            alterCommand = alterBuilder.toString();
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("DROPPING SQL FIELDS: " + alterCommand);
        }
        executeAlterCommand(alterCommand, contextId);
    }

    private void changeColumns(final Map<String, Integer> toChange, final String sqltable, final int contextId)
            throws OXException {
        if (toChange.isEmpty()) {
            /*
             * Nothing to change
             */
            return;
        }
        /*
         * Compose ALTER command
         */
        final String alterCommand;
        {
            final int size = toChange.size();
            final StringBuilder alterBuilder = new StringBuilder((size + 1) << 5);
            alterBuilder.append("ALTER TABLE ").append(sqltable).append(' ');
            final Iterator<Map.Entry<String, Integer>> iter = toChange.entrySet().iterator();
            {
                final Map.Entry<String, Integer> entry = iter.next();
                alterBuilder.append("MODIFY ").append(entry.getKey()).append(" VARCHAR(").append(
                        entry.getValue().intValue()).append(')');
            }
            for (int i = 1; i < size; i++) {
                final Map.Entry<String, Integer> entry = iter.next();
                alterBuilder.append(", MODIFY ").append(entry.getKey()).append(" VARCHAR(").append(
                        entry.getValue().intValue()).append(')');
            }
            alterCommand = alterBuilder.toString();
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("CHANGING SQL FIELDS' SIZE: " + alterCommand);
        }
        executeAlterCommand(alterCommand, contextId);
    }

    private void executeAlterCommand(final String alterCommand, final int contextId) throws OXException {
        /*
         * Fetch a writable connection
         */
        final Connection writeCon;
        try {
            writeCon = Database.get(contextId, true);
        } catch (final OXException e) {
            throw new OXException(e);
        }
        Statement st = null;
        try {
            /*
             * Execute ALTER command with a new statement
             */
            st = writeCon.createStatement();
            st.executeUpdate(alterCommand);
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, st);
            Database.back(contextId, true, writeCon);
        }
    }

    private final class Result {

        final Map<String, Integer> toChange;

        final Set<String> toDelete;

        /**
         * Initializes a new {@link Result}
         *
         * @param toChange
         *            The map containing the names of the columns to change as
         *            keys and desired column's VARCHAR size as value
         * @param toDelete
         *            The set containing the names of the columns to delete
         */
        public Result(final Map<String, Integer> toChange, final Set<String> toDelete) {
            super();
            this.toChange = toChange;
            this.toDelete = toDelete;
        }

    }

}
