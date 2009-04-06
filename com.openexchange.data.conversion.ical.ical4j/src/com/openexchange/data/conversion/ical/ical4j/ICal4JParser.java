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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.util.CompatibilityHints;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ical4j.internal.AppointmentConverters;
import com.openexchange.data.conversion.ical.ical4j.internal.AttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.TaskConverters;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Tobias Prinz <tobias.prinz@open-xchange.com> ( hacks to fix bug 11958, which is ical4j ignoring timezone information if given after event data )
 */
public class ICal4JParser implements ICalParser {

    private static final String UTF8 = "UTF-8";

    private static final Log LOG = LogFactory.getLog(ICal4JParser.class);

    private static final Map<String, Integer> weekdays = new HashMap<String, Integer>();
    static {
        weekdays.put("MO", Integer.valueOf(AppointmentObject.MONDAY));
        weekdays.put("TU", Integer.valueOf(AppointmentObject.TUESDAY));
        weekdays.put("WE", Integer.valueOf(AppointmentObject.WEDNESDAY));
        weekdays.put("TH", Integer.valueOf(AppointmentObject.THURSDAY));
        weekdays.put("FR", Integer.valueOf(AppointmentObject.FRIDAY));
        weekdays.put("SA", Integer.valueOf(AppointmentObject.SATURDAY));
        weekdays.put("SO", Integer.valueOf(AppointmentObject.SUNDAY));
    }

