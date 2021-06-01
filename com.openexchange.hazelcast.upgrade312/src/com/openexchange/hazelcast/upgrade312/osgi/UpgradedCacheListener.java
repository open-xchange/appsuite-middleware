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

package com.openexchange.hazelcast.upgrade312.osgi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.openexchange.caching.CacheKey;
import com.openexchange.java.Strings;
import com.openexchange.legacy.PoolAndSchema;
import com.openexchange.legacy.PortableContextInvalidationCallable;


/**
 * {@link UpgradedCacheListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpgradedCacheListener implements com.openexchange.caching.events.CacheListener {

    /** The cache region name for the schema cache */
    static final String CACHE_REGION = "OXDBPoolCache";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpgradedCacheListener.class);
    private static final int SHUTDOWN_DELAY = 3000;

    private final List<ClientConfig> clientConfigs;

    /**
     * Initializes a new {@link UpgradedCacheListener}.
     *
     * @param clientConfigs The client configuration(s) to use
     */
    public UpgradedCacheListener(List<ClientConfig> clientConfigs) {
        super();
        this.clientConfigs = clientConfigs;
    }

    @Override
    public void onEvent(Object sender, com.openexchange.caching.events.CacheEvent cacheEvent, boolean fromRemote) {
        /*
         * check received event
         */
        if (fromRemote || null == cacheEvent || false == CACHE_REGION.equals(cacheEvent.getRegion()) ||
            com.openexchange.caching.events.CacheOperation.INVALIDATE != cacheEvent.getOperation() || null == cacheEvent.getKeys()) {
            LOG.trace("Skipping unrelated event: {}", cacheEvent);
            return;
        }
        LOG.info("Processing: {}", cacheEvent);
        /*
         * Propagate cache event through calling 'c.o.ms.internal.portable.PortableContextInvalidationCallable' on each remote member using a Hazelcast client
         */
        HazelcastInstance client = getHazelcastClient();
        if (null == client) {
            LOG.warn("Unable to acquire Hazelcast client, unable to propagate event.");
            return;
        }
        /*
         * Determine remote members
         */
        final Set<Member> remoteMembers = getRemoteMembers(client);
        if (null == remoteMembers || remoteMembers.isEmpty()) {
            LOG.warn("Found no remote members in cluster");
        } else {
            IExecutorService executor = client.getExecutorService("default");
            Map<Member, Future<Boolean>> futures = executor.submitToMembers(new PortableContextInvalidationCallable(parseSchemas(cacheEvent)), remoteMembers);
            LOG.info("Successfully submitted invalidation of schemas to remote members:{}{}", Strings.getLineSeparator(), getMembersString(remoteMembers));
            /*
             * Check each submitted task
             */
            for (Map.Entry<Member, Future<Boolean>> submittedTask : futures.entrySet()) {
                Member member = submittedTask.getKey();
                Future<Boolean> future = submittedTask.getValue();
                try {
                    Boolean result = null;
                    int retryCount = 3;
                    while (retryCount-- > 0) {
                        try {
                            result = future.get();
                            retryCount = 0;
                        } catch (InterruptedException e) {
                            // Interrupted - Keep interrupted state
                            Thread.currentThread().interrupt();
                            retryCount = 0;
                        } catch (CancellationException e) {
                            // Canceled
                            retryCount = 0;
                        } catch (ExecutionException e) {
                            Throwable cause = e.getCause();

                            // Check for Hazelcast timeout
                            if (!(cause instanceof com.hazelcast.core.OperationTimeoutException)) {
                                throw e;
                            }

                            // Timeout while awaiting remote result
                            if (retryCount <= 0) {
                                // No further retry
                                cancelFutureSafe(future);
                            }
                        }
                    }

                    if (null != result && result.booleanValue()) {
                        LOG.info("Successfully invalidated schemas on member {}", member);
                    } else {
                        LOG.warn("Failed invalidation of schemas on member {}", member);
                    }
                } catch (Exception e) {
                    LOG.warn("Failed invalidation of schemas on member {}", member, e);
                }
            }
        }

        LOG.info("Shutting down client after {}ms...", Integer.valueOf(SHUTDOWN_DELAY));
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(SHUTDOWN_DELAY));
        client.shutdown();
        LOG.info("Client shutdown completed.");
    }

    private HazelcastInstance getHazelcastClient() {
        if (null != clientConfigs) {
            for (int i = 0; i < clientConfigs.size(); i++) {
                try {
                    return HazelcastClient.newHazelcastClient(clientConfigs.get(i));
                } catch (IllegalStateException e) {
                    if (i + 1 < clientConfigs.size()) {
                        LOG.info("Error initializing Hazelcast client, trying alternative configuration.");
                    } else {
                        LOG.error("Error initializing Hazelcast client", e);
                    }
                }
            }
        }
        return null;
    }

    private static <R> void cancelFutureSafe(Future<R> future) {
        if (null != future) {
            try { future.cancel(true); } catch (Exception e) {/*Ignore*/}
        }
    }

    /**
     * Gets the remote members from specified Hazelcast instance.
     *
     * @param hazelcastInstance The Hazelcast instance for the cluster
     * @return The remote members
     */
    private static Set<Member> getRemoteMembers(HazelcastInstance hazelcastInstance) {
        if (null == hazelcastInstance) {
            return Collections.emptySet();
        }

        // Get cluster representation
        Cluster cluster = hazelcastInstance.getCluster();

        // Determine cluster members
        return cluster.getMembers();
    }

    private static List<PoolAndSchema> parseSchemas(com.openexchange.caching.events.CacheEvent cacheEvent) {
        List<Serializable> keys = cacheEvent.getKeys();
        Set<PoolAndSchema> schemas = new LinkedHashSet<PoolAndSchema>(keys.size());
        for (Serializable key : keys) {
            if (CacheKey.class.isInstance(key)) {
                // Cache key
                CacheKey ck = (CacheKey) key;
                int poolId = ck.getContextId();
                String[] keyz = ck.getKeys();
                if (keyz.length > 0) {
                    String schema = keyz[0];
                    schemas.add(new PoolAndSchema(poolId, schema));
                }
            } else {
                LOG.warn("Skipping unexpected cache key: {}", key);
            }
        }
        return new ArrayList<>(schemas);
    }

    private static Object getMembersString(final Set<Member> remoteMembers) {
        return new Object() {
            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder(remoteMembers.size() << 2);
                boolean first = true;
                for (Member member : remoteMembers) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(member.getAddress().getHost());
                }
                return sb.toString();
            }
        };
    }

}
