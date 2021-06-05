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

package com.openexchange.config.cascade.user.cache;

import java.io.Serializable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.caching.events.CacheOperation;


/**
 * {@link CacheInvalidator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CacheInvalidator implements CacheListener, ServiceTrackerCustomizer<CacheEventService, CacheEventService> {

    private final BundleContext context;

    public CacheInvalidator(BundleContext context) {
        super();
        this.context = context;
    }

    /*
     * CacheListener
     */
    @Override
    public void onEvent(Object sender, CacheEvent cacheEvent, boolean fromRemote) {
        if ("UserConfiguration".equals(cacheEvent.getRegion())) {
            CacheOperation operation = cacheEvent.getOperation();
            if (operation == CacheOperation.CLEAR) {
                clear();
            } else if (null != cacheEvent.getKeys()) {
                for (Serializable key : cacheEvent.getKeys()) {
                    if (CacheKey.class.isInstance(key)) {
                        CacheKey cacheKey = (CacheKey) key;
                        if (CacheOperation.INVALIDATE == operation) {
                            String[] keys = cacheKey.getKeys();
                            if (null != keys && 0 < keys.length && null != keys[0]) {
                                try {
                                    invalidateUser(Integer.parseInt(keys[0]), cacheKey.getContextId());
                                } catch (NumberFormatException e) {
                                    org.slf4j.LoggerFactory.getLogger(CacheInvalidator.class).error(
                                        "Unexpected cache key for region {}: \"{}\", skipping invalidation.",
                                        cacheEvent.getRegion(), keys[0], e);
                                }
                            }
                        } else if (CacheOperation.INVALIDATE_GROUP == operation) {
                            invalidateContext(cacheKey.getContextId());
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------- //

    @Override
    public CacheEventService addingService(ServiceReference<CacheEventService> reference) {
        CacheEventService service = context.getService(reference);
        service.addListener("UserConfiguration", this);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<CacheEventService> reference, CacheEventService service) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<CacheEventService> reference, CacheEventService service) {
        service.removeListener("UserConfiguration", this);
    }

    // -------------------------------------------------------------------------------------------------------------------------- //

    /*
     * Cache invalidation
     */
    public void invalidateUser(int userId, int contextId) {
        PropertyMapManagement.getInstance().dropFor(userId, contextId);
    }

    public void invalidateContext(int contextId) {
        PropertyMapManagement.getInstance().dropFor(contextId);
    }

    public void clear() {
        PropertyMapManagement.getInstance().clear();
    }

}
