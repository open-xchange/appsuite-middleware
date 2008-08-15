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
package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Description;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionError;

import java.util.List;
import java.util.TimeZone;
import java.util.Date;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Alarm<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T,U> {
    public boolean isSet(U calendar) {
        return true;
    }

    public void emit(int index, U calendar, T component, List<ConversionWarning> warnings, Context ctx) throws ConversionError {
        if(Task.class.isAssignableFrom(calendar.getClass())) {
            emitTaskAlarm((Task)calendar, (VToDo) component, warnings);
        }  else if ( AppointmentObject.class.isAssignableFrom(calendar.getClass())) {
            emitAppointmentAlarm((AppointmentObject)calendar, (VEvent) component, warnings);
        }
    }

    private void emitAppointmentAlarm(AppointmentObject appointmentObject, VEvent component, List<ConversionWarning> warnings) {
        if(0 >= appointmentObject.getAlarm()) {
            return;
        }
        VAlarm alarm = new VAlarm();
        Dur duration = new Dur(String.format("-PT%dM", appointmentObject.getAlarm()));
        Trigger trigger = new Trigger(duration);
        alarm.getProperties().add(trigger);

        Action action = new Action("DISPLAY");
        alarm.getProperties().add(action);

        String note = appointmentObject.getNote();
        if(note == null) { note = "Open-XChange"; }

        Description description = new Description(note);
        alarm.getProperties().add(description);

        component.getAlarms().add(alarm);

        
    }

    private void emitTaskAlarm(Task task, VToDo component, List<ConversionWarning> warnings) {
        if(task.getAlarm() == null) {
            return;
        }
        VAlarm alarm = new VAlarm();
        Trigger trigger = new Trigger(EmitterTools.toDateTime(task.getAlarm()));
        alarm.getProperties().add(trigger);

        Action action = new Action("DISPLAY");
        alarm.getProperties().add(action);

        String note = task.getNote();
        if(note == null) { note = "Open-XChange"; }

        Description description = new Description(note);
        alarm.getProperties().add(description);

        component.getAlarms().add(alarm);
    }


    public boolean hasProperty(T t) {
        return true; // Not strictly true, but to inlcude the warning we have to enter #parse always
    }

    public void parse(int index, T component, U cObj, TimeZone timeZone, Context ctx, List<ConversionWarning> warnings) throws ConversionError {
       VAlarm alarm = getAlarm(index, component, warnings);

        if(alarm == null) {
            return;
        }

        net.fortuna.ical4j.model.Date icaldate = alarm.getTrigger().getDateTime();
        if(null == icaldate) {
            icaldate = alarm.getTrigger().getDate();
        }

        Date remindOn = null;

        if(null != icaldate) {
            remindOn = ParserTools.recalculateAsNeeded(icaldate, alarm.getTrigger(), timeZone);
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
            appObj.setAlarm(delta / 60000);
            appObj.setAlarmFlag(true); // bugfix: 7473
        } else {
            final Task taskObj = (Task) cObj;
            taskObj.setAlarm(new Date(taskObj.getStartDate().getTime() - delta));
            taskObj.setAlarmFlag(true); // bugfix: 7473
        }
    }

    private VAlarm getAlarm(int index, Component component, List<ConversionWarning> warnings) {
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
            
            if(null != alarm.getTrigger() && "DISPLAY".equalsIgnoreCase(alarm.getAction().getValue())) {
                return alarm;
            }
            warnings.add(new ConversionWarning(index, "Can only convert DISPLAY alarms with triggers"));

        }
        return null;
    }
    
}
