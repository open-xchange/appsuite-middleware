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
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.sql.DBUtils;
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
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
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
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
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
