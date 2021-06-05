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


package com.openexchange.filestore.impl;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.DestroyAwareFileStorage;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.InterestsAware;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link CompositeFileStorageService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositeFileStorageService implements FileStorageService, ServiceTrackerCustomizer<FileStorageProvider, FileStorageProvider> {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositeFileStorageService.class);

    /** The list of known providers */
    private final List<FileStorageProvider> providers = new CopyOnWriteArrayList<FileStorageProvider>();

    /** The bundle context */
    private final BundleContext bundleContext;

    private final Map<Long, ServiceRegistration<Reloadable>> reloadableRegistrations;

    /** The cache holding initialized storages for previously requested file storage URIs */
    private final LoadingCache<URI, FileStorage> storageCache;

    /**
     * Initializes a new {@link CompositeFileStorageService}.
     *
     * @param bundleContext A reference to the bundle context
     */
    public CompositeFileStorageService(BundleContext bundleContext) {
        super();
        this.bundleContext = bundleContext;
        this.reloadableRegistrations = new HashMap<Long, ServiceRegistration<Reloadable>>();
        this.storageCache = CacheBuilder.newBuilder()
            .maximumSize(50000)
            .expireAfterAccess(2, TimeUnit.HOURS)
            .removalListener(new RemovalListener<URI, FileStorage>() {

                @Override
                public void onRemoval(RemovalNotification<URI, FileStorage> notification) {
                    FileStorage fileStorage = notification.getValue();
                    if (fileStorage instanceof DestroyAwareFileStorage) {
                        ((DestroyAwareFileStorage) fileStorage).onDestroyed();
                    }
                }
            })
            .build(new CacheLoader<URI, FileStorage>() {

                @Override
                public FileStorage load(URI uri) throws Exception {
                    return initFileStorage(uri);
                }
            });
    }

    /**
     * Initializes the appropriate file storage for specified URI; e.g. <code>"file:///var/files/1234_ctx_store"</code>.
     *
     * @param uri The URI
     * @return The URI-associated file storage
     * @throws OXException If file storage cannot be returned
     */
    FileStorage initFileStorage(URI uri) throws OXException {
        /*
         * Lookup suitable provider with highest ranking
         */
        FileStorageProvider candidate = null;
        for (FileStorageProvider provider : providers) {
            if (provider.supports(uri) && (null == candidate || provider.getRanking() > candidate.getRanking())) {
                candidate = provider;
            }
        }
        if (null != candidate && candidate.getRanking() >= DEFAULT_RANKING) {
            return new CloseableTrackingFileStorage(candidate.getFileStorage(uri));
        }
        /*
         * Fall back to default implementation
         */
        return new CloseableTrackingFileStorage(getInternalFileStorage(uri));
    }

    @Override
    public FileStorage getFileStorage(URI uri) throws OXException {
        try {
            return null == uri ? null : storageCache.get(FileStorages.ensureScheme(uri));
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    /**
     * Gets the internal (if any) file storage.
     * <p>
     * If there is no internal representation, this method does the same as {@link #getFileStorage(URI)}.
     *
     * @param uri The URI to create the file storage from
     * @return The internal file storage
     * @throws OXException If storage cannot be returned
     */
    public FileStorage getInternalFileStorage(URI uri) throws OXException {
        if (null == uri) {
            return null;
        }
        try {
            LocalFileStorage standardFS = new LocalFileStorage(uri);
            HashingFileStorage hashedFS = new HashingFileStorage(uri, new File(new File(uri), "hashed"));
            return new CompositingFileStorage(standardFS, "hashed", Collections.<String, FileStorage> singletonMap("hashed", hashedFS));
        } catch (IllegalArgumentException e) {
            throw OXException.general("Cannot create file storage for URI: \"" + uri + "\". That URI does not hold the preconditions to be absolute, hierarchical with a scheme equal to \"file\", a non-empty path component, and undefined authority, query, and fragment components.", e);
        }
    }

    @Override
    public boolean supports(URI uri) throws OXException {
        return true;
    }

    @Override
    public int getRanking() {
        return Integer.MAX_VALUE;
    }

    // ---------------------------------------- ServiceTracker methods --------------------------------------------------

    /**
     * Service property identifying a service's registration number. The value of this property must be of type {@code Long}.
     * <p>
     * The value of this property is assigned by the Framework when a service is registered. The Framework assigns a unique, non-negative
     * value that is larger than all previously assigned values since the Framework was started. These values are <b>NOT</b> persistent
     * across restarts of the Framework.
     */
    private static final String SERVICE_ID = Constants.SERVICE_ID;

    @Override
    public synchronized FileStorageProvider addingService(ServiceReference<FileStorageProvider> reference) {
        FileStorageProvider provider = bundleContext.getService(reference);
        /*
         * remember provider unless already known
         */
        List<FileStorageProvider> providers = this.providers;
        if (providers.contains(provider)) {
            bundleContext.ungetService(reference);
            return null;
        }
        if (!providers.add(provider)) {
            // Adding to list failed
            bundleContext.ungetService(reference);
            return null;
        }
        /*
         * register reloadable callback for this provider's interests if applicable
         */
        if (InterestsAware.class.isInstance(provider)) {
            InterestsAware interestsAware = (InterestsAware) provider;
            Long serviceId = (Long) reference.getProperty(SERVICE_ID);
            LoadingCache<URI, FileStorage> storageCache = this.storageCache;
            reloadableRegistrations.put(serviceId, bundleContext.registerService(Reloadable.class, new Reloadable() {

                @Override
                public Interests getInterests() {
                    return interestsAware.getInterests();
                }

                @Override
                public void reloadConfiguration(ConfigurationService configService) {
                    /*
                     * invalidate any cached storages upon configuration changes
                     */
                    storageCache.invalidateAll();
                    LOG.info("Cached file storages invalidated successfully.");
                }
            }, null));
        }
        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<FileStorageProvider> reference, FileStorageProvider provider) {
        // Ignore
    }

    @Override
    public synchronized void removedService(ServiceReference<FileStorageProvider> reference, FileStorageProvider provider) {
        /*
         * remove previously remembered provider & unregister appropriate reloadable callback if applicable
         */
        if (providers.remove(provider)) {
            storageCache.invalidateAll();
            bundleContext.ungetService(reference);
            Long serviceId = (Long) reference.getProperty(SERVICE_ID);
            ServiceRegistration<Reloadable> reloadableRegistration = reloadableRegistrations.remove(serviceId);
            if (null != reloadableRegistration) {
                reloadableRegistration.unregister();
            }
        }
    }

}
