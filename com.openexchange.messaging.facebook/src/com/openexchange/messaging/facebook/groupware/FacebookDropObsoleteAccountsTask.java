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

package com.openexchange.messaging.facebook.groupware;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.messaging.facebook.FacebookConstants;
import com.openexchange.messaging.facebook.FacebookMessagingService;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link FacebookDropObsoleteAccountsTask} - Drops obsolete Facebook messaging accounts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FacebookDropObsoleteAccountsTask extends UpdateTaskAdapter {

    private final DatabaseService dbService;

    /**
     * Initializes a new {@link FacebookDropObsoleteAccountsTask}.
     */
    public FacebookDropObsoleteAccountsTask(final DatabaseService dbService) {
        super();
        this.dbService = dbService;
    }

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.messaging.generic.groupware.MessagingGenericCreateTableTask", "com.openexchange.groupware.update.tasks.CreateGenconfTablesTask" };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int contextId = params.getContextId();
        final Connection writeCon = dbService.getForUpdateTask(contextId);
        try {
            writeCon.setAutoCommit(false);
            final List<int[]> dataList = listFacebookMessagingAccounts(writeCon);
            for (final int[] data : dataList) {
                if (!checkData(data, writeCon)) {
                    dropAccountByData(data, writeCon);
                }
            }
            writeCon.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(writeCon);
            throw createSQLError(e);
        } catch (final OXException e) {
            DBUtils.rollback(writeCon);
            throw e;
        } catch (final Exception e) {
            DBUtils.rollback(writeCon);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            dbService.backForUpdateTask(contextId, writeCon);
        }
    }

    private static void dropAccountByData(final int[] data, final Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        try {
            /*
             * Delete genconf
             */
            stmt = writeCon.prepareStatement("DELETE FROM genconf_attributes_strings WHERE cid = ? AND id = ?");
            stmt.setInt(1, data[3]);
            stmt.setInt(2, data[1]);
            stmt.executeUpdate();
            /*
             * Delete account
             */
            stmt.close();
            stmt = writeCon.prepareStatement("DELETE FROM messagingAccount WHERE cid = ? AND user = ? AND serviceId = ? AND account = ?");
            stmt.setInt(1, data[3]);
            stmt.setInt(2, data[2]);
            stmt.setString(3, FacebookMessagingService.getServiceId());
            stmt.setInt(4, data[0]);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static boolean checkData(final int[] data, final Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = writeCon.prepareStatement("SELECT value FROM genconf_attributes_strings WHERE cid = ? AND id = ? AND name = ?");
            stmt.setInt(1, data[3]);
            stmt.setInt(2, data[1]);
            stmt.setString(3, FacebookConstants.FACEBOOK_OAUTH_ACCOUNT);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static List<int[]> listFacebookMessagingAccounts(final Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = writeCon.prepareStatement("SELECT account, confId, user, cid FROM messagingAccount WHERE serviceId = ?");
            stmt.setString(1, FacebookMessagingService.getServiceId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<int[]> dataList = new ArrayList<int[]>(64);
            do {
                final int[] data = new int[4];
                data[0] = rs.getInt(1); // account
                data[1] = rs.getInt(2); // confId
                data[2] = rs.getInt(3); // user
                data[3] = rs.getInt(4); // cid
                dataList.add(data);
            } while (rs.next());
            return dataList;
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static OXException createSQLError(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }
}
