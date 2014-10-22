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

package com.openexchange.tools.oxfolder;

import gnu.trove.list.TIntList;
import gnu.trove.procedure.TIntProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Autoboxing;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.UserService;


/**
 * {@link OXFolderDependentDeleter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OXFolderDependentDeleter {

    /**
     * Deletes any existing dependent entities (e.g. subscriptions, publications, shares) for the supplied folder ID.
     *
     * @param con A "write" connection to the database
     * @param session The affected session
     * @param folder The deleted folder
     * @param handDown <code>true</code> to also remove the subscriptions and publications of any nested subfolder, <code>false</code>,
     *                 otherwise
     * @return The number of removed subscriptions and publications
     * @throws OXException
     */
    public static void folderDeleted(Connection con, Session session, FolderObject folder, boolean handDown) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        Context context = serverSession.getContext();
        TIntList subfolderIDs;
        try {
            subfolderIDs = handDown ? OXFolderSQL.getSubfolderIDs(folder.getObjectID(), con, context) : null;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }

        deletePublicationsAndSubscriptions(con, context, folder, subfolderIDs);
        deleteShares(con, serverSession, folder, subfolderIDs);
    }

    private static void deleteShares(Connection con, ServerSession session, FolderObject folder, TIntList subfolderIDs) throws OXException {
        UserService userService = ServerServiceRegistry.getServize(UserService.class, true);
        ShareService shareService = ServerServiceRegistry.getServize(ShareService.class, true);
        session.setParameter(Connection.class.getName(), con);
        try {
            List<OCLPermission> permissions = folder.getPermissions();
            List<Integer> userIDs = new ArrayList<Integer>(permissions.size());
            for (OCLPermission permission : permissions) {
                if (!permission.isGroupPermission()) {
                    userIDs.add(permission.getEntity());
                }
            }

            User[] users = userService.getUser(session.getContext(), Autoboxing.I2i(userIDs));
            List<Integer> guestIDs = new ArrayList<Integer>(permissions.size());
            for (User user : users) {
                if (user.isGuest()) {
                    guestIDs.add(user.getId());
                }
            }

            final int module = folder.getModule();
            final int folderID = folder.getObjectID();
            if (null != subfolderIDs && 0 < subfolderIDs.size()) {
                final List<ShareTarget> targets = new ArrayList<ShareTarget>(subfolderIDs.size() + 1);

                targets.add(new ShareTarget(module, Integer.toString(folderID)));
                subfolderIDs.forEach(new TIntProcedure() {
                    @Override
                    public boolean execute(int subfolderID) {
                        targets.add(new ShareTarget(module, Integer.toString(subfolderID)));
                        return true;
                    }
                });

                shareService.deleteTargets(session, targets, guestIDs);
            } else {
                shareService.deleteTarget(session, new ShareTarget(module, Integer.toString(folderID)), guestIDs);
            }
        } finally {
            session.setParameter(Connection.class.getName(), null);
        }
    }

    private static void deletePublicationsAndSubscriptions(Connection con, Context context, FolderObject folder, TIntList subfolderIDs) throws OXException {
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        try {
            /*
             * prepare clause for folder IDs
             */
            String whereFolderID;
            if (null == subfolderIDs || 0 == subfolderIDs.size()) {
                whereFolderID = "=?;";
            } else {
                StringBuilder StringBuilder = new StringBuilder(" IN (?");
                for (int i = 0; i < subfolderIDs.size(); i++) {
                    StringBuilder.append(",?");
                }
                StringBuilder.append(");");
                whereFolderID = StringBuilder.toString();
            }
            /*
             * delete publications
             */
            stmt1 = con.prepareStatement("DELETE FROM publications WHERE cid=? AND module=? AND entity" + whereFolderID);
            stmt1.setInt(1, context.getContextId());
            int folderID = folder.getObjectID();
            stmt1.setString(2, Module.getModuleString(folder.getModule(), folderID));
            stmt1.setInt(3, folderID);
            if (null != subfolderIDs && 0 < subfolderIDs.size()) {
                for (int i = 0; i < subfolderIDs.size(); i++) {
                    stmt1.setInt(i + 4, subfolderIDs.get(i));
                }
            }
            /*
             * delete subscriptions
             */
            stmt1.executeUpdate();
            stmt2 = con.prepareStatement("DELETE FROM subscriptions WHERE cid=? AND folder_id" + whereFolderID);
            stmt2.setInt(1, context.getContextId());
            stmt2.setInt(2, folderID);
            if (null != subfolderIDs && 0 < subfolderIDs.size()) {
                for (int i = 0; i < subfolderIDs.size(); i++) {
                    stmt2.setInt(i + 3, subfolderIDs.get(i));
                }
            }
            stmt2.executeUpdate();
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt1);
            DBUtils.closeSQLStuff(stmt2);
        }
    }

}
