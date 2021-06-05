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
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.file.storage.CompositeFileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageAccountDeleteListener;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.oauth.AbstractOAuthFileStorageService;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractCloudStorageServiceRegisterer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractCloudStorageServiceRegisterer implements ServiceTrackerCustomizer<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCloudStorageServiceRegisterer.class);

    private final BundleContext context;
    private final ServiceLookup serviceLookup;

    private FileStorageAccountManagerProvider provider;
    private AbstractOAuthFileStorageService service;
    private ServiceRegistration<FileStorageService> serviceRegistration;
    private ServiceRegistration<OAuthAccountDeleteListener> listenerRegistration;
    private ServiceRegistration<FileStorageAccountDeleteListener> fsAccountListenerRegistration;
    private ServiceRegistration<OAuthAccountAssociationProvider> associationProviderRegistration; // guarded by synchronized

    /**
     * Initialises a new {@link AbstractCloudStorageServiceRegisterer}.
     */
    public AbstractCloudStorageServiceRegisterer(BundleContext context, ServiceLookup serviceLookup) {
        super();
        this.context = context;
        this.serviceLookup = serviceLookup;
    }

    @Override
    public FileStorageAccountManagerProvider addingService(ServiceReference<FileStorageAccountManagerProvider> reference) {
        FileStorageAccountManagerProvider provider = getContext().getService(reference);
        if (false == provider.supports(getProviderId())) {
            getContext().ungetService(reference);
            return null;
        }

        synchronized (this) {
            AbstractOAuthFileStorageService service = this.service;
            if (null == service) {
                // Try to create the service
                service = getCloudFileStorageService();
                this.serviceRegistration = getContext().registerService(FileStorageService.class, service, null);
                this.fsAccountListenerRegistration = getContext().registerService(FileStorageAccountDeleteListener.class, service, null);
                this.listenerRegistration = getContext().registerService(OAuthAccountDeleteListener.class, service, null);
                this.associationProviderRegistration = getContext().registerService(OAuthAccountAssociationProvider.class, getOAuthAccountAssociationProvider(service), null);
                this.service = service;
                this.provider = provider;
            } else {
                // Already created before, but new provider
                CompositeFileStorageAccountManagerProvider compositeProvider = service.getCompositeAccountManager();
                if (null == compositeProvider) {
                    compositeProvider = new CompositeFileStorageAccountManagerProvider();
                    compositeProvider.addProvider(this.provider);
                    unregisterService(null);
                    service = getCloudFileStorageService(compositeProvider);
                    this.serviceRegistration = getContext().registerService(FileStorageService.class, service, null);
                    this.fsAccountListenerRegistration = getContext().registerService(FileStorageAccountDeleteListener.class, service, null);
                    this.listenerRegistration = getContext().registerService(OAuthAccountDeleteListener.class, service, null);
                    this.associationProviderRegistration = getContext().registerService(OAuthAccountAssociationProvider.class, getOAuthAccountAssociationProvider(service), null);
                    this.service = service;
                    this.provider = compositeProvider;
                }
                compositeProvider.addProvider(provider);
            }

        }
        LOG.info("Cloud storage service for '{}' registered successfully", getProviderId());
        return provider;
    }

    /**
     * Initialises and returns the cloud file storage service
     *
     * @return the cloud file storage service
     */
    protected abstract AbstractOAuthFileStorageService getCloudFileStorageService();

    /**
     *
     * @param compositeProvider
     * @return
     */
    protected abstract AbstractOAuthFileStorageService getCloudFileStorageService(CompositeFileStorageAccountManagerProvider compositeProvider);

    /**
     *
     * @param storageService
     * @return
     */
    protected abstract OAuthAccountAssociationProvider getOAuthAccountAssociationProvider(AbstractOAuthFileStorageService storageService);

    /**
     * Returns the provider's identifier
     *
     * @return the provider's identifier1
     */
    protected abstract String getProviderId();

    @Override
    public void modifiedService(ServiceReference<FileStorageAccountManagerProvider> reference, FileStorageAccountManagerProvider service) {
        //nothing
    }

    @Override
    public void removedService(ServiceReference<FileStorageAccountManagerProvider> reference, FileStorageAccountManagerProvider service) {
        synchronized (this) {
            if (null == provider) {
                return;
            }

            CompositeFileStorageAccountManagerProvider compositeProvider = this.service.getCompositeAccountManager();
            if (null == compositeProvider) {
                unregisterService(reference);
            } else {
                compositeProvider.removeProvider(provider);
                if (!compositeProvider.hasAnyProvider()) {
                    unregisterService(reference);
                }
            }
        }
    }

    /**
     * Unregisters the services
     *
     * @param reference The {@link ServiceReference} for the provider
     */
    private void unregisterService(ServiceReference<FileStorageAccountManagerProvider> reference) {
        ServiceRegistration<FileStorageService> serviceRegistration = this.serviceRegistration;
        if (null != serviceRegistration) {
            this.serviceRegistration = null;
            serviceRegistration.unregister();
        }

        ServiceRegistration<FileStorageAccountDeleteListener> fsAccountListenerRegistration = this.fsAccountListenerRegistration;
        if (null != fsAccountListenerRegistration) {
            this.fsAccountListenerRegistration = null;
            fsAccountListenerRegistration.unregister();
        }

        ServiceRegistration<OAuthAccountDeleteListener> listenerRegistration = this.listenerRegistration;
        if (null != listenerRegistration) {
            this.listenerRegistration = null;
            listenerRegistration.unregister();
        }

        ServiceRegistration<OAuthAccountAssociationProvider> associationProviderRegistration = this.associationProviderRegistration;
        if (null != associationProviderRegistration) {
            this.associationProviderRegistration = null;
            associationProviderRegistration.unregister();
        }

        ServiceReference<FileStorageAccountManagerProvider> ref = reference;
        if (null != ref) {
            getContext().ungetService(ref);
        }
        LOG.info("Cloud storage service for '{}' unregistered successfully", getProviderId());
        this.service = null;
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
     * Gets the serviceLookup
     *
     * @return The serviceLookup
     */
    public ServiceLookup getServiceLookup() {
        return serviceLookup;
    }
}
