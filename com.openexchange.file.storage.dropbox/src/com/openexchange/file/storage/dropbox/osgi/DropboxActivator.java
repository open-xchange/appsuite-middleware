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

package com.openexchange.file.storage.dropbox.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.dropbox.DropboxServices;
import com.openexchange.file.storage.dropbox.http.DropboxHttpClientConfiguration;
import com.openexchange.file.storage.oauth.osgi.AbstractCloudStorageActivator;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.TimerService;
import com.openexchange.version.VersionService;

/**
 * {@link DropboxActivator} - Activator for Dropbox bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxActivator extends AbstractCloudStorageActivator {

    /**
     * Initializes a new {@link DropboxActivator}.
     */
    public DropboxActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { FileStorageAccountManagerLookupService.class, ConfigurationService.class, SessiondService.class, MimeTypeMap.class, 
                                TimerService.class, OAuthService.class, OAuthAccessRegistryService.class, SSLConfigurationService.class, ConfigViewFactory.class,
                                HttpClientService.class, VersionService.class, ClusterLockService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        DropboxServices.setServices(this);
        registerService(SpecificHttpClientConfigProvider.class, new DropboxHttpClientConfiguration(getService(VersionService.class)));
        super.startBundle();
    }

    @Override
    public <S> void registerService(final Class<S> clazz, final S service) {
        super.registerService(clazz, service);
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            super.stopBundle();
            cleanUp();
            DropboxServices.setServices(null);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(DropboxActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected ServiceTrackerCustomizer<OAuthServiceMetaData, OAuthServiceMetaData> getServiceRegisterer(BundleContext context) {
        return new OAuthServiceMetaDataRegisterer(context, this);
    }

    @Override
    protected KnownApi getAPI() {
        return KnownApi.DROPBOX;
    }
}
