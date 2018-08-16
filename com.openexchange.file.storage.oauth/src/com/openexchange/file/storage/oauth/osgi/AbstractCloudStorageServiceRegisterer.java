/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2018-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
    private ServiceRegistration<OAuthAccountAssociationProvider> associationProviderRegistration; // guarded by synchronized

    /**
     * Initialises a new {@link AbstractCloudStorageServiceRegisterer}.
     */
    public AbstractCloudStorageServiceRegisterer(BundleContext context, ServiceLookup serviceLookup) {
        super();
        this.context = context;
        this.serviceLookup = serviceLookup;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public FileStorageAccountManagerProvider addingService(ServiceReference<FileStorageAccountManagerProvider> reference) {
        FileStorageAccountManagerProvider provider = context.getService(reference);
        if (false == provider.supports(getProviderId())) {
            context.ungetService(reference);
            return null;
        }
        synchronized (this) {
            AbstractOAuthFileStorageService service = this.service;
            if (null == service) {
                // Try to create the service
                service = getCloudFileStorageService(serviceLookup);
                this.serviceRegistration = context.registerService(FileStorageService.class, service, null);
                this.listenerRegistration = context.registerService(OAuthAccountDeleteListener.class, service, null);
                this.associationProviderRegistration = context.registerService(OAuthAccountAssociationProvider.class, getOAuthAccountAssociationProvider(service), null);
                this.service = service;
                this.provider = provider;
            } else {
                // Already created before, but new provider
                CompositeFileStorageAccountManagerProvider compositeProvider = service.getCompositeAccountManager();
                if (null == compositeProvider) {
                    compositeProvider = new CompositeFileStorageAccountManagerProvider();
                    compositeProvider.addProvider(this.provider);
                    unregisterService(null);
                    service = getCloudFileStorageService(serviceLookup, compositeProvider);
                    this.serviceRegistration = context.registerService(FileStorageService.class, service, null);
                    this.listenerRegistration = context.registerService(OAuthAccountDeleteListener.class, service, null);
                    this.associationProviderRegistration = context.registerService(OAuthAccountAssociationProvider.class, getOAuthAccountAssociationProvider(service), null);
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
    protected abstract AbstractOAuthFileStorageService getCloudFileStorageService(ServiceLookup serviceLookup);

    /**
     * 
     * @param serviceLookup
     * @param compositeProvider
     * @return
     */
    protected abstract AbstractOAuthFileStorageService getCloudFileStorageService(ServiceLookup serviceLookup, CompositeFileStorageAccountManagerProvider compositeProvider);

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

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void modifiedService(ServiceReference<FileStorageAccountManagerProvider> reference, FileStorageAccountManagerProvider service) {
        //nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference<FileStorageAccountManagerProvider> reference, FileStorageAccountManagerProvider service) {
        if (null == provider) {
            return;
        }
        synchronized (this) {
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
            context.ungetService(ref);
        }
        LOG.info("Cloud storage service for '{}' unregistered successfully", getProviderId());
        this.service = null;
    }
}
