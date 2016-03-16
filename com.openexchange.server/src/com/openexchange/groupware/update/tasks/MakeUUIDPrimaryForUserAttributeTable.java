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
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.sql.DBUtils.startTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link MakeUUIDPrimaryForUserAttributeTable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MakeUUIDPrimaryForUserAttributeTable extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link MakeUUIDPrimaryForUserAttributeTable}.
     */
    public MakeUUIDPrimaryForUserAttributeTable() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        ProgressState progress = params.getProgressState();
        Connection con = Database.getNoTimeout(params.getContextId(), true);
        try {
            startTransaction(con);
            progress.setTotal(getTotalRows(con));
            if (!Tools.columnExists(con, "user_attribute", "uuid")) {
                throw UpdateExceptionCodes.COLUMN_NOT_FOUND.create("uuid");
            }

            AddUUIDForUserAttributeTable.fillUUIDs(con, progress);

            dropDuplicates(con);

            // Drop foreign key
            String foreignKey = Tools.existsForeignKey(con, "user", new String[] {"cid", "id"}, "user_attribute", new String[] {"cid", "id"});
            if (null != foreignKey && !foreignKey.equals("")) {
                Tools.dropForeignKey(con, "user_attribute", foreignKey);
            }

            dropOrphaned(con);

            Tools.modifyColumns(con, "user_attribute", new Column("uuid", "BINARY(16) NOT NULL"));
            Tools.createPrimaryKeyIfAbsent(con, "user_attribute", new String[] { "cid", "uuid" });

            // Re-create foreign key ?
            // Tools.createForeignKey(con, "user_attribute", new String[] {"cid", "id"}, "user", new String[] {"cid", "id"});

            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            rollback(con);
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(params.getContextId(), true, con);
        }
    }

    private void dropOrphaned(final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT attrs.cid, attrs.id, hex(uuid) FROM user_attribute AS attrs LEFT JOIN user ON attrs.cid = user.cid AND attrs.id = user.id WHERE user.id IS NULL");
            rs = stmt.executeQuery();

            if (!rs.next()) {
                return;
            }

            class Orphaned {
                final UUID uuid;
                final int cid;
                final int user;

                Orphaned(int cid, int user, UUID uuid) {
                    super();
                    this.cid = cid;
                    this.user = user;
                    this.uuid = uuid;
                }
            }

            List<Orphaned> orphaneds = new LinkedList<Orphaned>();
            do {
                orphaneds.add(new Orphaned(rs.getInt(1), rs.getInt(2), UUIDs.fromUnformattedString(rs.getString(3))));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            for (final Orphaned orphaned : orphaneds) {
                stmt = con.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND id=? AND ?=HEX(uuid)");
                stmt.setInt(1, orphaned.cid);
                stmt.setInt(2, orphaned.user);
                stmt.setString(3, UUIDs.getUnformattedString(orphaned.uuid));
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void dropDuplicates(final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, HEX(uuid) FROM user_attribute GROUP BY cid, uuid HAVING count(*) > 1");
            rs = stmt.executeQuery();

            if (!rs.next()) {
                return;
            }

            class Dup {
                final UUID uuid;
                final int cid;

                Dup(int cid, UUID uuid) {
                    super();
                    this.cid = cid;
                    this.uuid = uuid;
                }
            }

            final List<Dup> dups = new LinkedList<Dup>();
            do {
                dups.add(new Dup(rs.getInt(1), UUIDs.fromUnformattedString(rs.getString(2))));
            } while (rs.next());

            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            for (final Dup dup : dups) {
                stmt = con.prepareStatement("SELECT cid, id, name, value FROM user_attribute WHERE cid=? AND ?=HEX(uuid)");
                stmt.setInt(1, dup.cid);
                stmt.setString(2, UUIDs.getUnformattedString(dup.uuid));
                rs = stmt.executeQuery();

                if (rs.next()) {
                    final int cid = rs.getInt(1);
                    final int id = rs.getInt(2);
                    final String name = rs.getString(3);
                    final String value = rs.getString(4);
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;

                    stmt = con.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND ?=HEX(uuid)");
                    stmt.setInt(1, dup.cid);
                    stmt.setString(2, UUIDs.getUnformattedString(dup.uuid));
                    stmt.executeUpdate();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;

                    stmt = con.prepareStatement("INSERT INTO user_attribute (cid,id,name,value,uuid) VALUES (?,?,?,?,UNHEX(?))");
                    stmt.setInt(1, cid);
                    stmt.setInt(2, id);
                    stmt.setString(3, name);
                    stmt.setString(4, value);
                    stmt.setString(5, UUIDs.getUnformattedString(dup.uuid));
                    stmt.executeUpdate();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }

                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AddUUIDForUserAttributeTable.class.getName() };
    }

    private static int getTotalRows(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        int rows = 0;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM user_attribute WHERE uuid IS NULL");
            while (rs.next()) {
                rows += rs.getInt(1);
            }
        } finally {
            closeSQLStuff(rs, stmt);
        }
        return rows;
    }
}
