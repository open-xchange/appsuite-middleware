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

package com.openexchange.report.internal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;


public class Tools {

    public static final Map<String, Integer> getAllSchemata(final org.slf4j.Logger logger) throws SQLException, OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        final Map<String, Integer> schemaMap = new LinkedHashMap<String, Integer>(50); // Keep insertion order
        {
            final Connection readcon;
            try {
                readcon = dbService.getReadOnly();
            } catch (final OXException e) {
                logger.error("", e);
                throw e;
            }
            /*
             * Get all schemas and put them into a map.
             */
            Statement statement = null;
            ResultSet rs = null;
            try {
                statement = readcon.createStatement();
                rs = statement.executeQuery("SELECT read_db_pool_id, db_schema FROM context_server2db_pool GROUP BY db_schema");
                while (rs.next()) {
                    schemaMap.put(rs.getString(2), Integer.valueOf(rs.getInt(1)));
                }
            } catch (final SQLException e) {
                logger.error("", e);
                throw e;
            } finally {
                DBUtils.closeSQLStuff(rs, statement);
                dbService.backReadOnly(readcon);
            }
        }
        return schemaMap;
    }
}
