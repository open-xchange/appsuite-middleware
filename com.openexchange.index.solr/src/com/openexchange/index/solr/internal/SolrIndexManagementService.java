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

package com.openexchange.index.solr.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexManagementService;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.solr.SolrIndexEventProperties;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link SolrIndexManagementService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrIndexManagementService implements IndexManagementService {

    private static final String PROPERTY = "com.openexchange.index.lockedIndices";


    @Override
    public void lockIndex(int contextId, int userId, int module) throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection con = dbService.getWritable(contextId);
        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        try {
            DBUtils.startTransaction(con);
            sstmt = con.prepareStatement("SELECT value FROM user_attribute WHERE cid = ? AND id = ? AND name = ? FOR UPDATE");
            sstmt.setInt(1, contextId);
            sstmt.setInt(2, userId);
            sstmt.setString(3, PROPERTY);

            rs = sstmt.executeQuery();
            if (rs.next()) {
                String value = rs.getString(1);
                String[] split = value.split(",");
                StringBuilder sb = new StringBuilder();
                for (String m : split) {
                    m = m.trim();
                    if (String.valueOf(module).equals(m)) {
                        continue;
                    }
                    sb.append(m);
                    sb.append(',');
                }
                sb.append(module);

                ustmt = con.prepareStatement("UPDATE user_attribute SET value = ? WHERE cid = ? AND id = ? AND name = ?");
                ustmt.setString(1, sb.toString());
                ustmt.setInt(2, contextId);
                ustmt.setInt(3, userId);
                ustmt.setString(4, PROPERTY);
                ustmt.executeUpdate();
            } else {
                ustmt = con.prepareStatement("INSERT INTO user_attribute (cid, id, name, value, uuid) VALUES (?, ?, ?, ?, ?)");
                ustmt.setInt(1, contextId);
                ustmt.setInt(2, userId);
                ustmt.setString(3, PROPERTY);
                ustmt.setString(4, String.valueOf(module));
                ustmt.setBytes(5, UUIDs.toByteArray(UUID.randomUUID()));

                ustmt.executeUpdate();
            }

            con.commit();
        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, sstmt);
            DBUtils.closeSQLStuff(ustmt);
            DBUtils.autocommit(con);
            dbService.backWritable(contextId, con);
        }

        Map<String, Integer> properties = new HashMap<String, Integer>();
        properties.put(SolrIndexEventProperties.PROP_CONTEXT_ID, new Integer(contextId));
        properties.put(SolrIndexEventProperties.PROP_USER_ID, new Integer(userId));
        properties.put(SolrIndexEventProperties.PROP_MODULE, new Integer(module));
        Event event = new Event(SolrIndexEventProperties.TOPIC_LOCK_INDEX, properties);
        EventAdmin eventAdmin = Services.getService(EventAdmin.class);
        eventAdmin.sendEvent(event);
    }

    @Override
    public void unlockIndex(int contextId, int userId, int module) throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection con = dbService.getWritable(contextId);
        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        try {
            DBUtils.startTransaction(con);
            sstmt = con.prepareStatement("SELECT value FROM user_attribute WHERE cid = ? AND id = ? AND name = ? FOR UPDATE");
            sstmt.setInt(1, contextId);
            sstmt.setInt(2, userId);
            sstmt.setString(3, PROPERTY);

            rs = sstmt.executeQuery();
            if (rs.next()) {
                String value = rs.getString(1);
                String[] split = Strings.splitByComma(value);
                final String sModule = String.valueOf(module);
                StringBuilder sb = new StringBuilder();
                for (String m : split) {
                    m = m.trim();
                    if (sModule.equals(m)) {
                        continue;
                    }
                    sb.append(m);
                    sb.append(',');
                }

                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }

                ustmt = con.prepareStatement("UPDATE user_attribute SET value = ? WHERE cid = ? AND id = ? AND name = ?");
                ustmt.setString(1, sb.toString());
                ustmt.setInt(2, contextId);
                ustmt.setInt(3, userId);
                ustmt.setString(4, PROPERTY);
                ustmt.executeUpdate();
            }

            con.commit();
        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, sstmt);
            DBUtils.closeSQLStuff(ustmt);
            DBUtils.autocommit(con);
            dbService.backWritable(contextId, con);
        }
    }

    @Override
    public boolean isLocked(int contextId, int userId, int module) throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection con = dbService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT value FROM user_attribute WHERE cid = ? AND id = ? AND name = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, PROPERTY);

            rs = stmt.executeQuery();
            if (rs.next()) {
                String value = rs.getString(1);
                String[] split = value.split(",");
                for (String m : split) {
                    m = m.trim();
                    if (String.valueOf(module).equals(m)) {
                        return true;
                    }
                }
            }

            return false;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            dbService.backReadOnly(contextId, con);
        }
    }

}
