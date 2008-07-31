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

import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ical4j.internal.AttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.TaskConverters;
import com.openexchange.data.conversion.ical.ical4j.internal.AppointmentConverters;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.tasks.Task;

import java.util.List;
import java.util.Date;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ICal4JEmitter implements ICalEmitter {

    public String writeAppointments(List<AppointmentObject> appointmentObjects, List<ConversionError> errors, List<ConversionWarning> warnings) {
        Calendar calendar = new Calendar();

        for(AppointmentObject appointment : appointmentObjects) {
            VEvent event = createEvent(appointment, errors, warnings);
            calendar.getComponents().add(event);
        }

        return calendar.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String writeTasks(final List<Task> tasks,
        final List<ConversionError> errors,
        final List<ConversionWarning> warnings) {
        final Calendar calendar = new Calendar();
        for (final Task task : tasks) {
            final VToDo vtodo;
            try {
                vtodo = createEvent(task, warnings);
                calendar.getComponents().add(vtodo);
            } catch (ConversionError conversionError) {
                errors.add( conversionError );
            }
        }
        return calendar.toString();
    }

    private VEvent createEvent(AppointmentObject appointment, List<ConversionError> errors, List<ConversionWarning> warnings) {

        final VEvent vevent = new VEvent();
        for (final AttributeConverter<VEvent, AppointmentObject> converter : AppointmentConverters.ALL) {
            if (converter.isSet(appointment)) {
                try {
                    converter.emit(appointment, vevent, warnings);
                } catch (ConversionError conversionError) {
                    errors.add( conversionError );
                }
            }
        }

        vevent.getProperties().add(new DtStart());
        vevent.getStartDate().setDate(date(appointment.getStartDate()));
        vevent.getStartDate().setUtc(true);

        vevent.getProperties().add(new DtEnd());
        vevent.getEndDate().setDate(date(appointment.getEndDate()));
        vevent.getEndDate().setUtc(true);

        vevent.getProperties().add(new Summary(appointment.getTitle()));
        vevent.getProperties().add(new Description(appointment.getNote()));
        
        if(null != appointment.getCategories()) {
            vevent.getProperties().add(new Categories(appointment.getCategories()));
        }


        if( appointment.getPrivateFlag() ) {
            vevent.getProperties().add(new Clazz("private"));
        }

        return vevent;
    }

    /**
     * Converts a task object into an iCal event.
     * @param task task to convert.
     * @return the iCal event representing the task.
     */
    private VToDo createEvent(final Task task, List<ConversionWarning> warnings) throws ConversionError {
        final VToDo vtodo = new VToDo();
        for (final AttributeConverter<VToDo, Task> converter : TaskConverters.ALL) {
            if (converter.isSet(task)) {
                converter.emit(task, vtodo, warnings);
            }
        }
        return vtodo;
    }
    
    private net.fortuna.ical4j.model.Date date(Date endDate) {
        net.fortuna.ical4j.model.Date d = new net.fortuna.ical4j.model.DateTime(true);
        d.setTime(endDate.getTime());
        return d;
    }
}
