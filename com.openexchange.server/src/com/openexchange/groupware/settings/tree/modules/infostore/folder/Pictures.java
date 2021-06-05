/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.user.User;


/**
 * {@link Pictures}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8
 */
public class Pictures implements PreferencesItemService {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Pictures.class);

    private static final String DEFAULT_ID = FileStorageAccount.DEFAULT_ID;

    /**
     * Initializes a new {@link Pictures}.
     */
    public Pictures() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "infostore", "folder", "pictures" };
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
                 * Use infostore default pictures folder if available
                 */
                long start = System.currentTimeMillis();
                if (InfostoreFacades.isInfoStoreAvailable()) {
                    LOG.debug("After InfostoreFacades.isInfoStoreAvailable(): {}ms", Long.valueOf(System.currentTimeMillis() - start));
                    int folderID = new OXFolderAccess(ctx).getDefaultFolderID(user.getId(), FolderObject.INFOSTORE, FolderObject.PICTURES);
                    LOG.debug("After OXFolderAccess(ctx).getDefaultFolder(): {}ms", Long.valueOf(System.currentTimeMillis() - start));
                    if (-1 != folderID) {
                        setting.setSingleValue(String.valueOf(folderID));
                    }
                    return;
                }
                /*
                 * Get pictures folder from primary account
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
                                FileStorageFolder picturesFolder = ((MediaFolderAwareFolderAccess) fa).getPicturesFolder();
                                setting.setSingleValue(new FolderID(
                                    fileStorageService.getId(), defaultAccount.getId(), picturesFolder.getId()).toUniqueID());
                            }
                            return;
                        } finally {
                            accountAccess.close();
                        }
                    }
                } catch (OXException e) {
                    LOG.error("Infostore pictures folder could not be applied to user configuration.", e);
                }
            }
        };
    }

}
