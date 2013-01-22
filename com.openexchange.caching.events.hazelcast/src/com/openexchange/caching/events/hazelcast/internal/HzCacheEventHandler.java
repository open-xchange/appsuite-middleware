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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.caching.events.hazelcast.internal;

import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link HzCacheEventHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class HzCacheEventHandler implements CacheListener, MessageListener<HzCacheEvent> {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(HzCacheEventHandler.class);
    private static final String TOPIC_NAME = "cacheEvents";
    private static final AtomicReference<HazelcastInstance> HZ_REFERENCE = new AtomicReference<HazelcastInstance>();
    
    private final CacheEventService cacheEvents;

    /**
     * Sets the specified {@link HazelcastInstance}.
     *
     * @param hazelcast The {@link HazelcastInstance}
     */
    public static void setHazelcastInstance(final HazelcastInstance hazelcast) {
        HZ_REFERENCE.set(hazelcast);
    }
    
    /**
     * Initializes a new {@link HzCacheEventHandler}.
     * @throws OXException 
     */
    public HzCacheEventHandler(CacheEventService cacheEvents) throws OXException {
        super();
        this.cacheEvents = cacheEvents;
        getTopic().addMessageListener(this);
    }
    
    public void stop() {
        cacheEvents.removeListener(this);
        try {
            getTopic().removeMessageListener(this);
        } catch (OXException e) {
            LOG.warn("Error removing message listener", e);
        }
    }

    @Override
    public void onEvent(CacheEvent cacheEvent, String senderID) {
        try {
            getTopic().publish(new HzCacheEvent(cacheEvent, senderID));
        } catch (OXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private ITopic<HzCacheEvent> getTopic() throws OXException {
        try {
            HazelcastInstance hazelcastInstance = HZ_REFERENCE.get();
            if (null == hazelcastInstance || false == hazelcastInstance.getLifecycleService().isRunning()) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
            }
            return hazelcastInstance.getTopic(TOPIC_NAME);
        } catch (HazelcastException e) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(e, HazelcastInstance.class.getName());
        } catch (RuntimeException e) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(e, HazelcastInstance.class.getName());
        }
    }

    @Override
    public void onMessage(Message<HzCacheEvent> message) {
        if (null != message && this != message.getSource()) {
            HzCacheEvent event = message.getMessageObject();
            if (null != event) {
                cacheEvents.notify(event.getCacheEvent(), event.getSenderID());                
            }
        }
    }

}
