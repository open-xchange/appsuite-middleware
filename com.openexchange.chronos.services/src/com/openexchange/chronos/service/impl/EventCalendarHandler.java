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

package com.openexchange.chronos.service.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventAdmin;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.chronos.service.event.impl.ChronosCommonEvent;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link EventCalendarHandler} - Throws OSGi events on changes of calendar events
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class EventCalendarHandler implements CalendarHandler {

    /** The {@link EventAdmin} to propagate new events through */
    private final EventAdmin eventAdmin;

    /**
     * Initializes a new {@link EventCalendarHandler}.
     * 
     * @param services The {@link ServiceLookup} to get services from
     * @throws OXException In case of missing service
     */
    public EventCalendarHandler(ServiceLookup services) throws OXException {
        super();
        if (null == services) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ServiceLookup.class.getName());
        }
        EventAdmin eventAdmin = services.getService(EventAdmin.class);
        if (null == eventAdmin) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(EventAdmin.class.getName());
        }
        this.eventAdmin = eventAdmin;
    }

    @Override
    public void handle(CalendarEvent event) {

        // Check for new events
        if (false == event.getCreations().isEmpty()) {
            for (CreateResult result : event.getCreations()) {
                triggerEvent(new ChronosCommonEvent(event, ChronosOSGiEvent.CREATED.getActionID(), result.getCreatedEvent()), ChronosOSGiEvent.CREATED.getTopic());
            }
        }

        // Check for updated events
        if (false == event.getUpdates().isEmpty()) {
            for (UpdateResult result : event.getUpdates()) {
                triggerEvent(new ChronosCommonEvent(event, ChronosOSGiEvent.UPDATED.getActionID(), result.getUpdate(), result.getOriginal()), ChronosOSGiEvent.UPDATED.getTopic());
            }
        }

        // Check for deleted events
        if (false == event.getDeletions().isEmpty()) {
            for (DeleteResult result : event.getDeletions()) {
                // Create an Event instance for the deleted event to propagate
                Event deletedEvent = new Event();
                EventID eventID = result.getEventID();
                deletedEvent.setId(eventID.getObjectID());
                deletedEvent.setFolderId(eventID.getFolderID());
                deletedEvent.setRecurrenceId(eventID.getRecurrenceID());
                deletedEvent.setTimestamp(result.getTimestamp());

                triggerEvent(new ChronosCommonEvent(event, ChronosOSGiEvent.DELETED.getActionID(), deletedEvent), ChronosOSGiEvent.DELETED.getTopic());
            }
        }
    }

    /**
     * Triggers the OSGi event with given {@link CommonEvent} under the given topic
     * 
     * @param chronosEvent The {@link Event} to propagate
     * @param topic The topic of the event
     */
    private void triggerEvent(CommonEvent chronosEvent, String topic) {
        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, chronosEvent);

        final org.osgi.service.event.Event osgievent = new org.osgi.service.event.Event(topic, ht);
        eventAdmin.postEvent(osgievent);
    }
}
