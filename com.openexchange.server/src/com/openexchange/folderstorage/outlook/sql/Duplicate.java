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

package com.openexchange.folderstorage.outlook.sql;

import static com.openexchange.folderstorage.outlook.sql.Utility.getDatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import com.davekoelle.AlphanumComparator;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Duplicate}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Duplicate {

    /**
     * Initializes a new {@link Duplicate}.
     */
    private Duplicate() {
        super();
    }

    /**
     * Detects & deletes duplicates from virtual table.
     * 
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @return The name-2-IDs mapping
     * @throws OXException
     */
    public static Map<String, List<String>> lookupDuplicateNames(final int cid, final int tree, final int user) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        // Get a connection
        final Connection con;
        try {
            con = databaseService.getWritable(cid);
        } catch (final OXException e) {
            throw e;
        }
        try {
            con.setAutoCommit(false); // BEGIN
            final Map<String, List<String>> ret = lookupDuplicateNames(cid, tree, user, con);
            con.commit(); // COMMIT
            return ret;
        } catch (final SQLException e) {
            DBUtils.rollback(con); // ROLLBACK
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            DBUtils.rollback(con); // ROLLBACK
            throw e;
        } catch (final Exception e) {
            DBUtils.rollback(con); // ROLLBACK
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            databaseService.backWritable(cid, con);
        }
    }

    /**
     * Detects & deletes duplicates from virtual table.
     * 
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param con The connection
     * @return The name-2-IDs mapping
     * @throws OXException
     */
    public static Map<String, List<String>> lookupDuplicateNames(final int cid, final int tree, final int user, final Connection con) throws OXException {
        if (null == con) {
            return lookupDuplicateNames(cid, tree, user);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int pos;
            final Map<String, String> name2parent;
            try {
                /*
                 * Detect possible duplicates
                 */
                stmt =
                    con.prepareStatement("SELECT name, COUNT(name), parentId FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? GROUP BY parentId, name");
                pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos, user);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    return java.util.Collections.emptyMap();
                }
                name2parent = new HashMap<String, String>();
                do {
                    if (rs.getInt(2) > 1) {
                        name2parent.put(rs.getString(1), rs.getString(3));
                    }
                } while (rs.next());
                if (name2parent.isEmpty()) {
                    return java.util.Collections.emptyMap();
                }
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
            /*
             * Get folder identifiers
             */
            final Map<String, List<String>> name2ids = new HashMap<String, List<String>>();
            for (final Entry<String, String> entry : name2parent.entrySet()) {
                try {
                    stmt =
                        con.prepareStatement("SELECT folderId FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND name = ? AND parentId = ?");
                    final String name = entry.getKey();
                    pos = 1;
                    stmt.setInt(pos++, cid);
                    stmt.setInt(pos++, tree);
                    stmt.setInt(pos++, user);
                    stmt.setString(pos++, name);
                    stmt.setString(pos++, entry.getValue());
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        final List<String> folderIds = new ArrayList<String>(4);
                        do {
                            folderIds.add(rs.getString(1));
                        } while (rs.next());
                        java.util.Collections.sort(folderIds, new AlphanumComparator(Locale.US));
                        folderIds.remove(0); // Remove first
                        name2ids.put(name, folderIds);
                    }
                } finally {
                    DBUtils.closeSQLStuff(rs, stmt);
                }
            }
            /*
             * Delete duplicates from table
             */
            for (final Entry<String, List<String>> entry : name2ids.entrySet()) {
                final List<String> folderIds = entry.getValue();
                final int sz = folderIds.size();
                for (int i = 0; i < sz; i++) {
                    Delete.deleteFolder(cid, tree, user, folderIds.get(i), false, false, con);
                }
            }
            /*
             * Return
             */
            return name2ids;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

}
