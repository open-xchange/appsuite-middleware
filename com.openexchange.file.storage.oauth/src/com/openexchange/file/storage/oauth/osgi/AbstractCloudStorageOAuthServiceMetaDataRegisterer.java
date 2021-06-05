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

package com.openexchange.file.storage.oauth.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractCloudStorageOAuthServiceMetaDataRegisterer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractCloudStorageOAuthServiceMetaDataRegisterer implements ServiceTrackerCustomizer<OAuthServiceMetaData, OAuthServiceMetaData> {

    private final BundleContext context;
    private final ServiceLookup services;
    private final String identifier;
    private volatile ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> tracker;

    /**
     * Initialises a new {@link AbstractCloudStorageOAuthServiceMetaDataRegisterer}.
     * 
     * @param services TODO
     */
    public AbstractCloudStorageOAuthServiceMetaDataRegisterer(BundleContext context, ServiceLookup services, String identifier) {
        super();
        this.context = context;
        this.services = services;
        this.identifier = identifier;
    }

    /**
     * @return
     */
    protected abstract ServiceTrackerCustomizer<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> getRegisterer();

    @Override
    public OAuthServiceMetaData addingService(ServiceReference<OAuthServiceMetaData> reference) {
        OAuthServiceMetaData oAuthServiceMetaData = getContext().getService(reference);
        if (false == identifier.equals(oAuthServiceMetaData.getId())) {
            // Not of interest
            getContext().ungetService(reference);
            return null;
        }
        ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> tracker = new ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider>(getContext(), FileStorageAccountManagerProvider.class, getRegisterer());
        this.tracker = tracker;
        tracker.open();
        return oAuthServiceMetaData;
    }

    @Override
    public void modifiedService(ServiceReference<OAuthServiceMetaData> reference, OAuthServiceMetaData service) {
        // nothing
    }

    @Override
    public void removedService(ServiceReference<OAuthServiceMetaData> reference, OAuthServiceMetaData service) {
        if (null == service) {
            return;
        }
        OAuthServiceMetaData oAuthServiceMetaData = service;
        if (identifier.equals(oAuthServiceMetaData.getId())) {
            ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> tracker = this.tracker;
            if (null != tracker) {
                tracker.close();
                this.tracker = null;
            }
        }
        getContext().ungetService(reference);
    }

    /**
     * Gets the context
     *
     * @return The context
     */
    public BundleContext getContext() {
        return context;
    }

    /**
     * Gets the services
     *
     * @return The services
     */
    public ServiceLookup getServiceLookup() {
        return services;
    }
}
