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

package com.openexchange.file.storage.osgi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link OSGIFileStorageAccountManagerLookupService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public class OSGIFileStorageAccountManagerLookupService implements FileStorageAccountManagerLookupService {

    /**
     * Dummy value.
     */
    protected static final Object PRESENT = new Object();

    /**
     * The backing queue.
     */
    protected final ConcurrentMap<FileStorageAccountManagerProvider, Object> providers;

    /**
     * The bundle context reference.
     */
    protected volatile BundleContext bundleContext;

    /**
     * The tracker instance.
     */
    private volatile ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> tracker;

    /**
     * Initializes a new {@link OSGIFileStorageAccountManagerLookupService}.
     */
    public OSGIFileStorageAccountManagerLookupService() {
        super();
        providers = new ConcurrentHashMap<FileStorageAccountManagerProvider, Object>(8, 0.9f, 1);
    }

    /**
     * Starts the tracker.
     *
     * @param context The bundle context
     */
    public void start(final BundleContext context) {
        this.bundleContext = context;
        if (null == tracker) {
            final ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> tracker = new ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider>(context, FileStorageAccountManagerProvider.class, new Customizer());
            tracker.open();
            this.tracker = tracker;
        }
    }

    /**
     * Stops the tracker.
     */
    public void stop() {
        final ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> tracker = this.tracker;
        if (null != tracker) {
            tracker.close();
            this.tracker = null;
        }
        this.bundleContext = null;
    }

    private static final String PARAM_DEFAULT_ACCOUNT = "file.storage.defaultAccount";

    @Override
    public FileStorageAccountManager getAccountManager(final String accountId, final Session session) throws OXException {
        final String paramName = new StringBuilder(PARAM_DEFAULT_ACCOUNT).append('@').append(accountId).toString();
        FileStorageAccountManager accountManager = (FileStorageAccountManager) session.getParameter(paramName);
        if (null == accountManager) {
            FileStorageAccountManagerProvider candidate = null;
            for (final FileStorageAccountManagerProvider provider : providers.keySet()) {
                if ((null == candidate) || (provider.getRanking() > candidate.getRanking())) {
                    final FileStorageAccountManager cAccountManager = provider.getAccountManager(accountId, session);
                    if (null != cAccountManager) {
                        candidate = provider;
                        accountManager = cAccountManager;
                    }
                }
            }
            if (null == accountManager) {
                final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OSGIFileStorageAccountManagerLookupService.class);
                final String ls = Strings.getLineSeparator();
                logger.warn("{}    There is no file storage service available that provides account \"{}\".{}    Please ensure the appropriate {} is up and running.{}    Refer to /opt/open-xchange/sbin/listservices", ls, accountId, ls, FileStorageService.class.getSimpleName(), ls);
                return null;
            }
            session.setParameter(paramName, accountManager);
        }
        return accountManager;
    }

    @Override
    public FileStorageAccountManager getAccountManagerFor(final String serviceId) throws OXException {
        if (null == serviceId) {
            final String msg = "serviceId is null";
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(new NullPointerException(msg), msg);
        }

        FileStorageAccountManagerProvider candidate = null;
        for (final FileStorageAccountManagerProvider provider : providers.keySet()) {
            if (provider.supports(serviceId) && ((null == candidate) || (provider.getRanking() > candidate.getRanking()))) {
                candidate = provider;
            }
        }
        if (null == candidate) {
            throw FileStorageExceptionCodes.NO_ACCOUNT_MANAGER_FOR_SERVICE.create(serviceId);
        }
        return candidate.getAccountManagerFor(serviceId);
    }

    private final class Customizer implements ServiceTrackerCustomizer<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider> {

        protected Customizer() {
            super();
        }

        @Override
        public FileStorageAccountManagerProvider addingService(final ServiceReference<FileStorageAccountManagerProvider> reference) {
            final BundleContext context = bundleContext;
            final FileStorageAccountManagerProvider service = context.getService(reference);
            {
                if (null == providers.putIfAbsent(service, PRESENT)) {
                    return service;
                }
                final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OSGIFileStorageAccountManagerLookupService.Customizer.class);
                logger.warn(new StringBuilder(128).append("File storage account manager provider ").append(service.getClass().getSimpleName()).append(
                    " could not be added. Provider is already present.").toString());
            }
            /*
             * Adding to registry failed
             */
            context.ungetService(reference);
            return null;
        }

        @Override
        public void modifiedService(final ServiceReference<FileStorageAccountManagerProvider> reference, final FileStorageAccountManagerProvider service) {
            // Nothing to do
        }

        @Override
        public void removedService(final ServiceReference<FileStorageAccountManagerProvider> reference, final FileStorageAccountManagerProvider service) {
            if (null != service) {
                try {
                    providers.remove(service);
                } finally {
                    bundleContext.ungetService(reference);
                }
            }
        }
    } // End of Customizer class

}
