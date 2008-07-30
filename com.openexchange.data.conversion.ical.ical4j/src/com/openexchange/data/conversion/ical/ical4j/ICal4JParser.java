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

import internal.AttributeConverter;
import internal.task.TaskConverters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Task;

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


    public List<AppointmentObject> parseAppointments(String icalText, TimeZone defaultTZ, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) {
        List<AppointmentObject> appointments = new ArrayList<AppointmentObject>();
        net.fortuna.ical4j.model.Calendar calendar = parse(icalText);
        for (final Object componentObj : calendar.getComponents(VEvent.VEVENT)) {
            if (componentObj instanceof VEvent) {
                final VEvent vevent = (VEvent) componentObj;
                appointments.add(convertAppointment(vevent, defaultTZ, ctx));
            } else {
                // FIXME
            }
        }
        return appointments;
    }

    /**
     * {@inheritDoc}
     */
    public List<Task> parseTasks(final String icalText, final TimeZone defaultTZ, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) {
        List<Task> tasks = new ArrayList<Task>();
        net.fortuna.ical4j.model.Calendar calendar = parse(icalText);
        for (final Object componentObj : calendar.getComponents(VToDo.VTODO)) {
            if (componentObj instanceof VToDo) {
                final VToDo vtodo = (VToDo) componentObj;
                tasks.add(convertTask( vtodo, defaultTZ, ctx));
            } else {
                // FIXME
            }
        }
        return tasks;
    }


    protected AppointmentObject convertAppointment(final VEvent vevent, TimeZone defaultTZ, Context ctx) {

        AppointmentObject appointment = new AppointmentObject();

        TimeZone tz = determineTimeZone(vevent, defaultTZ);

        setStart(appointment, vevent, tz);
        setEnd(appointment, vevent, tz);
        applyDuration(appointment, vevent);
        applyClass(appointment, vevent);
        setTitle(appointment, vevent);
        setDescription(appointment, vevent);
        setLocation(appointment,  vevent);
        applyTransparency(appointment, vevent);
        setParticipants(appointment, vevent, ctx);
        setCategories(appointment, vevent);
        setRecurrence(appointment, vevent, tz);
        setDeleteExceptions(appointment, vevent, tz);

        setAlarm(appointment, vevent, tz);

        appointment.setTimezone(getTimeZoneID(tz));

        return appointment;
    }

    protected Task convertTask(VToDo vtodo, TimeZone defaultTZ, Context ctx) {
        final TimeZone tz = determineTimeZone(vtodo, defaultTZ);
        final Task task = new Task();
        for (final AttributeConverter<Task> converter : TaskConverters.ALL) {
            if (converter.hasProperty(vtodo)) {
                converter.parse(vtodo, task);
            }
        }

        setStart(task, vtodo, tz);
        setEnd(task, vtodo, tz);
        if(null == task.getEndDate())  {
            setDueDate(task, vtodo, tz);
        }
        setDateCompleted(task, vtodo, tz);
        applyDuration(task, vtodo);
        applyClass(task, vtodo);
        setParticipants(task, vtodo, ctx);
        setCategories(task, vtodo);
        setRecurrence(task, vtodo, tz);
        setDeleteExceptions(task, vtodo, tz);
        setAlarm(task, vtodo, tz);

        setPercentComplete(task, vtodo);
        setPriority(task, vtodo);
        setStatus(task, vtodo);

        return task;
    }

    private void setStart(CalendarObject cObj, Component component, TimeZone tz) {
        DtStart start = (DtStart) component.getProperty("DTSTART");
        if(null == start) {
            return;
        }
        Date startDate = toDate(start, tz);
        cObj.setStartDate(startDate);
    }

    private void setEnd(CalendarObject cObj, Component component, TimeZone tz) {
        DtEnd end = (DtEnd) component.getProperty("DTEND");
        if(null == end) {
            return;
        }
        Date endDate = toDate(end, tz);
        cObj.setEndDate(endDate);
    }

    private void setDueDate(Task task, Component component, TimeZone tz) {
        Due due = (Due) component.getProperty("DUE");
        if(null == due) {
            return;
        }
        Date endDate = toDate(due, tz);
        task.setEndDate(endDate);
    }

    private void setDateCompleted(Task task, Component  component, TimeZone tz) {
        Completed completed = (Completed) component.getProperty("COMPLETED");
        if(null == completed) {
            return;
        }
        Date completedDate = toDate(completed,tz);
        task.setDateCompleted(completedDate);
    }

    private void setPercentComplete(Task task, Component component) {
        VToDo todo = (VToDo) component;
        if(null == todo.getPercentComplete()) {
            return;
        }
        int percentage = todo.getPercentComplete().getPercentage();
        task.setPercentComplete(percentage);
    }

    private void setPriority(Task task, Component component) {
        VToDo todo = (VToDo) component;
        if(null == todo.getPriority()) {
            return;
        }
        int priority = todo.getPriority().getLevel();
        if(priority < 5) {
            task.setPriority(Task.HIGH);
            return;
        }
        if(priority > 5) {
            task.setPriority(Task.LOW);
            return;
        }
        task.setPriority(Task.NORMAL);
    }

    private void setStatus(Task task, Component component) {
        VToDo todo = (VToDo) component;
        if(null == todo.getStatus()) {
            return;
        }
        String status = todo.getStatus().getValue();
        if (status.equals("NEEDS-ACTION")) {
            task.setStatus(Task.NOT_STARTED);
        } else if (status.equals("IN-PROCESS")) {
            task.setStatus(Task.IN_PROGRESS);
        } else if (status.equals("COMPLETED")) {
            task.setStatus(Task.DONE);
        } else if (status.equals("CANCELLED")) {
            task.setStatus(Task.DEFERRED);
        }
    }

    private void applyDuration(CalendarObject cObj,Component component) {
        Duration duration = (Duration) component.getProperty("Duration");
        if(duration == null) {
            return;
        }
        Date endDate = duration.getDuration().getTime(cObj.getStartDate());
        cObj.setEndDate(endDate);
    }

    private void applyClass(CalendarObject cObj, Component component) {
        if(component.getProperty("CLASS") != null) {
            cObj.setPrivateFlag("private".equals(component.getProperty("CLASS").getValue()));
        }
    }

    private void setDescription(CalendarObject cObj, Component component) {
        if(component.getProperty("DESCRIPTION") != null) {
            cObj.setNote(component.getProperty("DESCRIPTION").getValue());
        }
    }

    private void setLocation(AppointmentObject appointment, Component vevent) {
        if(vevent.getProperty("LOCATION") != null) {
            appointment.setLocation(vevent.getProperty("LOCATION").getValue());
        }
    }

    private void setTitle(CalendarObject cObj, Component component) {
        if(component.getProperty("SUMMARY") != null) {
            cObj.setTitle(component.getProperty("SUMMARY").getValue());
        }
    }

    private void applyTransparency(AppointmentObject appointment, Component vevent) {
        if(vevent.getProperty("TRANSP") != null) {
            String value = vevent.getProperty("TRANSP").getValue().toLowerCase();
            if(value.equals("opaque"))  {
                appointment.setShownAs(AppointmentObject.RESERVED);
            } else if (value.equals("transparent")) {
                appointment.setShownAs(AppointmentObject.FREE);
            }
        }
    }

    private void setParticipants(CalendarObject cObj, Component component, Context ctx) {

        PropertyList properties = component.getProperties("ATTENDEE");
        List<String> mails = new LinkedList<String>();

        for(int i = 0, size = properties.size(); i < size; i++) {
            Attendee attendee = (Attendee) properties.get(i);
            URI uri = attendee.getCalAddress();
            if("mailto".equalsIgnoreCase(uri.getScheme())) {
                String mail = uri.getSchemeSpecificPart();
                mails.add( mail );
            }
        }

        List<User> users = findUsers(mails, ctx);

        for(User user : users) {
            cObj.addParticipant( new UserParticipant(user.getId()) );
            mails.remove(user.getMail());
        }

        for(String mail : mails) {
            ExternalUserParticipant external = new ExternalUserParticipant(mail);
            external.setDisplayName(null);
            cObj.addParticipant(external);
        }

        PropertyList resourcesList = component.getProperties("RESOURCES");
        for(int i = 0, size = resourcesList.size(); i < size; i++) {
            Resources resources = (Resources) resourcesList.get(i);
            for(Iterator<Object> resObjects = resources.getResources().iterator(); resObjects.hasNext();) {
                ResourceParticipant participant = new ResourceParticipant();
                participant.setDisplayName(resObjects.next().toString());
                cObj.addParticipant(participant);
            }

        }

    }

    private void setCategories(CalendarObject cObj, Component component) {
        PropertyList categoriesList = component.getProperties("CATEGORIES");
        StringBuilder bob = new StringBuilder();
        for(int i = 0, size = categoriesList.size(); i < size; i++) {
            Categories categories = (Categories) categoriesList.get(i);
            for(Iterator<Object> catObjects = categories.getCategories().iterator(); catObjects.hasNext();) {
                bob.append(catObjects.next()).append(",");
            }
        }
        if(bob.length() > 0) {
            bob.setLength(bob.length()-1);
        }
        cObj.setCategories(bob.toString());
    }

    private void setRecurrence(CalendarObject cObj, Component component, TimeZone tz) {
        if(null == cObj.getStartDate()) {
            return;
        }
        Calendar startDate = new GregorianCalendar();
        startDate.setTime(cObj.getStartDate());

        PropertyList list = component.getProperties("RRULE");
        if(list.isEmpty()) {
            return;
        }
        Recur rrule = ((RRule) list.get(0)).getRecur();

        if("DAILY".equalsIgnoreCase(rrule.getFrequency())) {
            cObj.setRecurrenceType(AppointmentObject.DAILY);
        } else if ("WEEKLY".equalsIgnoreCase(rrule.getFrequency())) {
            cObj.setRecurrenceType(AppointmentObject.WEEKLY);
            setDays(cObj, rrule, startDate);
        } else if ("MONTHLY".equalsIgnoreCase(rrule.getFrequency())) {
            cObj.setRecurrenceType(AppointmentObject.MONTHLY);
            setMonthDay(cObj, rrule, startDate);
        } else if ("YEARLY".equalsIgnoreCase(rrule.getFrequency())) {
            cObj.setRecurrenceType(AppointmentObject.YEARLY);
            NumberList monthList = rrule.getMonthList();
            if(!monthList.isEmpty()) {
                cObj.setMonth((Integer)monthList.get(0) - 1);
                setMonthDay(cObj, rrule, startDate);
            } else {
                cObj.setMonth(startDate.get(Calendar.MONTH));
                setMonthDay(cObj, rrule, startDate);

            }


        }
        cObj.setInterval(rrule.getInterval());
        int count = rrule.getCount();
        if(-1 != count) {
            cObj.setRecurrenceCount(rrule.getCount());
        } else {
            cObj.setUntil(recalculate(new Date(rrule.getUntil().getTime()), tz));
        }

    }

    private void setMonthDay(CalendarObject cObj, Recur rrule, Calendar startDate) {
        NumberList monthDayList = rrule.getMonthDayList();
        if(!monthDayList.isEmpty()) {
            cObj.setDayInMonth((Integer)monthDayList.get(0));
        } else {
            NumberList weekNoList = rrule.getWeekNoList();
            if(!weekNoList.isEmpty()) {
                int week = (Integer)weekNoList.get(0);
                if(week == -1) { week = 5; }
                cObj.setDayInMonth(week); // Day in month stores week
                setDays(cObj, rrule, startDate);
            } else {
                // Default to monthly series on specific day of month
                cObj.setDayInMonth(startDate.get(Calendar.DAY_OF_MONTH));
            }
        }
    }

    private void setDays(CalendarObject cObj, Recur rrule, Calendar startDate) {
        WeekDayList weekdayList = rrule.getDayList();
        if(!weekdayList.isEmpty()) {
            int days = 0;
            for(int i = 0, size = weekdayList.size(); i < size; i++) {
                WeekDay weekday = (WeekDay) weekdayList.get(i);
                days |= weekdays.get(weekday.getDay());
            }
            cObj.setDays(days);
        } else {
            int day_of_week = startDate.get(Calendar.DAY_OF_WEEK);
            int days = -1;
            switch(day_of_week) {
                case Calendar.MONDAY : days = AppointmentObject.MONDAY; break;
                case Calendar.TUESDAY : days = AppointmentObject.TUESDAY; break;
                case Calendar.WEDNESDAY : days = AppointmentObject.WEDNESDAY; break;
                case Calendar.THURSDAY : days = AppointmentObject.THURSDAY; break;
                case Calendar.FRIDAY : days = AppointmentObject.FRIDAY; break;
                case Calendar.SATURDAY : days = AppointmentObject.SATURDAY; break;
                case Calendar.SUNDAY : days = AppointmentObject.SUNDAY; break;
            }
            cObj.setDays(days);
        }
    }

    private void setDeleteExceptions(CalendarObject cObj, Component component, TimeZone tz) {
        PropertyList exdates = component.getProperties("EXDATE");
        for(int i = 0, size = exdates.size(); i < size; i++) {
            ExDate exdate = (ExDate) exdates.get(0);
            
            DateList dates = exdate.getDates();
            for(int j = 0, size2 = dates.size(); j < size2; j++) {
                net.fortuna.ical4j.model.Date icaldate = (net.fortuna.ical4j.model.Date) dates.get(j);
                Date date = recalculateAsNeeded(icaldate, exdate, tz);
                cObj.addDeleteException(date);
            }
        }
    }

    private Date recalculateAsNeeded(net.fortuna.ical4j.model.Date icaldate, Property property, TimeZone tz) {
        boolean mustRecalculate = true;
        if(property.getParameter("TZID") != null) {
            mustRecalculate = false;
        } else if(DateTime.class.isAssignableFrom(icaldate.getClass())) {
            DateTime dateTime = (DateTime) icaldate;
            mustRecalculate = !dateTime.isUtc();
        }
        if(mustRecalculate) {
            return recalculate(icaldate, tz);
        }
        return new Date(icaldate.getTime());
    }

    private void setAlarm(CalendarObject cObj, Component component, TimeZone tz) {

        VAlarm alarm = getAlarm(component);

        if(alarm == null) {
            return;
        }


        net.fortuna.ical4j.model.Date icaldate = alarm.getTrigger().getDateTime();
        if(null == icaldate) {
            icaldate = alarm.getTrigger().getDate();
        }

        Date remindOn = null;

        if(null != icaldate) {
            remindOn = recalculateAsNeeded(icaldate, alarm.getTrigger(), tz);
        } else {
            Dur duration = alarm.getTrigger().getDuration();
            if(!duration.isNegative()) {
                return;
            }
            remindOn = duration.getTime(cObj.getStartDate());
        }

        int delta = (int) (cObj.getStartDate().getTime() - remindOn.getTime());

        if(AppointmentObject.class.isAssignableFrom(cObj.getClass())) {
            final AppointmentObject appObj = (AppointmentObject) cObj;
            appObj.setAlarm(delta);
            appObj.setAlarmFlag(true); // bugfix: 7473
        } else {
            final Task taskObj = (Task) cObj;
            taskObj.setAlarm(new Date(taskObj.getStartDate().getTime() - delta));
            taskObj.setAlarmFlag(true); // bugfix: 7473
        }

    }

    private VAlarm getAlarm(Component component) {
        ComponentList alarms = null;
        if(VEvent.class.isAssignableFrom(component.getClass())) {
            VEvent event = (VEvent) component;
            alarms = event.getAlarms();
        } else if (VToDo.class.isAssignableFrom(component.getClass())) {
            VToDo todo = (VToDo) component;
            alarms = todo.getAlarms();
        }

        if(alarms.size() == 0) {
            return null;
        }

        for(int i = 0, size = alarms.size(); i < size; i++) {
            VAlarm alarm = (VAlarm) alarms.get(0);
            if("DISPLAY".equalsIgnoreCase(alarm.getAction().getValue())) {
                return alarm;
            }

        }
        return null;
     }

    private Date toDate(DateProperty dateProperty, TimeZone tz) {
        Date date = new Date(dateProperty.getDate().getTime());
        if(inDefaultTimeZone(dateProperty, tz)) {
            date = recalculate(date, tz);
        }
        return date;
    }

    private TimeZone determineTimeZone(final CalendarComponent component, TimeZone defaultTZ) {

        for(String name : new String[]{"DTSTART", "DTEND", "DUE", "COMPLETED"}) {
            DateProperty dateProp = (DateProperty) component.getProperty(name);
            if(dateProp != null) {
                return chooseTimeZone(dateProp, defaultTZ);
            }
        }
        
        return null;
    }

    private TimeZone chooseTimeZone(DateProperty start, TimeZone defaultTZ) {
        TimeZone tz = defaultTZ;
        if(start.isUtc()) {
            tz = TimeZone.getTimeZone("UTC");
        }

        TimeZone inTZID = (null != start.getParameter("TZID")) ? TimeZone.getTimeZone(start.getParameter("TZID").getValue()) : null;
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

    private boolean inDefaultTimeZone(DateProperty dateProperty, TimeZone tz) {
        if(dateProperty.getParameter("TZID") != null) {
            return false;
        }
        return !dateProperty.isUtc();
    }

    // Transforms date from the default timezone to the date in the given timezone.
    private Date recalculate(Date date, TimeZone tz) {

        java.util.Calendar inDefault = new GregorianCalendar();
        inDefault.setTime(date);

        java.util.Calendar inTimeZone = new GregorianCalendar();
        inTimeZone.setTimeZone(tz);
        inTimeZone.set(inDefault.get(java.util.Calendar.YEAR), inDefault.get(java.util.Calendar.MONTH), inDefault.get(java.util.Calendar.DATE), inDefault.get(java.util.Calendar.HOUR_OF_DAY), inDefault.get(java.util.Calendar.MINUTE), inDefault.get(java.util.Calendar.SECOND));
        inTimeZone.set(java.util.Calendar.MILLISECOND, 0);
        return inTimeZone.getTime();
    }



    private net.fortuna.ical4j.model.Calendar parse(String icalText) {
        CalendarBuilder builder = new CalendarBuilder();
        
        try {
            return builder.build(new ByteArrayInputStream(icalText.getBytes("UTF-8"))); // FIXME: Encoding!
        } catch (IOException e) {
            //IGNORE
        } catch (ParserException e) {
            System.out.println(icalText);
            e.printStackTrace();
            //TODO: Rethrow
        }
        return null;
    }
    
    protected List<User> findUsers(List<String> mails, Context ctx) {
        return null; // TODO
    }
}