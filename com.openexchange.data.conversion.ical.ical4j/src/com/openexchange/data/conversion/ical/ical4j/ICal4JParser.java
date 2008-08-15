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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.data.conversion.ical.ical4j;


import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Resources;
import net.fortuna.ical4j.util.CompatibilityHints;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ical4j.internal.AttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.TaskConverters;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import com.openexchange.data.conversion.ical.ical4j.internal.AppointmentConverters;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.calendar.CalendarDataObject;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ICal4JParser implements ICalParser {

    private static final Log LOG = LogFactory.getLog(ICal4JParser.class);

    private static final Map<String, Integer> weekdays = new HashMap<String, Integer>();
    static {
        weekdays.put("MO", AppointmentObject.MONDAY);
        weekdays.put("TU", AppointmentObject.TUESDAY);
        weekdays.put("WE", AppointmentObject.WEDNESDAY);
        weekdays.put("TH", AppointmentObject.THURSDAY);
        weekdays.put("FR", AppointmentObject.FRIDAY);
        weekdays.put("SA", AppointmentObject.SATURDAY);
        weekdays.put("SO", AppointmentObject.SUNDAY);
    }

    public ICal4JParser() {
        CompatibilityHints.setHintEnabled(
                CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
        CompatibilityHints.setHintEnabled(
                CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
        CompatibilityHints.setHintEnabled(
                        CompatibilityHints.KEY_NOTES_COMPATIBILITY, true);

    }

    public List<CalendarDataObject> parseAppointments(String icalText, TimeZone defaultTZ, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
        try {
            return parseAppointments(new ByteArrayInputStream(icalText.getBytes("UTF-8")), defaultTZ, ctx, errors, warnings);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
        return new LinkedList<CalendarDataObject>();
    }

    public List<CalendarDataObject> parseAppointments(InputStream ical, TimeZone defaultTZ, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
        List<CalendarDataObject> appointments = new ArrayList<CalendarDataObject>();
        boolean cont = true;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(ical, "UTF-8"));

            while(true) {
                net.fortuna.ical4j.model.Calendar calendar = parse(reader);
                if(calendar == null) { break; }
                int i = 0;
                for(Object componentObj : calendar.getComponents("VEVENT")) {
                    Component vevent = (Component) componentObj;
                    try {
                        appointments.add(convertAppointment(i++, (VEvent)vevent, defaultTZ, ctx, warnings ));
                    } catch (ConversionError conversionError) {
                        errors.add(conversionError);
                    }
                }
            }
            
        } catch (UnsupportedEncodingException e) {
            // IGNORE
        }



        return appointments;
    }

    public List<Task> parseTasks(String icalText, TimeZone defaultTZ, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
        try {
            return parseTasks(new ByteArrayInputStream(icalText.getBytes("UTF-8")), defaultTZ, ctx, errors, warnings);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
        return new LinkedList<Task>();
    }

    public List<Task> parseTasks(InputStream ical, TimeZone defaultTZ, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
        List<Task> tasks = new ArrayList<Task>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ical, "UTF-8"));
            while(true) {
                net.fortuna.ical4j.model.Calendar calendar = parse(reader);
                if(calendar == null) { break; }
                int i = 0;
                for(Object componentObj : calendar.getComponents("VTODO")) {
                    Component vtodo = (Component) componentObj;
                    try {
                        tasks.add(convertTask(i++, (VToDo) vtodo, defaultTZ, ctx, warnings ));
                    } catch (ConversionError conversionError) {
                        errors.add(conversionError);
                    }
                }
            }

        } catch (UnsupportedEncodingException e) {
            // IGNORE
        }


        return tasks;
    }


    protected CalendarDataObject convertAppointment(int index, final VEvent vevent, TimeZone defaultTZ, Context ctx, List<ConversionWarning> warnings) throws ConversionError {

        CalendarDataObject appointment = new CalendarDataObject();

        TimeZone tz = determineTimeZone(vevent, defaultTZ);

        for (final AttributeConverter<VEvent, AppointmentObject> converter : AppointmentConverters.ALL) {
            if (converter.hasProperty(vevent)) {
                converter.parse(index, vevent, appointment, tz, ctx, warnings);
            }
            converter.verify(index, appointment, warnings);
        }

        
        
        appointment.setTimezone(getTimeZoneID(tz));

        return appointment;
    }

    protected Task convertTask(int index, VToDo vtodo, TimeZone defaultTZ, Context ctx, List<ConversionWarning> warnings) throws ConversionError{
        final TimeZone tz = determineTimeZone(vtodo, defaultTZ);
        final Task task = new Task();
        for (final AttributeConverter<VToDo, Task> converter : TaskConverters.ALL) {
            if (converter.hasProperty(vtodo)) {
                converter.parse(index, vtodo, task, tz, ctx, warnings);
            }
            converter.verify(index, task, warnings);
        }
        return task;
    }


    private static final TimeZone determineTimeZone(final CalendarComponent component,
        final TimeZone defaultTZ) throws ConversionError {
        for (String name : new String[] { DtStart.DTSTART, DtEnd.DTEND, Due.DUE, Completed.COMPLETED }) {
            final DateProperty dateProp = (DateProperty) component.getProperty(name);
            if (dateProp != null) {
                return chooseTimeZone(dateProp, defaultTZ);
            }
        }

        return null;
    }

    private static final TimeZone chooseTimeZone(DateProperty dateProperty, TimeZone defaultTZ) {
        TimeZone tz = defaultTZ;
        if (dateProperty.isUtc()) {
            tz = TimeZone.getTimeZone("UTC");
        }
        TimeZone inTZID = (null != dateProperty.getParameter("TZID")) ? TimeZone.getTimeZone(dateProperty.getParameter("TZID").getValue()) : null;
        if (null != inTZID) {
            tz = inTZID;
        }
        return tz;
    }

    private String getTimeZoneID(TimeZone tz) {
        if(net.fortuna.ical4j.model.TimeZone.class.isAssignableFrom(tz.getClass())) {
            return "UTC";
        }
        if(tz.getID().equals("GMT")) { // Hack for VTIMEZONE. iCal4J sets timezone to GMT, though we prefer UTC
            return "UTC";
        }
        return tz.getID();
    }

    private net.fortuna.ical4j.model.Calendar parse(BufferedReader reader) throws ConversionError {
        CalendarBuilder builder = new CalendarBuilder();

        try {
            StringBuilder chunk = new StringBuilder();
            String line;
            boolean read = false;
            // Copy until we find an END:VCALENDAR
            while((line = reader.readLine()) != null) {
                if(!line.startsWith("END:VCALENDAR")){
                    if(!line.matches("\\s*")) {
                        read = true;
                        chunk.append(line).append("\n");
                    }
                } else {
                    break;
                }
            }
            if(!read) {  return null; }
            chunk.append("END:VCALENDAR\n");
            return builder.build(new StringReader(chunk.toString())); // FIXME: Encoding!
        } catch (IOException e) {
            //IGNORE
        } catch (ParserException e) {
            LOG.warn(e.getMessage(), e);
            throw new ConversionError(-1, ConversionWarning.Code.PARSE_EXCEPTION, e.getMessage());
        }
        return null;
    }

}