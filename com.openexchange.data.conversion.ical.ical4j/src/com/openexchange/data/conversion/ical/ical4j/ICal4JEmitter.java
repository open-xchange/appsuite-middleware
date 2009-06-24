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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;

import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalItem;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.data.conversion.ical.ConversionWarning.Code;
import com.openexchange.data.conversion.ical.ical4j.internal.AppointmentConverters;
import com.openexchange.data.conversion.ical.ical4j.internal.AttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.TaskConverters;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ICal4JEmitter implements ICalEmitter {

    public String writeAppointments(final List<Appointment> appointmentObjects, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) {
        final Calendar calendar = new Calendar();
        initCalendar(calendar);
        int i = 0;
        for(final Appointment appointment : appointmentObjects) {
            final VEvent event = createEvent(i++, appointment, ctx, errors, warnings);
            calendar.getComponents().add(event);
        }
        return calendar.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String writeTasks(final List<Task> tasks,
        final List<ConversionError> errors,
        final List<ConversionWarning> warnings, final Context ctx) {
        final Calendar calendar = new Calendar();
        initCalendar(calendar);
        int i = 0;
        for (final Task task : tasks) {
            final VToDo vtodo;
            try {
                vtodo = createEvent(i++, task, ctx, warnings);
                calendar.getComponents().add(vtodo);
            } catch (final ConversionError conversionError) {
                errors.add( conversionError );
            }
        }
        return calendar.toString();
    }

    private VEvent createEvent(final int index, final Appointment appointment, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) {

        final VEvent vevent = new VEvent();
        for (final AttributeConverter<VEvent, Appointment> converter : AppointmentConverters.ALL) {
            if (converter.isSet(appointment)) {
                try {
                    converter.emit(index, appointment, vevent, warnings, ctx);
                } catch (final ConversionError conversionError) {
                    errors.add( conversionError );
                }
            }
        }

        return vevent;
    }

    /**
     * Converts a task object into an iCal event.
     * @param task task to convert.
     * @return the iCal event representing the task.
     */
    private VToDo createEvent(final int index, final Task task, final Context ctx, final List<ConversionWarning> warnings) throws ConversionError {
        final VToDo vtodo = new VToDo();
        for (final AttributeConverter<VToDo, Task> converter : TaskConverters.ALL) {
            if (converter.isSet(task)) {
                converter.emit(index, task, vtodo, warnings, ctx);
            }
        }
        return vtodo;
    }

    public ICalSession createSession() {
        final ICal4jSession retval = new ICal4jSession();
        initCalendar(retval.getCalendar());
        return retval;
    }

    public ICalItem writeAppointment(final ICalSession session,
        final Appointment appointment, final Context ctx,
        final List<ConversionError> errors, final List<ConversionWarning> warnings)
        throws ConversionError {
        final Calendar calendar = getCalendar(session);
        final VEvent event = createEvent(getAndIncreaseIndex(session), appointment,ctx, errors, warnings);
        calendar.getComponents().add(event);
        return new ICal4jItem(event);
    }


    public void writeSession(final ICalSession session, final OutputStream stream) throws ConversionError {
        final Calendar calendar = getCalendar(session);
        final CalendarOutputter outputter = new CalendarOutputter(false);
        try {
            outputter.output(calendar, stream);
        } catch (final IOException e) {
            throw new ConversionError(-1, Code.WRITE_PROBLEM, e);
        } catch (final ValidationException e) {
            throw new ConversionError(-1, Code.VALIDATION, e);
        }
    }

    public ICalItem writeTask(final ICalSession session, final Task task,
        final Context context, final List<ConversionError> errors,
        final List<ConversionWarning> warnings) throws ConversionError {
        final Calendar calendar = getCalendar(session);
        final VToDo vToDo = createEvent(getAndIncreaseIndex(session), task,context, warnings);
        calendar.getComponents().add(vToDo);
        return new ICal4jItem(vToDo);
    }

    private void initCalendar(final Calendar calendar) {
        final PropertyList properties = calendar.getProperties();
        final ProdId prodId = new ProdId();
        prodId.setValue(com.openexchange.server.impl.Version.NAME);
        properties.add(prodId);
        properties.add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
        properties.add(CalScale.GREGORIAN);
        properties.add(Method.REQUEST);
    }

    private Calendar getCalendar(final ICalSession session) throws ConversionError {
        if (!(session instanceof ICal4jSession)) {
            throw new ConversionError(-1, Code.INVALID_SESSION, session.getClass().getName());
        }
        return ((ICal4jSession) session).getCalendar();
    }


    private int getAndIncreaseIndex(final ICalSession session) throws ConversionError {
        if (!(session instanceof ICal4jSession)) {
            throw new ConversionError(-1, Code.INVALID_SESSION, session.getClass().getName());
        }
        return ((ICal4jSession) session).getAndIncreaseIndex();
    }
}
