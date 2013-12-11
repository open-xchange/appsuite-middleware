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

package com.openexchange.http.event;

import java.net.InetSocketAddress;
import java.text.MessageFormat;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.openexchange.eventsystem.Event;
import com.openexchange.eventsystem.EventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link HttpMessengerEventHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HttpMessengerEventHandler implements EventHandler {

    private static final String TALKINGSTICK = "__talkingstick";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HttpMessengerEventHandler.class);

    private final String mapName;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link HttpMessengerEventHandler}.
     */
    public HttpMessengerEventHandler(final ServiceLookup services) throws OXException {
        super();
        this.services = services;
        mapName = "http.event.talkingstick-map-0";

        final HazelcastInstance hzInstance = getHazelcastInstance();
        final IMap<String, String> map = hzInstance.getMap(mapName);
        final Member localMember = hzInstance.getCluster().getLocalMember();
        final String thisUuid = localMember.getUuid();
        if (null == map.putIfAbsent(TALKINGSTICK, thisUuid)) {
            LOG.info("{} will handle events for {}", getHostNameFrom(localMember), HttpMessengerEventHandler.class.getName());
        }
    }

    private String getHostNameFrom(final Member localMember) {
        final InetSocketAddress address = localMember.getInetSocketAddress();
        return address.isUnresolved() ? address.getAddress().toString() : address.getHostName();
    }

    private HazelcastInstance getHazelcastInstance() throws OXException {
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (hzInstance == null || !hzInstance.getLifecycleService().isRunning()) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
        }
        return hzInstance;
    }

    @Override
    public void handleEvent(final Event event) {
        try {
            final HazelcastInstance hzInstance = getHazelcastInstance();
            final Member localMember = hzInstance.getCluster().getLocalMember();
            final String thisUuid = localMember.getUuid();

            final IMap<String, String> map = hzInstance.getMap(mapName);
            String talkingStickHolder = map.get(TALKINGSTICK);
            if (null == talkingStickHolder) {
                talkingStickHolder = map.putIfAbsent(TALKINGSTICK, thisUuid);
                if (null == talkingStickHolder) {
                    talkingStickHolder = thisUuid;
                    LOG.info("{} will handle events for {}", getHostNameFrom(localMember), HttpMessengerEventHandler.class.getName());
                }
            }
            if (talkingStickHolder.equals(thisUuid)) {
                // It is about us to handle this event

                LOG.info("Handling event {}", event.getTopic());

                // TODO: Pass event to registered HTTP listeners

            }
        } catch (final Exception e) {
            LOG.warn("Could not handle event {}. Reason: {}", event.getTopic(), e.getMessage(), e);
        }
    }

}
