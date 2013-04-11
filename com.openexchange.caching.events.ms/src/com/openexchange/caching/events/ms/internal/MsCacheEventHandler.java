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

package com.openexchange.caching.events.ms.internal;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.exception.OXException;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.MsService;
import com.openexchange.ms.Topic;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link MsCacheEventHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class MsCacheEventHandler implements CacheListener, MessageListener<Map<String, Object>>, EventHandler {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(MsCacheEventHandler.class);
    private static final String TOPIC_NAME = "cacheEvents";
    private static final AtomicReference<MsService> MS_REFERENCE = new AtomicReference<MsService>();

    private final CacheEventService cacheEvents;
    private final String senderId;

    /**
     * Sets the specified {@link MsService}.
     *
     * @param service The {@link MsService}
     */
    public static void setMsService(final MsService service) {
        MS_REFERENCE.set(service);
    }

    /**
     * Initializes a new {@link MsCacheEventHandler}.
     *
     * @throws OXException
     */
    public MsCacheEventHandler(CacheEventService cacheEvents) throws OXException {
        super();
        this.cacheEvents = cacheEvents;
        cacheEvents.addListener(this);
        Topic<Map<String, Object>> topic = getTopic();
        this.senderId = topic.getSenderId();
        topic.addMessageListener(this);
    }

    @Override
    public void handleEvent(Event event) {
        // com.openexchange.push.ms.PushMsListener

        // com.openexchange.event.RemoteEvent

        // Topic: "com/openexchange/remote/*";
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
    public void onEvent(Object sender, CacheEvent cacheEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("publish: " + cacheEvent + " [" + senderId + "]");
        }
        try {
            getTopic().publish(cacheEvent.writePojo());
        } catch (OXException e) {
            LOG.warn("Error publishing cache event", e);
        }
    }

    private Topic<Map<String, Object>> getTopic() throws OXException {
        MsService msService = MS_REFERENCE.get();
        if (null == msService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MsService.class.getName());
        }
        return msService.getTopic(TOPIC_NAME);
    }

    @Override
    public void onMessage(Message<Map<String, Object>> message) {
        if (null != message && message.isRemote()) {
            Map<String, Object> cacheEvent = message.getMessageObject();
            if (null != cacheEvent) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("onMessage: " + message.getMessageObject() + " [" + message.getSenderId() + "]");
                }
                cacheEvents.notify(this, CacheEvent.fromPojo(cacheEvent));
            } else {
                LOG.warn("Discarding empty cache event message.");
            }
        }
    }

}
