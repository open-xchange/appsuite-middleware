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

package com.openexchange.groupware.settings.tree.folder;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageService;
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
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Infostore implements PreferencesItemService {

    /**
     * The logger.
     */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Infostore.class);

    private static final String DEFAULT_ID = FileStorageAccount.DEFAULT_ID;

    /**
     * Default constructor.
     */
    public Infostore() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPath() {
        return new String[] { "folder", "infostore" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {
            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasInfostore();
            }
            @Override
            public void getValue(final Session session, final Context ctx,
                final User user, final UserConfiguration userConfig,
                final Setting setting) throws OXException {
                if (user.isGuest()) {
                    return;
                }
                // Check availability of InfoStore
                if (InfostoreFacades.isInfoStoreAvailable()) {
                    int folderID = new OXFolderAccess(ctx).getDefaultFolderID(user.getId(), FolderObject.INFOSTORE, FolderObject.PUBLIC);
                    if (-1 != folderID) {
                        setting.setSingleValue(String.valueOf(folderID));
                    }
                    return;
                }
                // Choose the primary folder from another file storage
                final FileStorageAccountManagerLookupService lookupService = ServerServiceRegistry.getInstance().getService(FileStorageAccountManagerLookupService.class);
                if (null == lookupService) {
                    throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(FileStorageAccountManagerLookupService.class.getName());
                }
                try {
                    final FileStorageAccountManager defaultAccountManager = lookupService.getAccountManager(DEFAULT_ID, session);
                    if (null != defaultAccountManager) {
                        final FileStorageAccount defaultAccount = defaultAccountManager.getAccount(DEFAULT_ID, session);
                        final FileStorageService fileStorageService = defaultAccount.getFileStorageService();
                        final FileStorageAccountAccess accountAccess = fileStorageService.getAccountAccess(DEFAULT_ID, session);
                        accountAccess.connect();
                        try {
                            final FileStorageFolder personalFolder = accountAccess.getFolderAccess().getPersonalFolder();
                            setting.setSingleValue(new FolderID(
                                fileStorageService.getId(), defaultAccount.getId(), personalFolder.getId()).toUniqueID());
                            return;
                        } finally {
                            accountAccess.close();
                        }
                    }
                } catch (OXException e) {
                    LOG.error("Infostore default folder could not be applied to user configuration.", e);
                }
                // All failed
                setting.setSingleValue(String.valueOf(new OXFolderAccess(ctx).getDefaultFolderID(user.getId(), FolderObject.INFOSTORE, FolderObject.PUBLIC)));
            }
        };
    }
}
