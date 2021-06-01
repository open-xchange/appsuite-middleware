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
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.memory.FolderMapManagement;
import com.openexchange.java.util.Tools;
import com.openexchange.mailaccount.MailAccountListener;


/**
 * {@link CacheFolderStorageInvalidator} - Invalidates folder map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CacheFolderStorageInvalidator implements CacheListener, ServiceTrackerCustomizer<CacheEventService, CacheEventService>, MailAccountListener {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CacheFolderStorageInvalidator.class);

    private final BundleContext context;

    /**
     * Initializes a new {@link CacheFolderStorageInvalidator}.
     *
     * @param context The bundle context
     */
    public CacheFolderStorageInvalidator(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public void onBeforeMailAccountDeletion(int id, Map<String, Object> eventProps, int userId, int contextId, Connection con) throws OXException {
        // Don't care
    }

    @Override
    public void onAfterMailAccountDeletion(int id, Map<String, Object> eventProps, int userId, int contextId, Connection con) throws OXException {
        invalidateFor(userId, contextId);
    }

    @Override
    public void onMailAccountCreated(int id, Map<String, Object> eventProps, int userId, int contextId, Connection con) {
        invalidateFor(userId, contextId);
    }

    @Override
    public void onMailAccountModified(int id, Map<String, Object> eventProps, int userId, int contextId, Connection con) {
        invalidateFor(userId, contextId);
    }

    private void invalidateFor(int userId, int contextId) {
        FolderMapManagement.getInstance().dropFor(userId, contextId, true);
    }

    @Override
    public void onEvent(Object sender, CacheEvent cacheEvent, boolean fromRemote) {
        if (fromRemote) {
            // Remotely received
            LOGGER.debug("Handling incoming remote cache event: {}", cacheEvent);

            final String region = cacheEvent.getRegion();
            if ("GlobalFolderCache".equals(region)) {
                int contextId = Tools.getUnsignedInteger(cacheEvent.getGroupName());
                List<Serializable> cacheKeys = cacheEvent.getKeys();
                if (null == cacheKeys || 0 == cacheKeys.size()) {
                    FolderMapManagement.getInstance().dropFor(contextId, false);
                } else {
                    for (Serializable cacheKey : cacheKeys) {
                        if (cacheKey instanceof CacheKey) {
                            Serializable[] keys = ((CacheKey) cacheKey).getKeys();
                            if (keys.length > 1) {
                                String id = keys[1].toString();
                                String treeId = keys[0].toString();
                                removeFromUserCache(id, treeId, contextId);
                            }
                        }
                    }
                }
            } else if ("OXFolderCache".equals(region)) {
                for (Serializable key : cacheEvent.getKeys()) {
                    CacheKey cacheKey = (CacheKey) key;
                    String id = cacheKey.getKeys()[0].toString();
                    String treeId = FolderStorage.REAL_TREE_ID;
                    removeFromUserCache(id, treeId, cacheKey.getContextId());
                }
            }
        }
    }

    private void removeFromUserCache(String id, String treeId, int contextId) {
        FolderMapManagement.getInstance().dropFor(id, treeId, -1, contextId, null, false);
    }

    @Override
    public CacheEventService addingService(ServiceReference<CacheEventService> reference) {
        final CacheEventService service = context.getService(reference);
        service.addListener("GlobalFolderCache", this);
        service.addListener("OXFolderCache", this);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<CacheEventService> reference, CacheEventService service) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<CacheEventService> reference, CacheEventService service) {
        service.removeListener("GlobalFolderCache", this);
        service.removeListener("OXFolderCache", this);
        context.ungetService(reference);
    }

}
