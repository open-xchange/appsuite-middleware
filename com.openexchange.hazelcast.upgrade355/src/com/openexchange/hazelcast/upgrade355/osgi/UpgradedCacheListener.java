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

package com.openexchange.hazelcast.upgrade355.osgi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.openexchange.legacy.CacheEvent;
import com.openexchange.legacy.PortableCacheEvent;
import com.openexchange.legacy.PortableMessage;


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
        CacheEvent legacyEvent = reconstructEvent(cacheEvent);
        PortableMessage<PortableCacheEvent> legacyMessage = new PortableMessage<PortableCacheEvent>(senderID, PortableCacheEvent.wrap(legacyEvent));
        ITopic<Object> topic = client.getTopic(CACHE_EVENT_TOPIC);
        LOG.info("Successfully got reference to cache event topic: {}", topic);
        LOG.info("Publishing legacy cache event: {}", legacyEvent);
        topic.publish(legacyMessage);
        LOG.info("Successfully published legacy cache event, shutting down client after {}ms...", Integer.valueOf(SHUTDOWN_DELAY));
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
    private static CacheEvent reconstructEvent(com.openexchange.caching.events.CacheEvent cacheEvent) {
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
        return CacheEvent.INVALIDATE(CACHE_REGION, null, legacyKeys);
    }

}
