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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * Removes all facebook related account information (OAuth and messaging)
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class RemoveFacebookAccountsTask implements UpdateTaskV2 {

    private static final String OAUTH_ID = "com.openexchange.oauth.facebook";

    private static final String MESSAGING_ID = "com.openexchange.messaging.facebook";

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextId = params.getContextId();
        final DatabaseService ds = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        final Connection con = ds.getForUpdateTask(contextId);
        boolean rb = false;
        try {
            con.setAutoCommit(false);
            rb = true;

            removeOAuthAccounts(con);
            removeMessagingAccounts(con);

            con.commit();
            rb = false;
        } catch (final SQLException x) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(x, x.getMessage());
        } finally {
            if (rb) {
                Databases.rollback(con);
            }
            if (con != null) {
                Databases.autocommit(con);
                ds.backForUpdateTask(contextId, con);
            }
        }
    }

    /**
     * Removes all OAuth accounts related to facebook
     *
     * @param stmt The {@link PreparedStatement} to use
     * @param con The {@link Connection} to get the statement from
     * @throws SQLException
     */
    private void removeOAuthAccounts(Connection con) throws SQLException {
        if (!Databases.tablesExist(con, "oauthAccounts")) {
            return;
        }

        PreparedStatement stmt = con.prepareStatement("DELETE FROM oauthAccounts WHERE serviceId = ?");
        try {
            stmt.setString(1, OAUTH_ID);
            stmt.executeUpdate();
        } finally {
            stmt.close();
        }
    }

    /**
     * Removes all messaging accounts related to facebook
     *
     * @param stmt The {@link PreparedStatement} to use
     * @param con The {@link Connection} to get the statement from
     * @throws SQLException
     */
    private void removeMessagingAccounts(Connection con) throws OXException, SQLException {
        if (!Databases.tablesExist(con, "messagingAccount")) {
            return;
        }

        final List<int[]> dataList = listFacebookMessagingAccounts(con);
        for (final int[] data : dataList) {
            dropAccountByData(data, con);
        }
    }

    private List<int[]> listFacebookMessagingAccounts(Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT account, confId, user, cid FROM messagingAccount WHERE serviceId = ?");
            stmt.setString(1, MESSAGING_ID);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            final List<int[]> dataList = new ArrayList<int[]>(64);
            do {
                final int[] data = new int[4];
                data[0] = rs.getInt(1);// account
                data[1] = rs.getInt(2);// confId
                data[2] = rs.getInt(3);// user
                data[3] = rs.getInt(4);// cid
                dataList.add(data);
            } while (rs.next());

            return dataList;
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void dropAccountByData(int[] data, Connection writeCon) throws OXException {
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
            stmt.setString(3, MESSAGING_ID);
            stmt.setInt(4, data[0]);
            stmt.executeUpdate();
            stmt.close();
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private OXException createSQLError(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BACKGROUND);
    }
}
