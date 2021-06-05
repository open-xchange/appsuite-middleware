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

package com.openexchange.subscribe.microsoft.graph.osgi;

import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.context.ContextService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.microsoft.graph.contacts.MicrosoftGraphContactsService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.subscribe.microsoft.graph.groupware.MigrateMSLiveSubscriptionsTask;

/**
 * {@link MicrosoftGraphContactsActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MicrosoftGraphContactsActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        //@formatter:off
        return new Class<?>[] { OAuthService.class, OAuthServiceMetaDataRegistry.class, ContextService.class, 
            ClusterLockService.class, FolderService.class, MicrosoftGraphContactsService.class, 
            SSLSocketFactoryProvider.class };
        //@formatter:on
    }

    @Override
    protected void startBundle() throws Exception {
        track(OAuthServiceMetaData.class, new OAuthServiceMetaDataRegisterer(this, context));
        openTrackers();
        // Register the update task
        DefaultUpdateTaskProviderService providerService = new DefaultUpdateTaskProviderService(new MigrateMSLiveSubscriptionsTask());
        registerService(UpdateTaskProviderService.class.getName(), providerService);
    }
}
