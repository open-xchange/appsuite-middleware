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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.OXException;
import com.openexchange.authentication.LoginException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.login.Login;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;

/**
 * {@link LdapGlobalFolderCreator}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LdapGlobalFolderCreator implements LoginHandlerService {

    private static final Log LOG = LogFactory.getLog(LdapGlobalFolderCreator.class);

    /**
     * Initializes a new {@link LdapGlobalFolderCreator}.
     */
    public LdapGlobalFolderCreator() {
        super();
    }

    public void handleLogin(final Login login) throws LoginException {
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
//            final String name = new StringHelper(login.getUser().getLocale()).getString(FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME);
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
//            throw new LoginException(e);
//        } catch (final SettingException e) {
//            throw new LoginException(e);
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

}
