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

package com.openexchange.hazelcast.upgrade381.osgi;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
import com.openexchange.legacy.PortableContextInvalidationCallable;


/**
 * {@link UpgradedCacheListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpgradedCacheListener implements com.openexchange.caching.events.CacheListener {

    static final String CACHE_REGION = "Context";

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
        if (fromRemote || null == cacheEvent || false == CACHE_REGION.equals(cacheEvent.getRegion()) ||
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

        Set<Member> remoteMembers = getRemoteMembers(client);
        if (null != remoteMembers && false == remoteMembers.isEmpty()) {
            Member member = remoteMembers.iterator().next();
            IExecutorService executor = client.getExecutorService("default");
            Future<Boolean> future = executor.submitToMember(new PortableContextInvalidationCallable(parseContextIds(cacheEvent)), member);
            LOG.info("Successfully submitted invalidation of contexts to: {}", member);


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
                    LOG.info("Successfully invalidated of contexts, shutting down client after {}ms...", Integer.valueOf(SHUTDOWN_DELAY));
                } else {
                    LOG.warn("Failed invalidation of contexts, shutting down client after {}ms...", Integer.valueOf(SHUTDOWN_DELAY));
                }
            } catch (Exception e) {
                LOG.warn("Failed invalidation of contexts, shutting down client after {}ms...", Integer.valueOf(SHUTDOWN_DELAY), e);
            }
        } else {
            LOG.warn("Found no remote members in cluster, shutting down client after {}ms...", Integer.valueOf(SHUTDOWN_DELAY));
        }

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

    private static int[] parseContextIds(com.openexchange.caching.events.CacheEvent cacheEvent) {
        List<Serializable> keys = cacheEvent.getKeys();

        Set<Integer> ids = new LinkedHashSet<Integer>(keys.size());
        for (Serializable key : keys) {
            if (String.class.isInstance(key)) {
                // login info. Ignore.
            } else if (Integer.class.isInstance(key)) {
                // context identifier
                ids.add((Integer) key);
            } else {
                LOG.warn("Skipping unexpected cache key: {}", key);
            }
        }

        int[] contextIds = new int[ids.size()];
        Iterator<Integer> iterator = ids.iterator();
        for (int i = 0; i < contextIds.length; i++) {
            contextIds[i] = iterator.next().intValue();
        }
        return contextIds;
    }

}
