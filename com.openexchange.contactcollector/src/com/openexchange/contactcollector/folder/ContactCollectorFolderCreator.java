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

package com.openexchange.contactcollector.folder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.contactcollector.osgi.CCServiceRegistry;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
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
public class ContactCollectorFolderCreator implements LoginHandlerService {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactCollectorFolderCreator.class));

    /**
     * Initializes a new {@link ContactCollectorFolderCreator}.
     */
    public ContactCollectorFolderCreator() {
        super();
    }

    @Override
    public void handleLogin(final LoginResult login) throws OXException {
        final int cid = login.getSession().getContextId();
        DatabaseService databaseService = null;
        Connection con = null;
        try {
            databaseService = CCServiceRegistry.getInstance().getService(DatabaseService.class);
            con = databaseService.getWritable(cid);
            final String folderName = StringHelper.valueOf(login.getUser().getLocale()).getString(FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME);
            create(login.getSession(), login.getContext(), folderName, con);
        } catch (final OXException e) {
            throw new OXException(e);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (databaseService != null) {
                databaseService.backWritable(cid, con);
            }
        }
    }

    public void create(final Session session, final Context ctx, final String folderName, final Connection con) throws OXException, SQLException {
        final int cid = session.getContextId();
        final int userId = session.getUserId();

        final ServerUserSetting serverUserSetting = ServerUserSetting.getInstance(con);

        final Integer folderId = serverUserSetting.getContactCollectionFolder(cid, userId);
        final OXFolderAccess folderAccess = new OXFolderAccess(con, ctx);
        if (folderId != null && folderAccess.exists(folderId.intValue())) {
            /*
             * Folder already exists
             */
            return;
        }
        if (!serverUserSetting.isContactCollectionEnabled(cid, userId).booleanValue() && isConfigured(serverUserSetting, cid, userId)) {
            /*
             * Both - collect-on-mail-access and collect-on-mail-transport - disabled
             */
            return;
        }
        /*-
         * Should collect, or not explicitly set, so create folder
         * 
         * Create folder
         */
        int collectFolderID = 0;
        final int parent = folderAccess.getDefaultFolder(userId, FolderObject.CONTACT).getObjectID();
        try {
            collectFolderID =
                OXFolderManager.getInstance(session, folderAccess, con, con).createFolder(
                    createNewContactFolder(userId, folderName, parent),
                    true,
                    System.currentTimeMillis()).getObjectID();
        } catch (final OXException folderException) {
            if (folderException.isPrefix("FLD") && folderException.getCode() == OXFolderExceptionCode.NO_DUPLICATE_FOLDER.getNumber()) {
                LOG.info(new StringBuilder("Found Folder with name of contact collect folder. Guess this is the dedicated folder."));
                collectFolderID = OXFolderSQL.lookUpFolder(parent, folderName, FolderObject.CONTACT, con, ctx);
            }
        }
        /*
         * Remember folder ID
         */
        serverUserSetting.setContactCollectionFolder(cid, userId, Integer.valueOf(collectFolderID));
        serverUserSetting.setContactCollectOnMailAccess(cid, userId, serverUserSetting.isContactCollectOnMailAccess(cid, userId).booleanValue());
        serverUserSetting.setContactCollectOnMailTransport(cid, userId, serverUserSetting.isContactCollectOnMailTransport(cid, userId).booleanValue());
        if (LOG.isInfoEnabled()) {
            LOG.info(new StringBuilder("Contact collector folder (id=").append(collectFolderID).append(
                ") successfully created for user ").append(userId).append(" in context ").append(cid));
        }
    }

    private boolean isConfigured(final ServerUserSetting setting, final int cid, final int userId) throws OXException {
        return setting.getContactCollectionFolder(cid, userId) != null;
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
    public void handleLogout(final LoginResult logout) {
        // Nothing to do on logout
    }
}
