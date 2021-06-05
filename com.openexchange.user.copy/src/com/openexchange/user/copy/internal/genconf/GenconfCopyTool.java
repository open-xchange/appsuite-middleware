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

package com.openexchange.user.copy.internal.genconf;

import static com.openexchange.user.copy.internal.CopyTools.getIntOrNegative;
import static com.openexchange.user.copy.internal.CopyTools.setIntOrNull;
import static com.openexchange.user.copy.internal.CopyTools.setStringOrNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.user.copy.UserCopyExceptionCodes;


/**
 * {@link GenconfCopyTool}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class GenconfCopyTool {

    private static final String SELECT_ATTRIBUTES =
        "SELECT " +
            "name, value " +
        "FROM " +
            "genconf_attributes_#TYPE# " +
        "WHERE " +
            "cid = ? " +
        "AND " +
            "id = ?";

    private static final String INSERT_ATTRIBUTES =
        "INSERT INTO " +
            "genconf_attributes_#TYPE# " +
            "(cid, id, name, value, uuid) " +
        "VALUES " +
            "(?, ?, ?, ?, ?)";


    public static void writeAttributesToDB(final List<ConfAttribute> attributes, final Connection con, final int id, final int cid, final int type) throws OXException {
        final String sql = replaceTableInAttributeStatement(INSERT_ATTRIBUTES, type);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql);
            for (final ConfAttribute attribute : attributes) {
                int i = 1;
                stmt.setInt(i++, cid);
                stmt.setInt(i++, id);
                setStringOrNull(i++, stmt, attribute.getName());
                if (type == ConfAttribute.BOOL) {
                    setIntOrNull(i++, stmt, Integer.parseInt(attribute.getValue()));
                } else {
                    setStringOrNull(i++, stmt, attribute.getValue());
                }
                stmt.setBytes(i++, UUIDs.toByteArray(UUID.randomUUID()));

                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    public static List<ConfAttribute> loadAttributesFromDB(final Connection con, final int id, final int cid, final int type) throws OXException {
        final List<ConfAttribute> attributes = new ArrayList<ConfAttribute>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final boolean isBool = (type == ConfAttribute.BOOL);
            final String sql = replaceTableInAttributeStatement(SELECT_ATTRIBUTES, type);

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, cid);
            stmt.setInt(2, id);

            rs = stmt.executeQuery();
            while (rs.next()) {
                final ConfAttribute attribute = new ConfAttribute(type);
                attribute.setName(rs.getString(1));
                if (isBool) {
                    attribute.setValue(String.valueOf(getIntOrNegative(2, rs)));
                } else {
                    attribute.setValue(rs.getString(2));
                }

                attributes.add(attribute);
            }
        } catch (SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }

        return attributes;
    }

    private static String replaceTableInAttributeStatement(final String statement, final int type) {
        final boolean isBool = (type == ConfAttribute.BOOL);
        final String sql;
        if (isBool) {
            sql = statement.replaceFirst("#TYPE#", "bools");
        } else {
            sql = statement.replaceFirst("#TYPE#", "strings");
        }

        return sql;
    }

}
