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

package com.openexchange.eventsystem.internal;

import java.util.Map;
import com.openexchange.eventsystem.Event;
import com.openexchange.eventsystem.EventSystemService;
import com.openexchange.exception.OXException;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.MsService;
import com.openexchange.ms.Queue;
import com.openexchange.ms.Topic;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link EventSystemServiceImpl} - An event service using {@link MsService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class EventSystemServiceImpl implements EventSystemService {

    private static final String NAME_TOPIC = EventSystemConstants.NAME_TOPIC;
    private static final String NAME_QUEUE = EventSystemConstants.NAME_QUEUE;

    // ----------------------------------------------------------------------------------------- //

    private final ServiceLookup services;

    /**
     * Initializes a new {@link EventSystemServiceImpl}.
     *
     * @throws OXException If initialization fails
     */
    public EventSystemServiceImpl(final ServiceLookup services, final EventHandlerTracker handlers) throws OXException {
        super();
        this.services = services;

        final MsService msService = getMsService();
        final Topic<Map<String, Object>> topic = msService.getTopic(NAME_TOPIC);
        final Queue<Map<String, Object>> queue = msService.getQueue(NAME_QUEUE);

        final MessageListener<Map<String, Object>> messageListener = new EventDistributingMessageListener(handlers, true);
        topic.addMessageListener(messageListener);
        queue.addMessageListener(messageListener);
    }

    /**
     * Shuts-down this event system.
     */
    public void shutdown() {
        final MsService service = services.getOptionalService(MsService.class);
        if (null != service) {
            service.getTopic(NAME_TOPIC).cancel();
        }
    }

    private MsService getMsService() throws OXException {
        final MsService service = services.getService(MsService.class);
        if (null == service) {
            throw ServiceExceptionCode.serviceUnavailable(MsService.class);
        }
        return service;
    }

    @Override
    public void publish(final Event event) throws OXException {
        if (null == event) {
            return;
        }
        final MsService msService = getMsService();
        final Topic<Map<String, Object>> topic = msService.getTopic(NAME_TOPIC);
        topic.publish(EventUtility.wrap(event));
    }

    @Override
    public void deliver(final Event event) throws OXException {
        if (null == event) {
            return;
        }
        final MsService msService = getMsService();
        final Queue<Map<String, Object>> queue = msService.getQueue(NAME_QUEUE);
        queue.offer(EventUtility.wrap(event));
    }

}
