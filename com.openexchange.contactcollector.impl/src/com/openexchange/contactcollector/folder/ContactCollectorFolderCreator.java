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

package com.openexchange.contactcollector.folder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import com.openexchange.contactcollector.osgi.CCServiceRegistry;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.login.NonTransient;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderSQL;

/**
 * {@link ContactCollectorFolderCreator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactCollectorFolderCreator implements LoginHandlerService, NonTransient {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactCollectorFolderCreator.class);

    /**
     * Initializes a new {@link ContactCollectorFolderCreator}.
     */
    public ContactCollectorFolderCreator() {
        super();
    }

    @Override
    public void handleLogin(final LoginResult login) throws OXException {
        Session session = login.getSession();
        Context ctx = login.getContext();
        DatabaseService databaseService = CCServiceRegistry.getInstance().getService(DatabaseService.class);
        final String folderName = StringHelper.valueOf(login.getUser().getLocale()).getString(FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME);

        Connection con = databaseService.getReadOnly(ctx);
        try {
            if (exists(session, ctx, con)) {
                return;
            }
        } finally {
            databaseService.backReadOnly(ctx, con);
        }

        con = databaseService.getWritable(ctx);
        boolean modifiedData = false;
        try {
            modifiedData = create(login.getSession(), login.getContext(), folderName, con);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (modifiedData) {
                databaseService.backWritable(ctx, con);
            } else {
                databaseService.backWritableAfterReading(ctx, con);
            }
        }
    }

    public static boolean exists(Session session, Context ctx, Connection con) throws OXException {
        final int cid = session.getContextId();
        final int userId = session.getUserId();

        final ServerUserSetting serverUserSetting = ServerUserSetting.getInstance(con);

        final Integer folderId = serverUserSetting.getContactCollectionFolder(cid, userId);
        if (folderId != null) {
            final OXFolderAccess folderAccess = new OXFolderAccess(con, ctx);
            if (folderAccess.exists(folderId.intValue())) {
                // Folder already exists
                session.setParameter("__ccf#", folderId);
                return true;
            }
        }
        if (!serverUserSetting.isContactCollectionEnabled(cid, userId).booleanValue() && isConfigured(serverUserSetting, cid, userId)) {
            // Both - collect-on-mail-access and collect-on-mail-transport - disabled
            return true;
        }
        // Should collect, or not explicitly set, so create folder
        return false;
    }

    public static boolean create(final Session session, final Context ctx, final String folderName, final Connection con) throws OXException, SQLException {
        // Check again on the master if maybe another parallel request already created the folder.
        if (exists(session, ctx, con)) {
            return false;
        }
        final int cid = session.getContextId();
        final int userId = session.getUserId();
        final OXFolderAccess folderAccess = new OXFolderAccess(con, ctx);
        int collectFolderID = 0;
        {
            final int parent = folderAccess.getDefaultFolder(userId, FolderObject.CONTACT).getObjectID();
            try {
                final FolderObject folder = createNewContactFolder(userId, folderName, parent);
                final OXFolderManager folderManager = OXFolderManager.getInstance(session, folderAccess, con, con);
                collectFolderID = folderManager.createFolder(folder, true, System.currentTimeMillis()).getObjectID();
            } catch (final OXException oxe) {
                if (oxe.isPrefix("FLD") && oxe.getCode() == OXFolderExceptionCode.NO_DUPLICATE_FOLDER.getNumber()) {
                    LOG.info("Found Folder with name of contact collect folder. Guess this is the dedicated folder.");
                    collectFolderID = OXFolderSQL.lookUpFolder(parent, folderName, FolderObject.CONTACT, con, ctx);
                }
            }
        }
        /*
         * Remember folder ID
         */
        final ServerUserSetting serverUserSetting = ServerUserSetting.getInstance(con);
        final Integer folder = Integer.valueOf(collectFolderID);
        serverUserSetting.setContactCollectionFolder(cid, userId, folder);
        session.setParameter("__ccf#", folder);
        serverUserSetting.setContactCollectOnMailAccess(cid, userId, serverUserSetting.isContactCollectOnMailAccess(cid, userId).booleanValue());
        serverUserSetting.setContactCollectOnMailTransport(cid, userId, serverUserSetting.isContactCollectOnMailTransport(cid, userId).booleanValue());
        LOG.info("Contact collector folder (id={}) successfully created for user {} in context {}", folder, Integer.valueOf(userId), Integer.valueOf(cid));
        return true;
    }

    private static boolean isConfigured(final ServerUserSetting setting, final int cid, final int userId) throws OXException {
        return setting.getContactCollectionFolder(cid, userId) != null;
    }

    private static FolderObject createNewContactFolder(final int userId, final String name, final int parent) {
        final FolderObject newFolder = new FolderObject();
        newFolder.setFolderName(name);
        newFolder.setParentFolderID(parent);
        newFolder.setType(FolderObject.PRIVATE);
        newFolder.setModule(FolderObject.CONTACT);

        // User is Admin and can read, write or delete everything
        final OCLPermission perm = new OCLPermission();
        perm.setEntity(userId);
        perm.setFolderAdmin(true);
        perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setReadObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setWriteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setDeleteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setGroupPermission(false);
        newFolder.setPermissions(Collections.singletonList(perm));

        return newFolder;
    }

    @Override
    public void handleLogout(final LoginResult logout) {
        // Nothing to do on logout
    }
}
