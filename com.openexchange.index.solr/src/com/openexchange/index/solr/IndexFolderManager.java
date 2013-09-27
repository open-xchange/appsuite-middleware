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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.index.solr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link IndexFolderManager}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexFolderManager {

    public static boolean isIndexed(int contextId, int userId, int module, String account, String folder) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        try {
            return isIndexed(con, contextId, userId, module, account, folder);
        } finally {
            if (con != null) {
                dbService.backWritable(contextId, con);
            }
        }
    }

    private static boolean isIndexed(Connection con, int contextId, int userId, int module, String account, String folder) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT indexed FROM indexedFolders WHERE cid = ? AND uid = ? AND module = ? AND account = ? AND folder = ?");
            int i = 0;
            stmt.setInt(++i, contextId);
            stmt.setInt(++i, userId);
            stmt.setInt(++i, module);
            stmt.setString(++i, account);
            stmt.setString(++i, folder);

            rs = stmt.executeQuery();
            boolean isIndexed;
            if (rs.next()) {
                isIndexed = rs.getBoolean(1);
            } else {
                isIndexed = false;
                if (folder != null) {
                    createFolderEntry(con, contextId, userId, module, account, folder);
                }
            }

            return isIndexed;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    public static void setIndexed(int contextId, int userId, int module, String account, String folder) throws OXException {
        updateIndexed(true, contextId, userId, module, account, folder);
    }

    public static void unsetIndexed(int contextId, int userId, int module, String account, String folder) throws OXException {
        updateIndexed(false, contextId, userId, module, account, folder);
    }

    private static void updateIndexed(boolean isIndexed, int contextId, int userId, int module, String account, String folder) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE indexedFolders SET indexed = ? WHERE cid = ? AND uid = ? AND module = ? AND account = ? AND folder = ?");
            int i = 0;
            stmt.setBoolean(++i, isIndexed);
            stmt.setInt(++i, contextId);
            stmt.setInt(++i, userId);
            stmt.setInt(++i, module);
            stmt.setString(++i, account);
            stmt.setString(++i, folder);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (con != null) {
                dbService.backWritable(contextId, con);
            }
        }
    }

    public static boolean isLocked(int contextId, int userId, int module, String account, String folder) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        try {
            return isLocked(con, contextId, userId, module, account, folder);
        } finally {
            if (con != null) {
                dbService.backWritableAfterReading(contextId, con);
            }
        }

    }

    private static boolean isLocked(Connection con, int contextId, int userId, int module, String account, String folder) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT locked FROM indexedFolders WHERE cid = ? AND uid = ? AND module = ? AND account = ? AND folder = ?");
            int i = 0;
            stmt.setInt(++i, contextId);
            stmt.setInt(++i, userId);
            stmt.setInt(++i, module);
            stmt.setString(++i, account);
            stmt.setString(++i, folder);

            rs = stmt.executeQuery();
            boolean isLocked;
            if (rs.next()) {
                isLocked = rs.getBoolean(1);
            } else {
                isLocked = false;
                createFolderEntry(con, contextId, userId, module, account, folder);
            }

            return isLocked;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    public static boolean lock(int contextId, int userId, int module, String account, String folder) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        try {
            return lock(con, contextId, userId, module, account, folder);
        } finally {
            if (con != null) {
                dbService.backWritable(contextId, con);
            }
        }
    }

    private static boolean lock(Connection con, int contextId, int userId, int module, String account, String folder) throws OXException {
        PreparedStatement stmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        try {
            DBUtils.startTransaction(con);
            stmt = con.prepareStatement("SELECT locked FROM indexedFolders WHERE cid = ? AND uid = ? AND module = ? AND account = ? AND folder = ? FOR UPDATE");
            int i = 0;
            stmt.setInt(++i, contextId);
            stmt.setInt(++i, userId);
            stmt.setInt(++i, module);
            stmt.setString(++i, account);
            stmt.setString(++i, folder);

            rs = stmt.executeQuery();
            if (rs.next()) {
                boolean locked = rs.getBoolean(1);
                if (locked) {
                    con.commit();
                    return false;
                } else {
                    ustmt = con.prepareStatement("UPDATE indexedFolders SET locked = ? WHERE cid = ? AND uid = ? AND module = ? AND account = ? AND folder = ? AND locked = ?");
                    int j = 0;
                    ustmt.setBoolean(++j, true);
                    ustmt.setInt(++j, contextId);
                    ustmt.setInt(++j, userId);
                    ustmt.setInt(++j, module);
                    ustmt.setString(++j, account);
                    ustmt.setString(++j, folder);
                    ustmt.setBoolean(++j, false);
                    int rows = ustmt.executeUpdate();
                    con.commit();
                    return rows > 0;
                }
            } else {
                con.commit();
            }
        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            DBUtils.closeSQLStuff(rs, stmt);
            DBUtils.closeSQLStuff(ustmt);
        }

        createFolderEntry(con, contextId, userId, module, account, folder);
        return lock(con, contextId, userId, module, account, folder);
    }

    public static boolean unlock(int contextId, int userId, int module, String account, String folder) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        PreparedStatement stmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        try {
            DBUtils.startTransaction(con);
            stmt = con.prepareStatement("SELECT locked FROM indexedFolders WHERE cid = ? AND uid = ? AND module = ? AND account = ? AND folder = ? FOR UPDATE");
            int i = 0;
            stmt.setInt(++i, contextId);
            stmt.setInt(++i, userId);
            stmt.setInt(++i, module);
            stmt.setString(++i, account);
            stmt.setString(++i, folder);

            rs = stmt.executeQuery();
            if (rs.next()) {
                boolean locked = rs.getBoolean(1);
                if (locked) {
                    ustmt = con.prepareStatement("UPDATE indexedFolders SET locked = ? WHERE cid = ? AND uid = ? AND module = ? AND account = ? AND folder = ? AND locked = ?");
                    i = 0;
                    ustmt.setBoolean(++i, false);
                    ustmt.setInt(++i, contextId);
                    ustmt.setInt(++i, userId);
                    ustmt.setInt(++i, module);
                    ustmt.setString(++i, account);
                    ustmt.setString(++i, folder);
                    ustmt.setBoolean(++i, true);
                    int rows = ustmt.executeUpdate();

                    con.commit();
                    return rows > 0;
                } else {
                    con.commit();
                    return false;
                }
            } else {
                throw IndexExceptionCodes.MISSING_FOLDER_ENTRY.create(folder, account);
            }
        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            DBUtils.autocommit(con);
            DBUtils.closeSQLStuff(ustmt);
            if (con != null) {
                dbService.backWritable(contextId, con);
            }
        }
    }

    public static Map<String, Boolean> getIndexedFolders(int contextId, int userId, int module, String account) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, Boolean> results = new HashMap<String, Boolean>();
        try {
            stmt = con.prepareStatement("SELECT folder, indexed FROM indexedFolders WHERE cid = ? AND uid = ? AND module = ? AND account = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, module);
            stmt.setString(4, account);

            rs = stmt.executeQuery();
            while (rs.next()) {
                String folder = rs.getString(1);
                boolean indexed = rs.getBoolean(2);
                results.put(folder, indexed);
            }

            return results;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (con != null) {
                dbService.backReadOnly(contextId, con);
            }
        }
    }

    public static boolean setTimestamp(int contextId, int userId, int module, String account, String folder, long timestamp) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE indexedFolders SET timestamp = ? WHERE cid = ? AND uid = ? AND module = ? AND account = ? AND folder = ?");
            int i = 0;
            stmt.setLong(++i, timestamp);
            stmt.setInt(++i, contextId);
            stmt.setInt(++i, userId);
            stmt.setInt(++i, module);
            stmt.setString(++i, account);
            stmt.setString(++i, folder);
            int rows = stmt.executeUpdate();
            if (rows == 1) {
                return true;
            }

            return false;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (con != null) {
                dbService.backWritable(contextId, con);
            }
        }
    }

    public static long getTimestamp(int contextId, int userId, int module, String account, String folder) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        try {
            return getTimestamp(con, contextId, userId, module, account, folder);
        } finally {
            if (con != null) {
                dbService.backWritableAfterReading(contextId, con);
            }
        }
    }

    private static long getTimestamp(Connection con, int contextId, int userId, int module, String account, String folder) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT timestamp FROM indexedFolders WHERE cid = ? AND uid = ? AND module = ? AND account = ? AND folder = ?");
            int i = 0;
            stmt.setInt(++i, contextId);
            stmt.setInt(++i, userId);
            stmt.setInt(++i, module);
            stmt.setString(++i, account);
            stmt.setString(++i, folder);

            rs = stmt.executeQuery();
            long timestamp;
            if (rs.next()) {
                timestamp = rs.getLong(1);
            } else {
                createFolderEntry(con, contextId, userId, module, account, folder);
                return getTimestamp(con, contextId, userId, module, account, folder);
            }

            return timestamp;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    public static boolean deleteFolderEntry(int contextId, int userId, int module, String account, String folder) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM indexedFolders WHERE cid = ? AND uid = ? AND module = ? AND account = ? AND folder = ?");
            int i = 0;
            stmt.setInt(++i, contextId);
            stmt.setInt(++i, userId);
            stmt.setInt(++i, module);
            stmt.setString(++i, account);
            stmt.setString(++i, folder);
            int rows = stmt.executeUpdate();
            if (rows == 1) {
                return true;
            }

            return false;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (con != null) {
                dbService.backWritable(contextId, con);
            }
        }
    }

    public static List<String> getElapsedFolders(int contextId, int userId, int module, String account, long timestamp) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getReadOnly(contextId);
        List<String> folders = new ArrayList<String>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT folder, timestamp FROM indexedFolders WHERE cid = ? AND uid = ? AND module = ? AND account = ?");
            int i = 0;
            stmt.setInt(++i, contextId);
            stmt.setInt(++i, userId);
            stmt.setInt(++i, module);
            stmt.setString(++i, account);

            rs = stmt.executeQuery();
            while (rs.next()) {
                String folder = rs.getString(1);
                long last = rs.getLong(2);
                if (last < timestamp) {
                    folders.add(folder);
                }
            }

            return folders;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (con != null) {
                dbService.backReadOnly(contextId, con);
            }
        }
    }

    public static boolean createFolderEntry(int contextId, int userId, int module, String account, String folder) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        try {
            return createFolderEntry(con, contextId, userId, module, account, folder);
        } finally {
            if (con != null) {
                dbService.backWritable(contextId, con);
            }
        }
    }

    private static boolean createFolderEntry(Connection con, int contextId, int userId, int module, String account, String folder) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO indexedFolders (cid, uid, module, account, folder, timestamp, locked, indexed) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE cid = cid");
            int i = 0;
            stmt.setInt(++i, contextId);
            stmt.setInt(++i, userId);
            stmt.setInt(++i, module);
            stmt.setString(++i, account);
            stmt.setString(++i, folder);
            stmt.setLong(++i, System.currentTimeMillis());
            stmt.setBoolean(++i, false);
            stmt.setBoolean(++i, false);

            int rows = stmt.executeUpdate();
            if (rows == 1) {
                return true;
            }

            return false;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static DatabaseService getDbService() throws OXException {
        final DatabaseService dbService = Services.getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }

        return dbService;
    }

}
