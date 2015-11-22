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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.sql.DBUtils;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * {@link FolderCorrectOwnerTask} - Corrects values in the 'created_from' column for folders nested below/underneath personal 'Trash' folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderCorrectOwnerTask extends UpdateTaskAdapter {

    /**
     * Default constructor.
     */
    public FolderCorrectOwnerTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BACKGROUND);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(FolderCorrectOwnerTask.class);
        log.info("Performing update task {}", FolderCorrectOwnerTask.class.getSimpleName());

        Connection connnection = Database.getNoTimeout(params.getContextId(), true);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connnection.setAutoCommit(false);

            stmt = connnection.prepareStatement("SELECT cid, id FROM user ORDER BY cid, id");
            rs = stmt.executeQuery();

            class UserInfo {
                final int cid;
                final int id;

                UserInfo(int id, int cid) {
                    super();
                    this.id = id;
                    this.cid = cid;
                }
            }

            List<UserInfo> users = new LinkedList<UserInfo>();
            while (rs.next()) {
                users.add(new UserInfo(rs.getInt(1), rs.getInt(2)));
            }

            DBUtils.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            for (UserInfo userInfo : users) {
                int contextId = userInfo.cid;
                int userId = userInfo.id;


            }




            stmt = connnection.prepareStatement("UPDATE oxfolder_tree SET changing_date=? WHERE changing_date=?;");
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setLong(2, Long.MAX_VALUE);
            int corrected = stmt.executeUpdate();
            log.info("Corrected {} rows in 'oxfolder_tree'.", corrected);
            connnection.commit();
        } catch (SQLException e) {
            rollback(connnection);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            rollback(connnection);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            autocommit(connnection);
            Database.backNoTimeout(params.getContextId(), true, connnection);
        }
        log.info("{} successfully performed.", FolderCorrectOwnerTask.class.getSimpleName());
    }

    private List<int[]> getUsers(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, id FROM user ORDER BY cid, id");
            rs = stmt.executeQuery();

            List<int[]> users = new LinkedList<int[]>();
            while (rs.next()) {
                users.add(new int[] {rs.getInt(1), rs.getInt(2)});
            }
            return users;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private TIntIntMap getTrashFolders(int userId, int contextId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid=? AND type=? AND default_flag=1 AND created_from=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, FolderObject.TRASH);
            stmt.setInt(3, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }

            int trashId = rs.getInt(1);
            DBUtils.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            TIntIntMap fuids = new TIntIntHashMap();
            collectTrashFolders(trashId, fuids, userId, contextId, con);
            return fuids;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private void collectTrashFolders(int parentTrashId, TIntIntMap fuids, int userId, int contextId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fuid, created_from FROM oxfolder_tree WHERE cid=? AND parent=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, parentTrashId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }

            TIntList children = new TIntArrayList(8);
            do {
                int subfolderId = rs.getInt(1);
                if (!fuids.containsKey(subfolderId)) {
                    // Not present before, so examine child's sub-folders, too
                    children.add(subfolderId);

                    int createdFrom = rs.getInt(2);
                    if (createdFrom != userId) {
                        fuids.put(subfolderId, createdFrom);
                    }
                }
            } while (rs.next());
            DBUtils.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            for (int childId : children.toArray()) {
                collectTrashFolders(childId, fuids, userId, contextId, con);
            }
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private void adjustTrashOwnershipFor(int folderId, int oldOwner, int userId, int contextId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE oxfolder_tree SET created_from = ? WHERE cid=? AND fuid=?");
            stmt.setInt(1, userId);
            stmt.setInt(2, contextId);
            stmt.setInt(3, folderId);
            stmt.executeUpdate();

           // VersionControlUtil.changeFileStoreLocationsIfNecessary(oldOwner, userId, folderId, Con, con);

        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}
