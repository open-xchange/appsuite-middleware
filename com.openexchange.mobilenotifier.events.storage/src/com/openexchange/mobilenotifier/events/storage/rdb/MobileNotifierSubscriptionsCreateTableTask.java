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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mobilenotifier.events.storage.rdb;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.tableExists;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.mobilenotifier.MobileNotifierExceptionCodes;
import com.openexchange.mobilenotifier.events.storage.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link MobileNotifierSubscriptionsCreateTableTask}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobileNotifierSubscriptionsCreateTableTask extends UpdateTaskAdapter {

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final DatabaseService dbService = Services.getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final int contextId = params.getContextId();
        final Connection writeCon = dbService.getForUpdateTask(contextId);
        PreparedStatement stmt = null;
        boolean rollback = false;
        boolean restoreAutoCommit = false;
        try {
            writeCon.setAutoCommit(false); // BEGIN
            restoreAutoCommit = true;
            rollback = true;
            final String[] tableNames = MobileNotifierSubscriptionsCreateTableService.getTablesToCreate();
            final String[] createStmts = MobileNotifierSubscriptionsCreateTableService.getCreateStmts();
            for (int i = 0; i < tableNames.length; i++) {
                try {
                    if (tableExists(writeCon, tableNames[i])) {
                        continue;
                    }
                    stmt = writeCon.prepareStatement(createStmts[i]);
                    stmt.executeUpdate();
                } catch (final SQLException e) {
                    throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    DBUtils.closeSQLStuff(stmt);
                }
            }
            writeCon.commit(); // COMMIT
            rollback = false;
        } catch (final SQLException e) {
            throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MobileNotifierExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(writeCon);
            }
            closeSQLStuff(stmt);
            if (restoreAutoCommit) {
                DBUtils.autocommit(writeCon);
            }
            dbService.backForUpdateTask(contextId, writeCon);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

}
