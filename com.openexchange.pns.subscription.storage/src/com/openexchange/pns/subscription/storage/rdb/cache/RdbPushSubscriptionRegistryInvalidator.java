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

package com.openexchange.pns.subscription.storage.rdb.cache;

import static com.openexchange.java.Autoboxing.I;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.java.util.Pair;
import com.openexchange.java.util.Tools;

/**
 * {@link RdbPushSubscriptionRegistryInvalidator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class RdbPushSubscriptionRegistryInvalidator implements CacheListener, ServiceTrackerCustomizer<CacheEventService, CacheEventService> {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RdbPushSubscriptionRegistryInvalidator.class);

    private static final String REGION = RdbPushSubscriptionRegistryCache.REGION;

    // ---------------------------------------------------------------------------------------------------------------

    private final BundleContext context;
    private final RdbPushSubscriptionRegistryCache cache;

    /**
     * Initializes a new {@link RdbPushSubscriptionRegistryInvalidator}.
     *
     * @param context The bundle context
     */
    public RdbPushSubscriptionRegistryInvalidator(RdbPushSubscriptionRegistryCache cache, BundleContext context) {
        super();
        this.cache = cache;
        this.context = context;
    }

    @Override
    public void onEvent(Object sender, CacheEvent cacheEvent, boolean fromRemote) {
        if (fromRemote) {
            // Remotely received
            LOGGER.debug("Handling incoming remote cache event: {}", cacheEvent);

            if (REGION.equals(cacheEvent.getRegion())) {
                List<Serializable> keys = cacheEvent.getKeys();
                Set<Pair<Integer, Integer>> pairs = new LinkedHashSet<Pair<Integer,Integer>>(keys.size());
                for (Serializable key : keys) {
                    if (key instanceof CacheKey) {
                        CacheKey ckey = (CacheKey) key;
                        int contextId = getUnsignedInteger(cacheEvent.getGroupName());
                        if (contextId > 0) {
                            int userId = Integer.parseInt(ckey.getKeys()[0].toString());
                            if (pairs.add(new Pair<Integer, Integer>(I(contextId), I(userId)))) {
                                cache.dropFor(userId, contextId, false);
                            }
                        }
                    }
                }
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

    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Parses a positive <code>int</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>int</code> value or <code>-1</code> if parsing failed
     */
    private static final int getUnsignedInteger(final String s) {
        return Tools.getUnsignedInteger(s);
    }

}
