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

package com.openexchange.caching.events.ms.internal;

import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.exception.OXException;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.PortableMsService;
import com.openexchange.ms.Topic;

/**
 * {@link MsCacheEventHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class MsCacheEventHandler implements CacheListener, MessageListener<PortableCacheEvent> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MsCacheEventHandler.class);

    private static final String TOPIC_NAME = "cacheEvents-3";

    private final PortableMsService messagingService;
    private final CacheEventService cacheEvents;
    private final String senderId;

    /**
     * Initializes a new {@link MsCacheEventHandler}.
     *
     * @throws OXException
     */
    public MsCacheEventHandler(PortableMsService messagingService, CacheEventService cacheEvents) throws OXException {
        super();
        this.messagingService = messagingService;
        this.cacheEvents = cacheEvents;
        cacheEvents.addListener(this);
        Topic<PortableCacheEvent> topic = getTopic();
        this.senderId = topic.getSenderId();
        topic.addMessageListener(this);
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
    public void onEvent(Object sender, CacheEvent cacheEvent, boolean fromRemote) {
        if (false == fromRemote) {
            LOG.debug("Re-publishing locally received cache event to remote: {} [{}]", cacheEvent, senderId);
            try {
                getTopic().publish(PortableCacheEvent.wrap(cacheEvent));
            } catch (OXException e) {
                LOG.warn("Error publishing cache event", e);
            }
        }
    }

    private Topic<PortableCacheEvent> getTopic() throws OXException {
        return messagingService.getTopic(TOPIC_NAME);
    }

    @Override
    public void onMessage(Message<PortableCacheEvent> message) {
        if (null != message && message.isRemote()) {
            PortableCacheEvent cacheEvent = message.getMessageObject();
            if (null != cacheEvent) {
                LOG.debug("Re-publishing remotely received cache event locally: {} [{}]", message.getMessageObject(), message.getSenderId());
                cacheEvents.notify(this, PortableCacheEvent.unwrap(cacheEvent), true);
            } else {
                LOG.warn("Discarding empty cache event message.");
            }
        }
    }
}
