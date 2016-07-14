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
 * {@link GenconfAttributesStringsAddPrimaryKey}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class GenconfAttributesStringsAddPrimaryKey extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link GenconfAttributesStringsAddPrimaryKey}.
     */
    public GenconfAttributesStringsAddPrimaryKey() {
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
            Tools.modifyColumns(con, "genconf_attributes_strings", column);

            dropDuplicates(con);

            Tools.createPrimaryKeyIfAbsent(con, "genconf_attributes_strings", new String[] { "cid", "id", column.name });
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

    @Override
    public String[] getDependencies() {
        return new String[] { GenconfAttributesStringsAddUuidUpdateTask.class.getName() };
    }

    private void dropDuplicates(final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, id, HEX(uuid) FROM genconf_attributes_strings GROUP BY cid, id, uuid HAVING count(*) > 1");
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }

            class Dup {

                final UUID uuid;
                final int cid;
                final int id;

                Dup(int cid, int id, UUID uuid) {
                    super();
                    this.cid = cid;
                    this.id = id;
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
                stmt = con.prepareStatement("SELECT cid, id, name, value, widget FROM genconf_attributes_strings WHERE cid=? AND id=? AND ?=HEX(uuid)");
                stmt.setInt(1, dup.cid);
                stmt.setInt(2, dup.id);
                stmt.setString(3, UUIDs.getUnformattedString(dup.uuid));
                rs = stmt.executeQuery();

                if (rs.next()) {
                    int cid = rs.getInt(1);
                    int id = rs.getInt(2);
                    String name = rs.getString(3);
                    String value = rs.getString(4);
                    String widget = rs.getString(5);

                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;

                    stmt = con.prepareStatement("DELETE FROM genconf_attributes_strings WHERE cid=? AND id=? AND ?=HEX(uuid)");
                    stmt.setInt(1, dup.cid);
                    stmt.setInt(2, dup.id);
                    stmt.setString(3, UUIDs.getUnformattedString(dup.uuid));
                    stmt.executeUpdate();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;

                    stmt = con.prepareStatement("INSERT INTO genconf_attributes_strings (cid,id,name,value,widget,uuid) VALUES (?,?,?,?,?,UNHEX(?))");
                    stmt.setInt(1, cid);
                    stmt.setInt(2, id);
                    stmt.setString(3, name);
                    stmt.setString(4, value);
                    stmt.setString(5, widget);
                    stmt.setString(6, UUIDs.getUnformattedString(dup.uuid));
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

    private void setUUID(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        int oldPos, newPos;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, id, name, value FROM genconf_attributes_strings WHERE uuid IS NULL FOR UPDATE");
            rs = stmt.executeQuery();
            while (rs.next()) {
                PreparedStatement stmt2 = null;
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("UPDATE genconf_attributes_strings SET uuid = ? WHERE cid ");
                    oldPos = 1;
                    int cid = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND id ");
                    int id = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND name ");
                    String name = rs.getString(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND value ");
                    String value = rs.getString(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    stmt2 = con.prepareStatement(sb.toString());
                    newPos = 1;
                    UUID uuid = UUID.randomUUID();
                    stmt2.setBytes(newPos++, UUIDs.toByteArray(uuid));
                    stmt2.setInt(newPos++, cid);
                    stmt2.setInt(newPos++, id);
                    stmt2.setString(newPos++, name);
                    stmt2.setString(newPos++, value);
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
