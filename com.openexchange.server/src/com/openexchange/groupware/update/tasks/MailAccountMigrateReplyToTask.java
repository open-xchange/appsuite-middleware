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

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link MailAccountMigrateReplyToTask} - Migrate "replyTo" information from properties table to account tables.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountMigrateReplyToTask extends UpdateTaskAdapter {

    public MailAccountMigrateReplyToTask() {
        super();
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING);
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false); // BEGIN
            rollback = 1;

            process("user_mail_account", con);
            process("user_transport_account", con);

            con.commit(); // COMMIT
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    private void process(final String tableName, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, id, value FROM "+tableName+"_properties WHERE name = ?");
            stmt.setString(1, "replyto");
            rs = stmt.executeQuery();
            if (rs.next()) {
                class Prop {

                    int context;
                    int user;
                    int id;
                    String value;

                    Prop(final ResultSet rs) throws SQLException {
                        super();
                        this.context = rs.getInt(1);
                        this.user = rs.getInt(2);
                        this.id = rs.getInt(3);
                        this.value = rs.getString(4);
                    }
                } // End of class Prop
                final List<Prop> props = new LinkedList<Prop>();
                do {
                    props.add(new Prop(rs));
                } while (rs.next());
                closeSQLStuff(rs, stmt);
                rs = null;

                stmt = con.prepareStatement("UPDATE "+tableName+" SET replyTo = ? WHERE cid = ? AND user = ? AND id = ?");
                int pos;
                for (final Prop prop : props) {
                    pos = 1;
                    stmt.setString(pos++, prop.value);
                    stmt.setInt(pos++, prop.context);
                    stmt.setInt(pos++, prop.user);
                    stmt.setInt(pos, prop.id);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }
}
