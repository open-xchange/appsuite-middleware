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

package com.openexchange.contacts.ldap.folder;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;

/**
 * {@link LdapUserFolderCreator}
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public class LdapUserFolderCreator implements LoginHandlerService {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(LdapUserFolderCreator.class));

    /**
     * Initializes a new {@link LdapUserFolderCreator}.
     */
    public LdapUserFolderCreator() {
        super();
    }

    @Override
    public void handleLogin(final LoginResult login) throws OXException {
        // Here we create the users personal LDAP Folder
        final Session session = login.getSession();
        final int cid = session.getContextId();

        final Context ctx = new ContextImpl(cid);
        final FolderObject fo = new FolderObject();
//        fo.setPermissionsAsArray(new OCLPermission[] { defaultPerm });
        fo.setDefaultFolder(true);
        fo.setParentFolderID(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
        fo.setType(FolderObject.PUBLIC);
//        fo.setFolderName(displayName);
        fo.setModule(FolderObject.INFOSTORE);
//        newFolderId = OXFolderSQL.getNextSerial(ctx, writeCon);
//        OXFolderSQL.insertDefaultFolderSQL(newFolderId, userId, fo, creatingTime, ctx, writeCon);
//        if (LOG.isInfoEnabled()) {
//            LOG.info("User's default INFOSTORE folder successfully created");
//            LOG.info("All user default folders were successfully created");
//            /*
//             * TODO: Set standard special folders (projects, ...) located beneath system user folder
//             */
//            LOG.info(new StringBuilder("User ").append(userId).append(" successfully created").append(" in context ").append(cid).toString());
//        }

//        final Session session = login.getSession();
//        final int cid = session.getContextId();
//        final int userId = session.getUserId();
//        try {
//            final Integer folderId = ServerUserSetting.getContactCollectionFolder(cid, userId);
//            if (folderId != null && new OXFolderAccess(login.getContext()).exists(folderId.intValue())) {
//                /*
//                 * Folder already exists
//                 */
//                return;
//            }
//            /*
//             * Create folder
//             */
//            final String name = StringHelper.valueOf(login.getUser().getLocale()).getString(FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME);
//            final int parent = new OXFolderAccess(login.getContext()).getDefaultFolder(userId, FolderObject.CONTACT).getObjectID();
//            final int collectFolderID = OXFolderManager.getInstance(session).createFolder(
//                createNewContactFolder(userId, name, parent),
//                true,
//                System.currentTimeMillis()).getObjectID();
//            /*
//             * Remember folder ID
//             */
//            ServerUserSetting.setContactCollectionFolder(cid, userId, collectFolderID);
//            ServerUserSetting.setContactColletion(cid, userId, true);
//            if (LOG.isInfoEnabled()) {
//                LOG.info(new StringBuilder("Contact collector folder (id=").append(collectFolderID).append(
//                    ") successfully created for user ").append(userId).append(" in context ").append(cid));
//            }
//        } catch (final OXException e) {
//            throw new OXException(e);
//        } catch (final OXException e) {
//            throw new OXException(e);
//        }
    }

    private FolderObject createNewContactFolder(final int userId, final String name, final int parent) {
        final FolderObject newFolder = new FolderObject();
        newFolder.setFolderName(name);
        newFolder.setParentFolderID(parent);
        newFolder.setType(FolderObject.PRIVATE);
        newFolder.setModule(FolderObject.CONTACT);

        final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
        // User is Admin and can read, write or delete everything
        final OCLPermission perm = new OCLPermission();
        perm.setEntity(userId);
        perm.setFolderAdmin(true);
        perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setReadObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setWriteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setDeleteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setGroupPermission(false);
        perms.add(perm);
        newFolder.setPermissions(perms);

        return newFolder;
    }

    @Override
    public void handleLogout(final LoginResult logout) throws OXException {
        // Nothing to do on logout
    }
}
