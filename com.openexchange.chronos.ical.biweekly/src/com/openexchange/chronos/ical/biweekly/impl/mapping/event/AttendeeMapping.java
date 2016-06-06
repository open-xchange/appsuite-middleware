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

package com.openexchange.chronos.ical.biweekly.impl.mapping.event;

import java.util.ArrayList;
import java.util.List;
import biweekly.component.VEvent;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.biweekly.impl.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import com.openexchange.java.Enums;
import com.openexchange.java.Strings;

/**
 * {@link AttendeeMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AttendeeMapping extends AbstractICalMapping<VEvent, Event> {

    @Override
    public void export(Event event, VEvent vEvent, ICalParameters parameters, List<OXException> warnings) {
        List<Attendee> attendees = event.getAttendees();
        if (null == attendees || 0 == attendees.size()) {
            vEvent.removeProperties(biweekly.property.Attendee.class);
        } else {
            vEvent.removeProperties(biweekly.property.Attendee.class); // TODO: better merge?
            for (Attendee attendee : attendees) {
                vEvent.addAttendee(exportAttendee(attendee));
            }
        }
    }

    @Override
    public void importICal(VEvent vEvent, Event event, ICalParameters parameters, List<OXException> warnings) {
        List<biweekly.property.Attendee> properties = vEvent.getAttendees();
        if (null == properties || 0 == properties.size()) {
            event.setAttendees(null);
        } else {
            List<Attendee> attendees = new ArrayList<Attendee>(properties.size());
            for (biweekly.property.Attendee property : properties) {
                attendees.add(importAttendee(property));
            }
            event.setAttendees(attendees);
        }
    }

    private biweekly.property.Attendee exportAttendee(Attendee attendee) {
        biweekly.property.Attendee property = new biweekly.property.Attendee(attendee.getUri());
        property.setUri(attendee.getUri());
        property.setCommonName(attendee.getCommonName());
        property.setSentBy(attendee.getSentBy());
        return property;
    }

    private Attendee importAttendee(biweekly.property.Attendee property) {
        Attendee attendee = new Attendee();
        if (Strings.isNotEmpty(property.getUri())) {
            attendee.setUri(property.getUri());
        } else if (Strings.isNotEmpty(property.getEmail())) {
            if (property.getEmail().startsWith("mailto:")) {
                attendee.setUri(property.getEmail());
            } else {
                attendee.setUri("mailto:" + property.getEmail());
            }
        }
        attendee.setCommonName(property.getCommonName());
        attendee.setSentBy(property.getSentBy());
        if (null != property.getParticipationStatus()) {
            attendee.setPartStat(Enums.parse(ParticipationStatus.class, property.getParticipationStatus().getValue(), null));
        }
        if (null != property.getCalendarUserType()) {
            attendee.setCuType(Enums.parse(CalendarUserType.class, property.getCalendarUserType().getValue(), null));
        }
        attendee.setRsvp(property.getRsvp());
        return attendee;
    }

}
