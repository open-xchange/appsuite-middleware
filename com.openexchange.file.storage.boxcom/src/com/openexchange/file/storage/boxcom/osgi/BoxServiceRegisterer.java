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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.file.storage.boxcom.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.file.storage.CompositeFileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.boxcom.BoxConstants;
import com.openexchange.file.storage.boxcom.BoxFileStorageService;
import com.openexchange.file.storage.boxcom.Services;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccountDeleteListener;

/**
 * {@link BoxServiceRegisterer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoxServiceRegisterer implements ServiceTrackerCustomizer<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> {

    private final BundleContext context;
    private volatile FileStorageAccountManagerProvider provider;
    private volatile BoxFileStorageService service;
    private volatile ServiceRegistration<FileStorageService> serviceRegistration;
    private volatile ServiceRegistration<OAuthAccountDeleteListener> listenerRegistration;

    /**
     * Initializes a new {@link BoxServiceRegisterer}.
     */
    public BoxServiceRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    /** For testing only */
    public BoxFileStorageService getService() {
        return service;
    }

    @Override
    public FileStorageAccountManagerProvider addingService(final ServiceReference<FileStorageAccountManagerProvider> reference) {
        final FileStorageAccountManagerProvider provider = context.getService(reference);
        if (!provider.supports(BoxConstants.ID)) {
            context.ungetService(reference);
            return null;
        }
        synchronized (this) {
            BoxFileStorageService service = this.service;
            if (null == service) {
                /*
                 * Try to create Box.com service
                 */
                service = new BoxFileStorageService(Services.getServices());
                this.serviceRegistration = context.registerService(FileStorageService.class, service, null);
                this.listenerRegistration = context.registerService(OAuthAccountDeleteListener.class, service, null);
                this.service = service;
                this.provider = provider;
            } else {
                /*
                 * Already created before, but new provider
                 */
                CompositeFileStorageAccountManagerProvider compositeProvider = service.getCompositeAccountManager();
                if (null == compositeProvider) {
                    compositeProvider = new CompositeFileStorageAccountManagerProvider();
                    compositeProvider.addProvider(this.provider);
                    unregisterService(null);
                    service = new BoxFileStorageService(Services.getServices(), compositeProvider);
                    this.serviceRegistration = context.registerService(FileStorageService.class, service, null);
                    this.listenerRegistration = context.registerService(OAuthAccountDeleteListener.class, service, null);
                    this.service = service;
                    this.provider = compositeProvider;
                }
                compositeProvider.addProvider(provider);
            }
        }
        return provider;
    }

    @Override
    public void modifiedService(final ServiceReference<FileStorageAccountManagerProvider> reference, final FileStorageAccountManagerProvider provider) {
        // Ignore
    }

    @Override
    public void removedService(final ServiceReference<FileStorageAccountManagerProvider> reference, final FileStorageAccountManagerProvider provider) {
        if (null != provider) {
            synchronized (this) {
                final CompositeFileStorageAccountManagerProvider compositeProvider = this.service.getCompositeAccountManager();
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
    }

    private void unregisterService(final ServiceReference<FileStorageAccountManagerProvider> ref) {
        ServiceRegistration<FileStorageService> serviceRegistration = this.serviceRegistration;
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
            this.serviceRegistration = null;
        }

        ServiceRegistration<OAuthAccountDeleteListener> listenerRegistration = this.listenerRegistration;
        if (null != listenerRegistration) {
            listenerRegistration.unregister();
            this.listenerRegistration = null;
        }

        ServiceReference<FileStorageAccountManagerProvider> reference = ref;
        if (null != reference) {
            context.ungetService(reference);
        }

        this.service = null;
    }

}
