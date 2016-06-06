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

package com.openexchange.chronos.ical.ical4j.mapping.event;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.SentBy;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipantRole;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
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
    public void export(Event object, VEvent component, ICalParameters parameters, List<OXException> warnings) {
        List<Attendee> attendees = object.getAttendees();
        if (null == attendees || 0 == attendees.size()) {
            removeProperties(component, Property.ATTENDEE);
        } else {
            removeProperties(component, Property.ATTENDEE);  // TODO: better merge?
            for (Attendee attendee : attendees) {
                try {
                    component.getProperties().add(exportAttendee(attendee));
                } catch (URISyntaxException e) {
                    addConversionWarning(warnings, e, Property.ATTENDEE, e.getMessage());
                }
            }
        }
    }

    @Override
    public void importICal(VEvent component, Event object, ICalParameters parameters, List<OXException> warnings) {
        PropertyList properties = component.getProperties(Property.ATTENDEE);
        if (null == properties || 0 == properties.size()) {
            object.setAttendees(null);
        } else {
            List<Attendee> attendees = new ArrayList<Attendee>(properties.size());
            for (Iterator<?> iterator = properties.iterator(); iterator.hasNext();) {
                net.fortuna.ical4j.model.property.Attendee property = (net.fortuna.ical4j.model.property.Attendee) iterator.next();
                attendees.add(importAttendee(property));
            }
            object.setAttendees(attendees);
        }
    }

    private net.fortuna.ical4j.model.property.Attendee exportAttendee(Attendee attendee) throws URISyntaxException {
        net.fortuna.ical4j.model.property.Attendee property = new net.fortuna.ical4j.model.property.Attendee();
        property.setValue(attendee.getUri());
        if (Strings.isNotEmpty(attendee.getCommonName())) {
            property.getParameters().replace(new Cn(attendee.getCommonName()));
        } else {
            property.getParameters().removeAll(Parameter.CN);
        }
        if (Strings.isNotEmpty(attendee.getSentBy())) {
            property.getParameters().replace(new SentBy(attendee.getSentBy()));
        } else {
            property.getParameters().removeAll(Parameter.SENT_BY);
        }
        if (null != attendee.getPartStat()) {
            property.getParameters().replace(new PartStat(attendee.getPartStat().toString()));
        } else {
            property.getParameters().removeAll(Parameter.PARTSTAT);
        }

        return property;
    }

    private Attendee importAttendee(net.fortuna.ical4j.model.property.Attendee property) {
        Attendee attendee = new Attendee();
        if (null != property.getCalAddress()) {
            attendee.setUri(property.getCalAddress().toString());
        } else if (Strings.isNotEmpty(property.getValue())) {
            if (property.getValue().startsWith("mailto:")) {
                attendee.setUri(property.getValue());
            } else {
                attendee.setUri("mailto:" + property.getValue());
            }
        }
        attendee.setCommonName(optParameterValue(property, Parameter.CN));
        attendee.setSentBy(optParameterValue(property, Parameter.SENT_BY));
        attendee.setPartStat(Enums.parse(ParticipationStatus.class, optParameterValue(property, Parameter.PARTSTAT), null));
        attendee.setRole(Enums.parse(ParticipantRole.class, optParameterValue(property, Parameter.ROLE), null));
        attendee.setCuType(Enums.parse(CalendarUserType.class, optParameterValue(property, Parameter.CUTYPE), null));
        attendee.setRsvp(Boolean.valueOf(optParameterValue(property, Parameter.RSVP)));
        return attendee;
    }

}
