/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;

/**
 * Removes all facebook related account information (OAuth and messaging)
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class RemoveFacebookAccountsTask implements UpdateTaskV2 {

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            removeOAuthAccounts(con);
            removeMessagingAccounts(con);

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
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
            stmt.setString(1, "com.openexchange.oauth.facebook");
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
            stmt.setString(1, "com.openexchange.messaging.facebook");
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
        } catch (SQLException e) {
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
            stmt.setString(3, "com.openexchange.messaging.facebook");
            stmt.setInt(4, data[0]);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
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
