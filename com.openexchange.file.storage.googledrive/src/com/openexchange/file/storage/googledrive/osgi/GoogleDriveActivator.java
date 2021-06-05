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

package com.openexchange.file.storage.googledrive.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.oauth.osgi.AbstractCloudStorageActivator;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccountStorage;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.session.SessionHolder;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.TimerService;

/**
 * {@link GoogleDriveActivator} - Activator for Google Drive bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class GoogleDriveActivator extends AbstractCloudStorageActivator {

    /**
     * Initialises a new {@link GoogleDriveActivator}.
     */
    public GoogleDriveActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { FileStorageAccountManagerLookupService.class, ConfigurationService.class, SessiondService.class, SessionHolder.class, MimeTypeMap.class, TimerService.class, OAuthService.class, OAuthAccountStorage.class, OAuthAccessRegistryService.class, ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        super.startBundle();
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

    @Override
    protected ServiceTrackerCustomizer<OAuthServiceMetaData, OAuthServiceMetaData> getServiceRegisterer(BundleContext context) {
        return new OAuthServiceMetaDataRegisterer(context, this);
    }

    @Override
    protected KnownApi getAPI() {
        return KnownApi.GOOGLE;
    }
}
