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

package com.openexchange.hazelcast.upgrade324.osgi;

import static com.openexchange.java.Autoboxing.I;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.openexchange.legacy.CacheEvent;
import com.openexchange.legacy.CacheKeyImpl;
import com.openexchange.legacy.PortableCacheEvent;
import com.openexchange.legacy.PortableMessage;


/**
 * {@link UpgradedCacheListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpgradedCacheListener implements com.openexchange.caching.events.CacheListener {

    static final String CACHE_REGION_CONTEXT = "Context";
    static final String CACHE_REGION_SCHEMA_STORE = "OXDBPoolCache";

    private static final Set<String> REGIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(CACHE_REGION_CONTEXT, CACHE_REGION_SCHEMA_STORE)));

    private static final String CACHE_EVENT_TOPIC = "cacheEvents-3";
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpgradedCacheListener.class);
    private static final int SHUTDOWN_DELAY = 3000;

    private final String senderID;
    private final ClientConfig clientConfig;

    /**
     * Initializes a new {@link UpgradedCacheListener}.
     *
     * @param clientConfig The client configuration to use
     */
    public UpgradedCacheListener(ClientConfig clientConfig) {
        super();
        this.senderID = UUID.randomUUID().toString();
        this.clientConfig = clientConfig;
    }

    @Override
    public void onEvent(Object sender, com.openexchange.caching.events.CacheEvent cacheEvent, boolean fromRemote) {
        /*
         * check received event
         */
        if (fromRemote || null == cacheEvent || false == REGIONS.contains(cacheEvent.getRegion()) ||
            com.openexchange.caching.events.CacheOperation.INVALIDATE != cacheEvent.getOperation() || null == cacheEvent.getKeys()) {
            LOG.trace("Skipping unrelated event: {}", cacheEvent);
            return;
        }
        LOG.info("Processing: {}", cacheEvent);
        /*
         * reconstruct & redistribute legacy cache event
         */
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        LOG.info("Successfully initialzed Hazelcast client: {}", client);

        CacheEvent legacyEvent;
        if (CACHE_REGION_CONTEXT.equals(cacheEvent.getRegion())) {
            legacyEvent = reconstructContextEvent(cacheEvent);
        } else {
            legacyEvent = reconstructSchemaEvent(cacheEvent);
        }

        PortableMessage<PortableCacheEvent> legacyMessage = new PortableMessage<PortableCacheEvent>(senderID, PortableCacheEvent.wrap(legacyEvent));
        ITopic<Object> topic = client.getTopic(CACHE_EVENT_TOPIC);
        LOG.info("Successfully got reference to cache event topic: {}", topic);
        LOG.info("Publishing legacy cache event: {}", legacyEvent);
        topic.publish(legacyMessage);
        LOG.info("Successfully published legacy cache event, shutting down client after {}ms...", I(SHUTDOWN_DELAY));
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(SHUTDOWN_DELAY));
        client.shutdown();
        LOG.info("Client shutdown completed.");
    }

    /**
     * Reconstructs a legacy cache event from the supplied "new" cache event, ready for re-distribution in the legacy cluster.
     *
     * @param cacheEvent The "new" cache event
     * @return The corresponding legacy cache event
     */
    private static CacheEvent reconstructContextEvent(com.openexchange.caching.events.CacheEvent cacheEvent) {
        List<Serializable> keys = cacheEvent.getKeys();
        List<Serializable> legacyKeys = new ArrayList<Serializable>(keys.size());
        for (Serializable key : keys) {
            if (String.class.isInstance(key)) {
                // login info
                legacyKeys.add(key);
            } else if (Integer.class.isInstance(key)) {
                // context identifier
                legacyKeys.add(key);
            } else {
                LOG.warn("Skipping unexpected cache key: {}", key);
            }
        }
        return CacheEvent.INVALIDATE(CACHE_REGION_CONTEXT, null, legacyKeys);
    }

    /**
     * Reconstructs a legacy cache event from the supplied "new" cache event, ready for re-distribution in the legacy cluster.
     *
     * @param cacheEvent The "new" cache event
     * @return The corresponding legacy cache event
     */
    private static CacheEvent reconstructSchemaEvent(com.openexchange.caching.events.CacheEvent cacheEvent) {
        List<Serializable> keys = cacheEvent.getKeys();
        List<Serializable> legacyKeys = new ArrayList<Serializable>(keys.size());
        for (Serializable key : keys) {
            if (com.openexchange.caching.CacheKey.class.isInstance(key)) {
                // Cache key
                com.openexchange.caching.CacheKey ck = (com.openexchange.caching.CacheKey) key;
                legacyKeys.add(new CacheKeyImpl(ck.getContextId(), ck.getKeys()));
            } else {
                LOG.warn("Skipping unexpected cache key: {}", key);
            }
        }
        return CacheEvent.INVALIDATE(CACHE_REGION_SCHEMA_STORE, null, legacyKeys);
    }

}
