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

package com.openexchange.file.storage.infostore.osgi;

import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.infostore.internal.InfostoreFileStorageService;
import com.openexchange.file.storage.infostore.internal.TrashCleanupHandler;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;


/**
 * {@link InfostoreFileStorageActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFileStorageActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { InfostoreFacade.class, InfostoreSearchEngine.class, FolderService.class,
            ContentTypeDiscoveryService.class, ContextService.class, ConfigViewFactory.class, UserService.class, UserPermissionService.class, DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        registerService(FileStorageService.class, new InfostoreFileStorageService() {
            @Override
            public InfostoreFacade getInfostore() {
                return getService(InfostoreFacade.class);
            }

            @Override
            public InfostoreSearchEngine getSearch() {
                return getService(InfostoreSearchEngine.class);
            }
        }, null);
        registerService(LoginHandlerService.class, new TrashCleanupHandler());
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }
}
