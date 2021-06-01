/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.itip.ical;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.ITipSpecialHandling;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link ICal4JITipParser}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ICal4JITipParser {

    /**
     * Initializes a new {@link ICal4JITipParser}.
     */
    private ICal4JITipParser() {}

    public static ImportedCalendar importCalendar(InputStream ical) throws OXException {
        ICalService iCalService = Services.getService(ICalService.class);
        ICalParameters parameters = iCalService.initParameters();
        parameters.set(ICalParameters.IGNORE_UNSET_PROPERTIES, Boolean.TRUE);
        parameters.set(ICalParameters.IGNORE_ALARM, Boolean.TRUE);
        ImportedCalendar calendar = iCalService.importICal(ical, parameters);
        return calendar;
    }

    public static List<ITipMessage> parseMessage(InputStream ical, int owner, CalendarSession session) throws OXException {
        return parseMessage(importCalendar(ical), owner, session);
    }

    public static List<ITipMessage> parseMessage(ImportedCalendar calendar, int owner, CalendarSession session) throws OXException {
        List<ITipMessage> messages = new ArrayList<ITipMessage>();
        Map<String, ITipMessage> messagesPerUID = new HashMap<String, ITipMessage>();
        boolean microsoft = looksLikeMicrosoft(calendar);

        ITipMethod methodValue = (calendar.getMethod() == null) ? ITipMethod.NO_METHOD : ITipMethod.get(calendar.getMethod());

        for (Event event : calendar.getEvents()) {
            ITipMessage message = messagesPerUID.get(event.getUid());
            if (message == null) {
                message = new ITipMessage();
                if (microsoft) {
                    message.addFeature(ITipSpecialHandling.MICROSOFT);
                }
                message.setMethod(methodValue);
                messagesPerUID.put(event.getUid(), message);
                if (owner > 0) {
                    message.setOwner(owner);
                }
            }
            resolveAttendees(event, session, methodValue);
            resolveOrganizer(event, session);
            event.setFlags(CalendarUtils.getFlags(event, session.getUserId()));

            if (event.containsRecurrenceId()) {
                message.addException(event);
            } else {
                message.setEvent(event);
            }
        }
        messages.addAll(messagesPerUID.values());

        return messages;
    }

    private static void resolveAttendees(Event event, CalendarSession session, ITipMethod methodValue) throws OXException {
        if (event.getAttendees() == null || event.getAttendees().isEmpty()) {
            return;
        }
        session.getEntityResolver().prepare(event.getAttendees(), new int[] { session.getUserId() });
        if (ITipMethod.REPLY.equals(methodValue) && event.containsAttendees() && null != event.getAttendees() && event.getAttendees().size() == 1 && event.containsExtendedProperties() && null != event.getExtendedProperties()) {
            // Set attendee comment that is stored in the extended properties
            ExtendedProperties extendedProperties = event.getExtendedProperties();
            ExtendedProperty property = extendedProperties.get("COMMENT");
            if (null != property) {
                event.getAttendees().get(0).setComment(String.valueOf(property.getValue()));
                extendedProperties.remove(property);
                if (extendedProperties.isEmpty()) {
                    event.removeExtendedProperties();
                }
            }
        }
    }

    private static void resolveOrganizer(Event event, CalendarSession session) throws OXException {
        if (event.getOrganizer() == null) {
            return;
        }

        session.getEntityResolver().prepare(event.getOrganizer(), CalendarUserType.INDIVIDUAL, new int[] { session.getUserId() });
    }

    private static boolean looksLikeMicrosoft(ImportedCalendar calendar) {
        String property = calendar.getProdId();
        return null != property && Strings.toLowerCase(property).indexOf("microsoft") >= 0;
    }
}
