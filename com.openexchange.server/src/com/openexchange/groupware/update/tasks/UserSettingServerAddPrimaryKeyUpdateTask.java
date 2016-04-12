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
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link UserSettingServerAddPrimaryKeyUpdateTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class UserSettingServerAddPrimaryKeyUpdateTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link UserSettingServerAddPrimaryKeyUpdateTask}.
     */
    public UserSettingServerAddPrimaryKeyUpdateTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int cid = params.getContextId();
        Connection con = Database.getNoTimeout(cid, true);
        Column column = new Column("uuid", "BINARY(16) NOT NULL");
        try {
            con.setAutoCommit(false);
            setUUID(con);
            Tools.modifyColumns(con, "user_setting_server", column);

            dropDuplicates(con);

            // Drop possible foregin keys
            String foreignKey = Tools.existsForeignKey(con, "user", new String[] { "cid", "id" }, "user_setting_server", new String[] { "cid", "user" });
            if (null != foreignKey && !foreignKey.equals("")) {
                Tools.dropForeignKey(con, "user_setting_server", foreignKey);
            }

            dropOrphaned(con);

            Tools.createPrimaryKeyIfAbsent(con, "user_setting_server", new String[] { "cid", "user", column.name });

            // Re-create foreign key ?
            // Tools.createForeignKey(con, "user_setting_server", new String[] {"cid", "user"}, "user", new String[] {"cid", "id"});

            con.commit();
        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            Database.backNoTimeout(cid, true, con);
        }
    }

    private void dropOrphaned(final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT uss.cid, user, hex(uuid) FROM user_setting_server AS uss LEFT JOIN user ON uss.cid = user.cid AND uss.user = user.id WHERE user.id IS NULL");
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
                stmt = con.prepareStatement("DELETE FROM user_setting_server WHERE cid=? AND user=? AND ?=HEX(uuid)");
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
            stmt = con.prepareStatement("SELECT cid, user, HEX(uuid) FROM user_setting_server GROUP BY cid, user, uuid HAVING count(*) > 1");
            rs = stmt.executeQuery();

            if (!rs.next()) {
                return;
            }

            class Dup {

                final UUID uuid;
                final int cid;
                final int user;

                Dup(int cid, int user, UUID uuid) {
                    super();
                    this.cid = cid;
                    this.user = user;
                    this.uuid = uuid;
                }
            }

            List<Dup> dups = new LinkedList<Dup>();
            do {
                dups.add(new Dup(rs.getInt(1), rs.getInt(2), UUIDs.fromUnformattedString(rs.getString(3))));
            } while (rs.next());

            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            for (final Dup dup : dups) {
                stmt = con.prepareStatement("SELECT cid, user, contact_collect_folder, contact_collect_enabled, defaultStatusPrivate, defaultStatusPublic, contactCollectOnMailAccess, contactCollectOnMailTransport, folderTree FROM user_setting_server WHERE cid=? AND user=? AND ?=HEX(uuid)");
                stmt.setInt(1, dup.cid);
                stmt.setInt(2, dup.user);
                stmt.setString(3, UUIDs.getUnformattedString(dup.uuid));
                rs = stmt.executeQuery();

                if (rs.next()) {
                    final int cid = rs.getInt(1);
                    final int user = rs.getInt(2);
                    final int contact_collect_folder = rs.getInt(3);
                    final int contact_collect_enabled = rs.getInt(4);
                    final int defaultStatusPrivate = rs.getInt(5);
                    final int defaultStatusPublic = rs.getInt(6);
                    final int contactCollectOnMailAccess = rs.getInt(7);
                    final int contactCollectOnMailTransport = rs.getInt(8);
                    final int folderTree = rs.getInt(9);
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;

                    stmt = con.prepareStatement("DELETE FROM user_setting_server WHERE cid=? AND user=? AND ?=HEX(uuid)");
                    stmt.setInt(1, dup.cid);
                    stmt.setInt(2, dup.user);
                    stmt.setString(3, UUIDs.getUnformattedString(dup.uuid));
                    stmt.executeUpdate();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;

                    stmt = con.prepareStatement("INSERT INTO user_setting_server (cid,user,contact_collect_folder,contact_collect_enabled,defaultStatusPrivate,defaultStatusPublic,contactCollectOnMailAccess,contactCollectOnMailTransport,folderTree,uuid) VALUES (?,?,?,?,?,?,?,?,?,UNHEX(?))");
                    stmt.setInt(1, cid);
                    stmt.setInt(2, user);
                    stmt.setInt(3, contact_collect_folder);
                    stmt.setInt(4, contact_collect_enabled);
                    stmt.setInt(5, defaultStatusPrivate);
                    stmt.setInt(6, defaultStatusPublic);
                    stmt.setInt(7, contactCollectOnMailAccess);
                    stmt.setInt(8, contactCollectOnMailTransport);
                    stmt.setInt(9, folderTree);
                    stmt.setString(10, UUIDs.getUnformattedString(dup.uuid));
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
        return new String[] { UserSettingServerAddUuidUpdateTask.class.getName() };
    }

    private void setUUID(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        int oldPos, newPos;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, contact_collect_folder, contact_collect_enabled, defaultStatusPrivate, defaultStatusPublic, contactCollectOnMailTransport, contactCollectOnMailAccess, folderTree FROM user_setting_server WHERE uuid IS NULL FOR UPDATE");
            rs = stmt.executeQuery();
            while (rs.next()) {
                PreparedStatement stmt2 = null;
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("UPDATE user_setting_server SET uuid = ? WHERE cid ");
                    oldPos = 1;
                    int cid = rs.getInt(oldPos++);
                    boolean cidNull = rs.wasNull();
                    if (cidNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND user ");
                    int user = rs.getInt(oldPos++);
                    boolean userNull = rs.wasNull();
                    if (userNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND contact_collect_folder ");
                    int contactCollectFolder = rs.getInt(oldPos++);
                    boolean contactCollectFolderNull = rs.wasNull();
                    if (contactCollectFolderNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND contact_collect_enabled ");
                    boolean contactCollectEnabled = rs.getBoolean(oldPos++);
                    boolean contactCollectEnabledNull = rs.wasNull();
                    if (contactCollectEnabledNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND defaultStatusPrivate ");
                    int defaultStatusPrivate = rs.getInt(oldPos++);
                    boolean defaultStatusPrivateNull = rs.wasNull();
                    if (defaultStatusPrivateNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND defaultStatusPublic ");
                    int defaultStatusPublic = rs.getInt(oldPos++);
                    boolean defaultStatusPublicNull = rs.wasNull();
                    if (defaultStatusPublicNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND contactCollectOnMailTransport ");
                    boolean contactCollectOnMailTransport = rs.getBoolean(oldPos++);
                    boolean contactCollectOnMailTransportNull = rs.wasNull();
                    if (contactCollectOnMailTransportNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND contactCollectOnMailAccess ");
                    boolean contactCollectOnMailAccess = rs.getBoolean(oldPos++);
                    boolean contactCollectOnMailAccessNull = rs.wasNull();
                    if (contactCollectOnMailAccessNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND folderTree ");
                    int folderTree = rs.getInt(oldPos++);
                    boolean folderTreeNull = rs.wasNull();
                    if (folderTreeNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    stmt2 = con.prepareStatement(sb.toString());
                    newPos = 1;
                    UUID uuid = UUID.randomUUID();
                    stmt2.setBytes(newPos++, UUIDs.toByteArray(uuid));
                    if (!cidNull) {
                        stmt2.setInt(newPos++, cid);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
                    if (!userNull) {
                        stmt2.setInt(newPos++, user);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
                    if (!contactCollectFolderNull) {
                        stmt2.setInt(newPos++, contactCollectFolder);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
                    if (!contactCollectEnabledNull) {
                        stmt2.setBoolean(newPos++, contactCollectEnabled);
                    } else {
                        stmt2.setNull(newPos++, Types.BOOLEAN);
                    }
                    if (!defaultStatusPrivateNull) {
                        stmt2.setInt(newPos++, defaultStatusPrivate);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
                    if (!defaultStatusPublicNull) {
                        stmt2.setInt(newPos++, defaultStatusPublic);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
                    if (!contactCollectOnMailTransportNull) {
                        stmt2.setBoolean(newPos++, contactCollectOnMailTransport);
                    } else {
                        stmt2.setNull(newPos++, Types.BOOLEAN);
                    }
                    if (!contactCollectOnMailAccessNull) {
                        stmt2.setBoolean(newPos++, contactCollectOnMailAccess);
                    } else {
                        stmt2.setNull(newPos++, Types.BOOLEAN);
                    }
                    if (!folderTreeNull) {
                        stmt2.setInt(newPos++, folderTree);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
                    stmt2.execute();
                } finally {
                    DBUtils.closeSQLStuff(stmt2);
                }
            }
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

}
