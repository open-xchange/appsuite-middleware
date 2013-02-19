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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.hazelcast.channel;

import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.realtime.ResourceRegistry;
import com.openexchange.realtime.packet.ID;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link ResourceDirectory}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ResourceDirectory implements EventHandler {

    private static Log LOG = LogFactory.getLog(ResourceDirectory.class);

    private final String mapName;

    /**
     * Initializes a new {@link ResourceDirectory}.
     */
    public ResourceDirectory(String resourceDirectoryName) {
        super();
        this.mapName = resourceDirectoryName;
    }

    /**
     * Looks up the member node hosting the supplied ID.
     *
     * @param id The ID to lookup
     * @return The member, or <code>null</code> if not found
     * @throws OXException
     */
    public Member lookupMember(ID id) throws OXException {
        return getDirectory().get(id);
    }

    /**
     * Gets a value indicating whether the directory contains an ID or not.
     *
     * @param id The ID
     * @return <code>true</code> if there is an entry for the supplied ID, <code>false</code>, otherwise
     * @throws OXException
     */
    public boolean contains(ID id) throws OXException {
        return getDirectory().containsKey(id);
    }

    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();
        Object idProp = event.getProperty(ResourceRegistry.ID_PROPERTY);
        if (null == idProp || false == ID.class.isInstance(idProp)) {
            LOG.warn("Ignoring event without ID: " + event);
            return;
        }
        ID id = (ID) idProp;
        try {
            if (topic.equals(ResourceRegistry.TOPIC_REGISTERED)) {
                put(id);
            } else if (topic.equals(ResourceRegistry.TOPIC_UNREGISTERED)) {
                remove(id);
            }
        } catch (OXException e) {
            LOG.error("Error handling event", e);
        }
    }

    private void put(ID id) throws OXException {
        getDirectory().set(id, getLocalMember(), 0, TimeUnit.SECONDS);
    }

    private Member remove(ID id) throws OXException {
        return getDirectory().remove(id);
    }

    private IMap<ID, Member> getDirectory() throws OXException {
        try {
            HazelcastInstance hazelcastInstance = HazelcastAccess.getHazelcastInstance();
            if (null == hazelcastInstance || false == hazelcastInstance.getLifecycleService().isRunning()) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
            }
            return hazelcastInstance.getMap(mapName);
        } catch (RuntimeException e) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName(), e);
        }
    }

    private static Member getLocalMember() throws OXException {
        HazelcastInstance hazelcastInstance = HazelcastAccess.getHazelcastInstance();
        if (null == hazelcastInstance || false == hazelcastInstance.getLifecycleService().isRunning()) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
        }
        return hazelcastInstance.getCluster().getLocalMember();
    }

}
