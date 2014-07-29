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

package com.openexchange.flywaytest.migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;


/**
 * {@link V1_2_2__FillUUID}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.1
 */
public class V1_2_2__FillUUID implements JdbcMigration {

    @Override
    public void migrate(Connection connection) throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = null;
        PreparedStatement pStmt = null;
        try {
            rs = stmt.executeQuery("SELECT cid, uid, firstname, lastname, email FROM blablubber ORDER BY cid ASC FOR UPDATE");
            pStmt = connection.prepareStatement("UPDATE blablubber SET uuid = ? WHERE cid = ? AND uid = ?");
            while (rs.next()) {
                int i = 0;
                int cid = rs.getInt("cid");
                int uid = rs.getInt("uid");
                UUID uuid = UUID.randomUUID();
                pStmt.setString(++i, uuid.toString());
                pStmt.setInt(++i, cid);
                pStmt.setInt(++i, uid);
                pStmt.addBatch();
            }
            pStmt.executeBatch();
        } finally {
            if (null != pStmt) {
                pStmt.close();
            }
            if (null != rs) {
                rs.close();
            }
            stmt.close();
        }
    }

}
