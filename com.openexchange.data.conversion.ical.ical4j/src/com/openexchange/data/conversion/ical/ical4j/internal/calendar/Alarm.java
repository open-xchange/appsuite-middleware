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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Trigger;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Alarm<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T,U> {
    @Override
    public boolean isSet(final U calendar) {
        return true;
    }

    @Override
    public void emit(final Mode mode, final int index, final U calendar, final T component, final List<ConversionWarning> warnings, final Context ctx, final Object... args) throws ConversionError {
        if(Task.class.isAssignableFrom(calendar.getClass())) {
            emitTaskAlarm((Task)calendar, (VToDo) component, warnings);
        }  else if ( Appointment.class.isAssignableFrom(calendar.getClass())) {
            emitAppointmentAlarm((Appointment)calendar, (VEvent) component, warnings);
        }
    }

    private void emitAppointmentAlarm(final Appointment appointmentObject, final VEvent component, final List<ConversionWarning> warnings) {
        if (false == appointmentObject.containsAlarm() || 0 > appointmentObject.getAlarm()) {
            return;
        }
        final VAlarm alarm = new VAlarm();
        final Dur duration = new Dur(String.format("%sPT%dM",
            0 == appointmentObject.getAlarm() ? "" : "-", Integer.valueOf(appointmentObject.getAlarm())));
        final Trigger trigger = new Trigger(duration);
        alarm.getProperties().add(trigger);

        final Action action = new Action("DISPLAY");
        alarm.getProperties().add(action);

        String note = appointmentObject.getNote();
        if(note == null) { note = "Open-XChange"; }

        final Description description = new Description(note);
        alarm.getProperties().add(description);

        component.getAlarms().add(alarm);


    }

    private void emitTaskAlarm(final Task task, final VToDo component, final List<ConversionWarning> warnings) {
        if(task.getAlarm() == null) {
            return;
        }
        final VAlarm alarm = new VAlarm();
        final Trigger trigger = new Trigger(EmitterTools.toDateTime(task.getAlarm()));
        alarm.getProperties().add(trigger);

        final Action action = new Action("DISPLAY");
        alarm.getProperties().add(action);

        String note = task.getNote();
        if(note == null) { note = "Open-XChange"; }

        final Description description = new Description(note);
        alarm.getProperties().add(description);

        component.getAlarms().add(alarm);
    }


    @Override
    public boolean hasProperty(final T t) {
        return true; // Not strictly true, but to inlcude the warning we have to enter #parse always
    }

    @Override
    public void parse(final int index, final T component, final U calendarObject, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) throws ConversionError {
        /*
         * get relevant alarm trigger
         */
        Trigger trigger = getTrigger(index, component, warnings);
        if (null != trigger) {
            /*
             * determine related date
             */
            Parameter relatedParameter = trigger.getParameter("RELATED");
            Date relatedDate = null != relatedParameter && "END".equals(relatedParameter.getValue()) ?
                calendarObject.getEndDate() : calendarObject.getStartDate();
            if (Appointment.class.isAssignableFrom(calendarObject.getClass())) {
                /*
                 * set relative alarm timespan
                 */
                Long duration = parseTriggerDuration(index, trigger, relatedDate, timeZone, warnings);
                if (null != duration) {
                    Appointment appointment = (Appointment)calendarObject;
                    appointment.setAlarmFlag(true);
                    appointment.setAlarm((int)(duration.longValue() / 60));
                }
            } else if (Task.class.isAssignableFrom(calendarObject.getClass())) {
                /*
                 * set absolute alarm date-time
                 */
                Date date = parseTriggerDate(index, trigger, relatedDate, timeZone, warnings);
                if (null != date) {
                    Task task = (Task)calendarObject;
                    task.setAlarmFlag(true);
                    task.setAlarm(date);
                }
            } else {
                warnings.add(new ConversionWarning(index, "Can only parse alarms for appointments and tasks"));
            }
        }
    }

    private Trigger getTrigger(int index, T component, List<ConversionWarning> warnings) {
        /*
         * check mozilla x-props
         */
        if (null != component.getProperty("X-MOZ-LASTACK")) {
            Property mozillaSnoozeTime = component.getProperty("X-MOZ-SNOOZE-TIME");
            if (null == mozillaSnoozeTime) {
                /*
                 * alarm is acknowledged and not snoozed, so don't import vAlarm at all
                 */
                return null;
            } else {
                /*
                 * snooze - 'remind again'
                 */
                return new Trigger(mozillaSnoozeTime.getParameters(), mozillaSnoozeTime.getValue());
            }
        }
        /*
         * check default vAlarm
         */
        ComponentList alarms = null;
        if (VEvent.class.isAssignableFrom(component.getClass())) {
            alarms = ((VEvent)component).getAlarms();
        } else if (VToDo.class.isAssignableFrom(component.getClass())) {
            alarms = ((VToDo)component).getAlarms();
        } else {
            warnings.add(new ConversionWarning(index, "Can only extract alarms from VTODO or VEVENT components"));
        }
        if (null != alarms) {
            final int size = alarms.size();
            for (int i = 0; i < size; i++) {
                final VAlarm alarm = (VAlarm)alarms.get(i);
                if (null != alarm) {
                    final Trigger trigger = alarm.getTrigger();
                    if (null != trigger) {
                        final Action action = alarm.getAction();
                        if (null != action && "DISPLAY".equalsIgnoreCase(action.getValue())) {
                            return trigger;
                        }
                        warnings.add(new ConversionWarning(index, "Can only convert DISPLAY alarms with triggers"));
                    }
                }
            }
        }

        return null;
    }

    /**
     * Parses the alarm trigger as absolute date-time.
     *
     * @param index the current conversion index
     * @param trigger the iCal trigger property
     * @param start the date where the trigger is targeted at
     * @param timeZone the default timezone
     * @param warnings the conversion warnings
     * @return the duration, or <code>null</code> if none was found
     */
    private java.util.Date parseTriggerDate(int index, Trigger trigger, java.util.Date start, TimeZone timeZone, List<ConversionWarning> warnings) {
        Dur duration = trigger.getDuration();
        if (null != duration) {
            if (false == duration.isNegative() && false == new Dur("PT0S").equals(duration)) {
                warnings.add(new ConversionWarning(index, "Ignoring non-negative duration for alarm trigger"));
                return null;
            } else if (null == start) {
                warnings.add(new ConversionWarning(index, "Need corresponding time for relative trigger"));
                return null;
            } else {
                return duration.getTime(start);
            }
        } else if (null != trigger.getDateTime()) {
            return ParserTools.recalculateAsNeeded(trigger.getDateTime(), trigger, timeZone);
        } else {
            return null;
        }
    }

    /**
     * Parses the alarm trigger as relative duration in seconds.
     *
     * @param index the current conversion index
     * @param trigger the iCal trigger property
     * @param start the date where the trigger is targeted at
     * @param timeZone the default timezone
     * @param warnings the conversion warnings
     * @return the duration, or <code>null</code> if none was found
     */
    private Long parseTriggerDuration(int index, Trigger trigger, java.util.Date start, TimeZone timeZone, List<ConversionWarning> warnings) {
        Dur duration = trigger.getDuration();
        if (null != duration) {
            if (false == duration.isNegative() && false == new Dur("PT0S").equals(duration)) {
                warnings.add(new ConversionWarning(index, "Ignoring non-negative duration for alarm trigger"));
                return null;
            } else {
                return new Long((((duration.getWeeks() * 7 + duration.getDays()) * 24  + duration.getHours() ) * 60 + duration.getMinutes()) * 60 + duration.getSeconds());
            }
        } else if (null != trigger.getDateTime()) {
            java.util.Date date = ParserTools.recalculateAsNeeded(trigger.getDateTime(), trigger, timeZone);
            return new Long((start.getTime() - date.getTime()) / 1000);
        } else {
            return null;
        }
    }

}
