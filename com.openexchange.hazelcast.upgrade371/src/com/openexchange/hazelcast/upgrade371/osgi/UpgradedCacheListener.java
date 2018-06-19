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

package com.openexchange.hazelcast.upgrade371.osgi;

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

    private final ClientConfig clientConfig;

    /**
     * Initializes a new {@link UpgradedCacheListener}.
     *
     * @param clientConfig The client configuration to use
     */
    public UpgradedCacheListener(ClientConfig clientConfig) {
        super();
        this.clientConfig = clientConfig;
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
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        LOG.info("Successfully initialzed Hazelcast client: {}", client);
        /*
         * Determine remote members
         */
        final Set<Member> remoteMembers = getRemoteMembers(client);
        if (null != remoteMembers && false == remoteMembers.isEmpty()) {
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
        } else {
            LOG.warn("Found no remote members in cluster");
        }

        LOG.info("Shutting down client after {}ms...", Integer.valueOf(SHUTDOWN_DELAY));
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(SHUTDOWN_DELAY));
        client.shutdown();
        LOG.info("Client shutdown completed.");
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
                String schema = ck.getKeys()[0];
                schemas.add(new PoolAndSchema(poolId, schema));
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
