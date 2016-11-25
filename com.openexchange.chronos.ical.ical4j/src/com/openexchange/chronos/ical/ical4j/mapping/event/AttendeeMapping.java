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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipantRole;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import com.openexchange.java.Enums;
import com.openexchange.java.Strings;
import net.fortuna.ical4j.extensions.caldav.parameter.CalendarServerAttendeeRef;
import net.fortuna.ical4j.extensions.caldav.property.CalendarServerAttendeeComment;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.parameter.SentBy;

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
            removeProperties(component, CalendarServerAttendeeComment.PROPERTY_NAME);
        } else {
            PropertyList properties = component.getProperties(Property.ATTENDEE);
            removeProperties(component, Property.ATTENDEE);
            for (Attendee attendee : attendees) {
                net.fortuna.ical4j.model.property.Attendee property = getMatchingAttendee(properties, attendee.getUri());
                if (null == property) {
                    property = new net.fortuna.ical4j.model.property.Attendee();
                }
                try {
                    component.getProperties().add(exportAttendee(attendee, property));
                } catch (URISyntaxException e) {
                    addConversionWarning(warnings, e, Property.ATTENDEE, e.getMessage());
                }
            }
            if (Boolean.TRUE.equals(parameters.get(ICalParameters.ATTENDEE_COMMENTS, Boolean.class))) {
                properties = component.getProperties(CalendarServerAttendeeComment.PROPERTY_NAME);
                removeProperties(component, CalendarServerAttendeeComment.PROPERTY_NAME);
                for (Attendee attendee : attendees) {
                    if (Strings.isNotEmpty(attendee.getComment())) {
                        CalendarServerAttendeeComment property = getMatchingAttendeeComment(properties, attendee.getUri());
                        if (null == property) {
                            property = new CalendarServerAttendeeComment(CalendarServerAttendeeComment.FACTORY);
                        }
                        try {
                            component.getProperties().add(exportAttendeeComment(attendee, property));
                        } catch (URISyntaxException e) {
                            addConversionWarning(warnings, e, CalendarServerAttendeeComment.PROPERTY_NAME, e.getMessage());
                        }
                    }
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

    private static net.fortuna.ical4j.model.property.Attendee exportAttendee(Attendee attendee, net.fortuna.ical4j.model.property.Attendee property) throws URISyntaxException {
        property.setValue(attendee.getUri());
        if (Strings.isNotEmpty(attendee.getCn())) {
            property.getParameters().replace(new Cn(attendee.getCn()));
        } else {
            property.getParameters().removeAll(Parameter.CN);
        }
        if (null != attendee.getSentBy() && Strings.isNotEmpty(attendee.getSentBy().getUri())) {
            property.getParameters().replace(new SentBy(attendee.getSentBy().getUri()));
        } else {
            property.getParameters().removeAll(Parameter.SENT_BY);
        }
        if (null != attendee.getPartStat()) {
            property.getParameters().replace(getPartStat(attendee.getPartStat()));
        } else {
            property.getParameters().removeAll(Parameter.PARTSTAT);
        }
        if (null != attendee.getRole()) {
            property.getParameters().replace(new Role(attendee.getRole().toString()));
        } else {
            property.getParameters().removeAll(Parameter.ROLE);
        }
        if (null != attendee.getCuType()) {
            property.getParameters().replace(new CuType(attendee.getCuType().toString()));
        } else {
            property.getParameters().removeAll(Parameter.CUTYPE);
        }
        if (null != attendee.isRsvp()) {
            property.getParameters().replace(new Rsvp(attendee.isRsvp()));
        } else {
            property.getParameters().removeAll(Parameter.RSVP);
        }
        return property;
    }

    private static CalendarServerAttendeeComment exportAttendeeComment(Attendee attendee, CalendarServerAttendeeComment property) throws URISyntaxException {
        property.getParameters().replace(new CalendarServerAttendeeRef(attendee.getUri()));
        property.setValue(attendee.getComment());
        return property;
    }

    private static Attendee importAttendee(net.fortuna.ical4j.model.property.Attendee property) {
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
        Parameter sentByParameter = property.getParameter(Parameter.SENT_BY);
        if (null != sentByParameter && Strings.isNotEmpty(sentByParameter.getValue())) {
            CalendarUser sentByUser = new CalendarUser();
            sentByUser.setUri(property.getValue());
            attendee.setSentBy(sentByUser);
        }
        attendee.setCn(optParameterValue(property, Parameter.CN));
        attendee.setPartStat(Enums.parse(ParticipationStatus.class, optParameterValue(property, Parameter.PARTSTAT), null));
        attendee.setRole(Enums.parse(ParticipantRole.class, optParameterValue(property, Parameter.ROLE), null));
        attendee.setCuType(Enums.parse(CalendarUserType.class, optParameterValue(property, Parameter.CUTYPE), null));
        attendee.setRsvp(Boolean.valueOf(optParameterValue(property, Parameter.RSVP)));
        return attendee;
    }

    /**
     * Gets an attendee property from the supplied property list whose calendar address matches a specific URI.
     *
     * @param properties The property list to check
     * @param uri The URI to match the calendar user address against
     * @return The matching attendee, or <code>null</code> if not found
     */
    private static net.fortuna.ical4j.model.property.Attendee getMatchingAttendee(PropertyList properties, String uri) {
        for (Iterator<?> iterator = properties.iterator(); iterator.hasNext();) {
            net.fortuna.ical4j.model.property.Attendee property = (net.fortuna.ical4j.model.property.Attendee) iterator.next();
            URI calAddress = property.getCalAddress();
            if (null != calAddress && calAddress.toString().equals(uri)) {
                return property;
            }
        }
        return null;
    }

    /**
     * Gets an attendee property from the supplied property list whose calendar address matches a specific URI.
     *
     * @param properties The property list to check
     * @param uri The URI to match the calendar user address against
     * @return The matching attendee, or <code>null</code> if not found
     */
    private static CalendarServerAttendeeComment getMatchingAttendeeComment(PropertyList properties, String uri) {
        for (Iterator<?> iterator = properties.iterator(); iterator.hasNext();) {
            CalendarServerAttendeeComment property = (CalendarServerAttendeeComment) iterator.next();
            Parameter attendeeRefParameter = property.getParameter(CalendarServerAttendeeRef.PARAMETER_NAME);
            if (null != attendeeRefParameter && null != attendeeRefParameter.getValue() && attendeeRefParameter.getValue().equals(uri)) {
                return property;
            }
        }
        return null;
    }

    private static PartStat getPartStat(ParticipationStatus participationStatus) {
        if (null == participationStatus) {
            return null;
        }
        switch (participationStatus) {
            case ACCEPTED:
                return PartStat.ACCEPTED;
            case DECLINED:
                return PartStat.DECLINED;
            case DELEGATED:
                return PartStat.DELEGATED;
            case NEEDS_ACTION:
                return PartStat.NEEDS_ACTION;
            case TENTATIVE:
                return PartStat.TENTATIVE;
            default:
                return null;
        }
    }

}