    public ICal4JParser() {
        CompatibilityHints.setHintEnabled(
                CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
        CompatibilityHints.setHintEnabled(
                CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
        CompatibilityHints.setHintEnabled(
                        CompatibilityHints.KEY_NOTES_COMPATIBILITY, true);

    }

    public List<CalendarDataObject> parseAppointments(final String icalText, final TimeZone defaultTZ, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        try {
            return parseAppointments(new ByteArrayInputStream(icalText.getBytes(UTF8)), defaultTZ, ctx, errors, warnings);
        } catch (final UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public List<CalendarDataObject> parseAppointments(final InputStream ical, final TimeZone defaultTZ, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        final List<CalendarDataObject> appointments = new ArrayList<CalendarDataObject>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(ical, UTF8));

            while(true) {
                final net.fortuna.ical4j.model.Calendar calendar = parse(reader);
                if(calendar == null) { break; }
                int i = 0;
                for(final Object componentObj : calendar.getComponents("VEVENT")) {
                    final Component vevent = (Component) componentObj;
                    try {
                        appointments.add(convertAppointment(i++, (VEvent)vevent, defaultTZ, ctx, warnings ));
                    } catch (final ConversionError conversionError) {
                        errors.add(conversionError);
                    }
                }
            }
            
        } catch (final UnsupportedEncodingException e) {
            // IGNORE
        }



        return appointments;
    }

    public List<Task> parseTasks(final String icalText, final TimeZone defaultTZ, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        try {
            return parseTasks(new ByteArrayInputStream(icalText.getBytes(UTF8)), defaultTZ, ctx, errors, warnings);
        } catch (final UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
        return new LinkedList<Task>();
    }

    public List<Task> parseTasks(final InputStream ical, final TimeZone defaultTZ, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        final List<Task> tasks = new ArrayList<Task>();
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(ical, UTF8));
            while(true) {
                final net.fortuna.ical4j.model.Calendar calendar = parse(reader);
                if(calendar == null) { break; }
                int i = 0;
                for(final Object componentObj : calendar.getComponents("VTODO")) {
                    final Component vtodo = (Component) componentObj;
                    try {
                        tasks.add(convertTask(i++, (VToDo) vtodo, defaultTZ, ctx, warnings ));
                    } catch (final ConversionError conversionError) {
                        errors.add(conversionError);
                    }
                }
            }

        } catch (final UnsupportedEncodingException e) {
            // IGNORE
        }


        return tasks;
    }


    protected CalendarDataObject convertAppointment(final int index, final VEvent vevent, final TimeZone defaultTZ, final Context ctx, final List<ConversionWarning> warnings) throws ConversionError {

        final CalendarDataObject appointment = new CalendarDataObject();

        final TimeZone tz = determineTimeZone(vevent, defaultTZ);

        for (final AttributeConverter<VEvent, AppointmentObject> converter : AppointmentConverters.ALL) {
            if (converter.hasProperty(vevent)) {
                converter.parse(index, vevent, appointment, tz, ctx, warnings);
            }
            converter.verify(index, appointment, warnings);
        }

        
        
        appointment.setTimezone(getTimeZoneID(tz));

        return appointment;
    }

    protected Task convertTask(final int index, final VToDo vtodo, final TimeZone defaultTZ, final Context ctx, final List<ConversionWarning> warnings) throws ConversionError{
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
        for (final String name : new String[] { DtStart.DTSTART, DtEnd.DTEND, Due.DUE, Completed.COMPLETED }) {
            final DateProperty dateProp = (DateProperty) component.getProperty(name);
            if (dateProp != null) {
                return chooseTimeZone(dateProp, defaultTZ);
            }
        }

        return null;
    }

    private static final TimeZone chooseTimeZone(final DateProperty dateProperty, final TimeZone defaultTZ) {
        TimeZone tz = defaultTZ;
        if (dateProperty.isUtc()) {
            tz = TimeZone.getTimeZone("UTC");
        }
        final TimeZone inTZID = (null != dateProperty.getParameter("TZID")) ? TimeZone.getTimeZone(dateProperty.getParameter("TZID").getValue()) : null;
        if (null != inTZID) {
            tz = inTZID;
        }
        return tz;
    }

    private String getTimeZoneID(final TimeZone tz) {
        if(net.fortuna.ical4j.model.TimeZone.class.isAssignableFrom(tz.getClass())) {
            return "UTC";
        }
        if(tz.getID().equals("GMT")) { // Hack for VTIMEZONE. iCal4J sets timezone to GMT, though we prefer UTC
            return "UTC";
        }
        return tz.getID();
    }

    private net.fortuna.ical4j.model.Calendar parse(final BufferedReader reader) throws ConversionError {
        final CalendarBuilder builder = new CalendarBuilder();

        try {
            final StringBuilder chunk = new StringBuilder();
            String line;
            boolean read = false;
            boolean timezoneStarted = false; //hack to fix bug 11958 
            boolean timezoneEnded = false; //hack to fix bug 11958
            boolean timezoneRead = false; //hack to fix bug 11958
            final StringBuilder timezoneInfo = new StringBuilder(); //hack to fix bug 11958
            // Copy until we find an END:VCALENDAR
            boolean beginFound = false;
            while((line = reader.readLine()) != null) {
                if(line.startsWith("BEGIN:VCALENDAR")) {
                    beginFound = true;
                } else if ( !beginFound && !"".equals(line)) {
                    throw new ConversionError(-1, ConversionWarning.Code.DOES_NOT_LOOK_LIKE_ICAL_FILE);
                }
                if(!line.startsWith("END:VCALENDAR")){ //hack to fix bug 11958
                	if(line.matches("^\\s*BEGIN:VTIMEZONE")){
                		timezoneStarted = true;
                	}
                    if(!line.matches("\\s*")) {
                        read = true;
                        if(timezoneStarted && !timezoneEnded){ //hack to fix bug 11958
                        	timezoneInfo.append(line).append("\n");
                        } else {
                        	chunk.append(line).append("\n");
                        }
                    }
                	if(line.matches("^\\s*END:VTIMEZONE")){ //hack to fix bug 11958
                		timezoneEnded = true;
                		timezoneRead = true && timezoneStarted;
                	}
                } else {
                    break;
                }
            }
            if(!read) {  return null; }
            chunk.append("END:VCALENDAR\n");
            if(timezoneRead){
            	int locationForInsertion = chunk.indexOf("BEGIN:");
            	if(locationForInsertion > -1){
            		locationForInsertion = chunk.indexOf("BEGIN:", locationForInsertion + 1);
            		if(locationForInsertion > -1){
            			chunk.insert(locationForInsertion, timezoneInfo);
            		}
            	}
            }
            return builder.build(new StringReader(chunk.toString())); // FIXME: Encoding!
        } catch (final IOException e) {
            //IGNORE
        } catch (final ParserException e) {
            LOG.warn(e.getMessage(), e);
            throw new ConversionError(-1, ConversionWarning.Code.PARSE_EXCEPTION, e.getMessage());
        }
        return null;
    }

}