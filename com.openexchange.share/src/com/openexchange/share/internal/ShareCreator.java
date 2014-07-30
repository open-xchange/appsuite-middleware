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

package com.openexchange.share.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.Entity;
import com.openexchange.share.Share;
import com.openexchange.share.ShareRequest;
import com.openexchange.share.rdb.ShareStorage;
import com.openexchange.share.rdb.StorageParameters;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;


/**
 * {@link ExternalShareCreator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.x.x
 */
public class ExternalShareCreator extends AbstractCreator {

    /**
     * Initializes a new {@link ExternalShareCreator}.
     * @param shareRequest
     * @param entity
     * @param session
     */
    ExternalShareCreator(ShareRequest shareRequest, Entity entity, ServerSession session) {
        super(shareRequest, entity, session);
    }

    @Override
    protected void perform() throws OXException {
        DatabaseService dbService = getDatabaseService();
        UserService userService = getUserService();
        FolderService folderService = getFolderService();
        ShareStorage shareStorage = getShareStorage();

        Context context = session.getContext();
        User user = session.getUser();
        Connection con = dbService.getWritable(context);
        try {
            Databases.startTransaction(con);
            User guest = prepareGuest();
            int guestId = userService.createUser(con, context, guest);
            if (shareRequest.getItem() == null) {
                FolderServiceDecorator fsDecorator = new FolderServiceDecorator();
                fsDecorator.put(Connection.class.getName(), con);
                UserizedFolder folder = folderService.getFolder(
                    FolderStorage.REAL_TREE_ID,
                    shareRequest.getFolder(),
                    session,
                    fsDecorator);
                Folder modifiedFolder = modifyFolder(folder, guestId);
                folderService.updateFolder(modifiedFolder, new Date(), session, fsDecorator);
                Share share = createShare(guestId);
                StorageParameters parameters = new StorageParameters()
                    .put(Connection.class.getName(), con);
                shareStorage.storeShare(share, parameters);
            } else {
                // TODO
            }
            con.commit();
        } catch (SQLException e) {
            Databases.rollback(con);
            throw new OXException(e); // TODO
        } finally {
            Databases.autocommit(con);
            dbService.backWritable(context, con);
        }
    }

    private Share createShare(int guestId) {
        int contextId = session.getContextId();
        int userId = session.getUserId();
        Date created = new Date();
        DefaultShare share = new DefaultShare();
        share.setToken(ShareTool.generateToken(contextId));
        share.setAuthentication(AuthenticationMode.ANONYMOUS.getID()); // TODO
        share.setContextID(contextId);
        share.setCreated(created);
        share.setLastModified(created);
        share.setCreatedBy(userId);
        share.setModifiedBy(userId);
        share.setGuest(guestId);
        share.setDisplayName("Anonymous"); // TODO
        share.setModule(shareRequest.getModule().getFolderConstant());
        share.setFolder(shareRequest.getFolder());

        return share;
    }

    private Folder modifyFolder(UserizedFolder folder, int guestId) {
        folder.getPermissions();
        SharedFolder newFolder = new SharedFolder(folder.getID());
        newFolder.setTreeID(FolderStorage.REAL_TREE_ID);
        Permission[] origPermissions = folder.getPermissions();
        Permission[] newPermissions = new Permission[origPermissions.length + 1];
        System.arraycopy(origPermissions, 0, newPermissions, 0, origPermissions.length);

        SharePermission sharePermission = new SharePermission();
        sharePermission.setMaxPermissions(); // TODO
        sharePermission.setEntity(guestId);
        newPermissions[origPermissions.length] = sharePermission;
        return newFolder;
    }

    private User prepareGuest() {
        User user = session.getUser();
        UserImpl guest = new UserImpl();
        guest.setCreatedBy(session.getUserId());
        guest.setPreferredLanguage(user.getPreferredLanguage());
        guest.setTimeZone(user.getTimeZone());
        guest.setDisplayName(entity.getMailAddress());
        guest.setMail(entity.getMailAddress());
        guest.setMailEnabled(true);
        // TODO: password, mech etc.
        return guest;
    }

}
