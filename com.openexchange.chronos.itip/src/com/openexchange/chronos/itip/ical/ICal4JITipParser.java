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

package com.openexchange.chronos.itip.ical;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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

    public List<ITipMessage> parseMessage(InputStream ical, TimeZone defaultTZ, int owner, CalendarSession session) throws OXException {
        List<ITipMessage> messages = new ArrayList<ITipMessage>();
        Map<String, ITipMessage> messagesPerUID = new HashMap<String, ITipMessage>();
        ICalService iCalService = Services.getService(ICalService.class);
        ICalParameters parameters = iCalService.initParameters();
        parameters.set(ICalParameters.IGNORE_UNSET_PROPERTIES, Boolean.TRUE);
        parameters.set(ICalParameters.IGNORE_ALARM, Boolean.TRUE);
        ImportedCalendar calendar = iCalService.importICal(ical, parameters);

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

    private void resolveAttendees(Event event, CalendarSession session, ITipMethod methodValue) throws OXException {
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

    private void resolveOrganizer(Event event, CalendarSession session) throws OXException {
        if (event.getOrganizer() == null) {
            return;
        }

        session.getEntityResolver().prepare(event.getOrganizer(), CalendarUserType.INDIVIDUAL);
    }

    private boolean looksLikeMicrosoft(ImportedCalendar calendar) {
        String property = calendar.getProdId();
        return null != property && Strings.toLowerCase(property).indexOf("microsoft") >= 0;
    }
}
