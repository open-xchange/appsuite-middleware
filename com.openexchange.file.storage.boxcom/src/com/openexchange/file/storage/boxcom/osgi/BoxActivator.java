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

package com.openexchange.file.storage.boxcom.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.boxcom.Services;
import com.openexchange.file.storage.oauth.osgi.AbstractCloudStorageActivator;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link BoxActivator} - Activator for Box.com bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoxActivator extends AbstractCloudStorageActivator {

    /**
     * Initializes a new {@link BoxActivator}.
     */
    public BoxActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { FileStorageAccountManagerLookupService.class, ConfigurationService.class, SessiondService.class, MimeTypeMap.class, TimerService.class, OAuthService.class, ClusterLockService.class, OAuthAccessRegistryService.class, ThreadPoolService.class, ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServices(this);
        super.startBundle();
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServices(null);
    }

    @Override
    protected ServiceTrackerCustomizer<OAuthServiceMetaData, OAuthServiceMetaData> getServiceRegisterer(BundleContext context) {
        return new OAuthServiceMetaDataRegisterer(context, Services.getServices());
    }

    @Override
    protected KnownApi getAPI() {
        return KnownApi.BOX_COM;
    }
}
