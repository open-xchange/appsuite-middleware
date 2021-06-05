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

package com.openexchange.subscribe.osgi;

import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.folder.FolderService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;


/**
 * {@link ServiceTrackingActivator} - Tracks all services required by individual activators to offer a central service look-up.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class ServiceTrackingActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ServiceTrackingActivator}.
     */
    public ServiceTrackingActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigViewFactory.class, SecretService.class, DispatcherPrefixService.class, ContextService.class,
            UserService.class, UserPermissionService.class, InfostoreFacade.class, FolderService.class,
            com.openexchange.folderstorage.FolderService.class, DBProvider.class, SecretEncryptionFactoryService.class, CryptoService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

}
