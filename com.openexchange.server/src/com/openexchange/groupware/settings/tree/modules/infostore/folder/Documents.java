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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.groupware.settings.tree.modules.infostore.folder;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.MediaFolderAwareFolderAccess;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.InfostoreFacades;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;


/**
 * {@link Documents}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8
 */
public class Documents implements PreferencesItemService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Documents.class);
    private static final String DEFAULT_ID = FileStorageAccount.DEFAULT_ID;

    /**
     * Initializes a new {@link Documents}.
     */
    public Documents() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "infostore", "folder", "documents" };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return userConfig.hasInfostore();
            }

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                if (user.isGuest()) {
                    return;
                }
                /*
                 * Use infostore default documents folder if available
                 */
                long start = System.currentTimeMillis();
                if (InfostoreFacades.isInfoStoreAvailable()) {
                    LOG.debug("After InfostoreFacades.isInfoStoreAvailable(): {}ms", System.currentTimeMillis() - start);
                    int folderID = new OXFolderAccess(ctx).getDefaultFolderID(user.getId(), FolderObject.INFOSTORE, FolderObject.DOCUMENTS);
                    LOG.debug("After OXFolderAccess(ctx).getDefaultFolder(): {}ms", System.currentTimeMillis() - start);
                    if (-1 != folderID) {
                        setting.setSingleValue(Integer.valueOf(folderID));
                    }
                    return;
                }
                /*
                 * Get documents folder from primary account
                 */
                FileStorageAccountManagerLookupService accountLookupService = ServerServiceRegistry.getServize(
                    FileStorageAccountManagerLookupService.class);
                if (null == accountLookupService) {
                    throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(FileStorageAccountManagerLookupService.class.getName());
                }
                try {
                    FileStorageAccountManager defaultAccountManager = accountLookupService.getAccountManager(DEFAULT_ID, session);
                    if (null != defaultAccountManager) {
                        FileStorageAccount defaultAccount = defaultAccountManager.getAccount(DEFAULT_ID, session);
                        FileStorageService fileStorageService = defaultAccount.getFileStorageService();
                        FileStorageAccountAccess accountAccess = fileStorageService.getAccountAccess(DEFAULT_ID, session);
                        accountAccess.connect();
                        try {
                            FileStorageFolderAccess fa = accountAccess.getFolderAccess();
                            if (fa instanceof MediaFolderAwareFolderAccess) {
                                FileStorageFolder documentsFolder = ((MediaFolderAwareFolderAccess) fa).getDocumentsFolder();
                                setting.setSingleValue(new FolderID(
                                    fileStorageService.getId(), defaultAccount.getId(), documentsFolder.getId()).toUniqueID());
                            }
                            return;
                        } finally {
                            accountAccess.close();
                        }
                    }
                } catch (OXException e) {
                    LOG.error("Infostore documents folder could not be applied to user configuration.", e);
                }
            }
        };
    }

}
