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

package com.openexchange.folderstorage.cache.osgi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.folderstorage.cache.memory.FolderMapManagement;
import com.openexchange.java.util.Tools;

/**
 * {@link FolderMapInvalidator} - Invalidates folder map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderMapInvalidator implements CacheListener, ServiceTrackerCustomizer<CacheEventService, CacheEventService> {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FolderMapInvalidator.class);

    private static final String REGION = FolderMapManagement.REGION;

    private final BundleContext context;

    /**
     * Initializes a new {@link FolderMapInvalidator}.
     *
     * @param context The bundle context
     */
    public FolderMapInvalidator(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public void onEvent(Object sender, CacheEvent cacheEvent, boolean fromRemote) {
        if (fromRemote && REGION.equals(cacheEvent.getRegion())) {
            // Remotely received
            LOGGER.debug("Handling incoming remote cache event: {}", cacheEvent);

            int contextId = Tools.getUnsignedInteger(cacheEvent.getGroupName());
            if (contextId > 0) {
                for (Serializable cacheKey : cacheEvent.getKeys()) {
                    handleCacheKey(cacheKey, contextId);
                }
            }
        }
    }

    /**
     * Handles specified cache key.
     *
     * @param cacheKey The cache key to handle
     * @param contextId The context identifier
     */
    public static void handleCacheKey(Serializable cacheKey, int contextId) {
        if (cacheKey instanceof CacheKey) {
            CacheKey key = (CacheKey) cacheKey;
            int optUser = key.getContextId();
            Serializable[] keys = key.getKeys();
            if (null == keys || 0 == keys.length) {
                /*
                 * context-/user-wide invalidation
                 */
                if (0 < optUser) {
                    FolderMapManagement.getInstance().dropFor(optUser, contextId, false);
                } else {
                    FolderMapManagement.getInstance().dropFor(contextId, false);
                }
            } else {
                /*
                 * explicit invalidation of one or more folders
                 */
                String treeId = String.valueOf(keys[0]);
                List<String> folderIds = new ArrayList<String>(keys.length - 1);
                for (int i = 1; i < keys.length; i++) {
                    folderIds.add(String.valueOf(keys[i]));
                }
                FolderMapManagement.getInstance().dropFor(folderIds, treeId, optUser, contextId, null, false);
            }
        }
    }

    @Override
    public CacheEventService addingService(ServiceReference<CacheEventService> reference) {
        CacheEventService service = context.getService(reference);
        service.addListener(REGION, this);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<CacheEventService> reference, CacheEventService service) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<CacheEventService> reference, CacheEventService service) {
        service.removeListener(REGION, this);
        context.ungetService(reference);
    }

}
