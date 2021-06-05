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

package com.openexchange.mail.osgi;

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
import com.openexchange.mail.MailSessionCache;


/**
 * {@link MailSessionCacheInvalidator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class MailSessionCacheInvalidator implements CacheListener, ServiceTrackerCustomizer<CacheEventService, CacheEventService> {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MailSessionCacheInvalidator.class);

    private static final String REGION = MailSessionCache.REGION;

    private final BundleContext context;

    /**
     * Initializes a new {@link MailSessionCacheInvalidator}.
     *
     * @param context The bundle context
     */
    public MailSessionCacheInvalidator(BundleContext context) {
        super();
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
                for (Serializable cacheKey : keys) {
                    if (cacheKey instanceof CacheKey) {
                        CacheKey ckey = (CacheKey) cacheKey;
                        int contextId = Tools.getUnsignedInteger(cacheEvent.getGroupName());
                        if (contextId > 0) {
                            int userId = Integer.parseInt(ckey.getKeys()[0].toString());
                            if (pairs.add(new Pair<Integer, Integer>(I(contextId), I(userId)))) {
                                MailSessionCache.clearFor(userId, contextId, false);
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

}
