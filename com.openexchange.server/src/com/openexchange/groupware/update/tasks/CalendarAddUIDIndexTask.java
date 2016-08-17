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
import static com.openexchange.tools.update.Tools.createIndex;
import static com.openexchange.tools.update.Tools.existsIndex;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link CalendarAddUIDIndexTask} - Adds (cid,uid) index to calendar tables if missing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CalendarAddUIDIndexTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link CalendarAddUIDIndexTask}.
     */
    public CalendarAddUIDIndexTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { EnlargeCalendarUid.class.getName() };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int cid = params.getContextId();
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        final Connection con = dbService.getForUpdateTask(cid);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            final String[] tables = new String[] {"prg_dates", "del_dates"};
            createCalendarIndex(con, tables);
            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.backNoTimeout(cid, true, con);
        }
    }

    private void createCalendarIndex(final Connection con, final String[] tables) {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CalendarAddUIDIndexTask.class);
        final String name = "uidIndex";
        for (final String table : tables) {
            try {
                final String indexName = existsIndex(con, table, new String[] { "cid", "uid" });
                if (null == indexName) {
                    log.info("Creating new index named \"{}\" with columns (cid,uid) on table {}.", name, table);
                    createIndex(con, table, name, new String[] { "cid", "`uid`(255)" }, false);
                } else {
                    log.info("New index named \"{}\" with columns (cid,uid) already exists on table {}.", indexName, table);
                }
            } catch (final SQLException e) {
                log.error("Problem adding index \"{}\" on table {}.", name, table, e);
            }
        }
    }

}
