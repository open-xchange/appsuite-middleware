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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.favfolder.internal;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DeleteFavFolderTreePerformer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeleteFavFolderTreePerformer extends AbstractFavFolderPerformer {

    /**
     * Initializes a new {@link DeleteFavFolderTreePerformer}.
     */
    public DeleteFavFolderTreePerformer(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    /**
     * Deletes specified favorite folder tree.
     *
     * @param treeId The tree identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If deletion fails
     */
    public void deleteFavFolderTree(final int treeId, final int userId, final int contextId) throws OXException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            con.setAutoCommit(false); // BEGIN
            /*
             * Clean backup tables
             */
            stmt = con.prepareStatement("DELETE FROM virtualBackupSubscription WHERE cid = ? AND tree = ? AND user = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId); // cid
            stmt.setInt(pos++, treeId); // tree
            stmt.setInt(pos, userId); // user
            stmt.executeUpdate();
            closeSQLStuff(stmt);

            stmt = con.prepareStatement("DELETE FROM virtualBackupPermission WHERE cid = ? AND tree = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId); // cid
            stmt.setInt(pos++, treeId); // tree
            stmt.setInt(pos, userId); // user
            stmt.executeUpdate();
            closeSQLStuff(stmt);

            stmt = con.prepareStatement("DELETE FROM virtualBackupTree WHERE cid = ? AND tree = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId); // cid
            stmt.setInt(pos++, treeId); // tree
            stmt.setInt(pos, userId); // user
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            /*
             * Copy to backup tables "INSERT INTO del_oxfolder_tree SELECT * FROM oxfolder_tree WHERE cid = ? AND fuid = ?";
             */
            stmt = con.prepareStatement("INSERT INTO virtualBackupTree SELECT * FROM virtualTree WHERE cid = ? AND tree = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId); // cid
            stmt.setInt(pos++, treeId); // tree
            stmt.setInt(pos, userId); // user
            stmt.executeUpdate();
            closeSQLStuff(stmt);

            stmt =
                con.prepareStatement("INSERT INTO virtualBackupPermission SELECT * FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId); // cid
            stmt.setInt(pos++, treeId); // tree
            stmt.setInt(pos, userId); // user
            stmt.executeUpdate();
            closeSQLStuff(stmt);

            stmt =
                con.prepareStatement("INSERT INTO virtualBackupSubscription SELECT * FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId); // cid
            stmt.setInt(pos++, treeId); // tree
            stmt.setInt(pos, userId); // user
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            /*
             * Cleanse from working tables
             */
            stmt = con.prepareStatement("DELETE FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId); // cid
            stmt.setInt(pos++, treeId); // tree
            stmt.setInt(pos, userId); // user
            stmt.executeUpdate();
            closeSQLStuff(stmt);

            stmt = con.prepareStatement("DELETE FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId); // cid
            stmt.setInt(pos++, treeId); // tree
            stmt.setInt(pos, userId); // user
            stmt.executeUpdate();
            closeSQLStuff(stmt);

            stmt = con.prepareStatement("DELETE FROM virtualTree WHERE cid = ? AND tree = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId); // cid
            stmt.setInt(pos++, treeId); // tree
            stmt.setInt(pos, userId); // user
            stmt.executeUpdate();
            /*
             * Finally commit
             */
            con.commit(); // COMMIT
        } catch (final SQLException e) {
            rollback(con);
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            rollback(con);
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }
    }

}
