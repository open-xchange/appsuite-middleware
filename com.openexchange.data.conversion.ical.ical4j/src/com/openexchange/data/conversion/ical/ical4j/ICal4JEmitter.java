/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.data.conversion.ical.ical4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionWarning.Code;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalItem;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.SimpleMode;
import com.openexchange.data.conversion.ical.ZoneInfo;
import com.openexchange.data.conversion.ical.ical4j.internal.AttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools;
import com.openexchange.data.conversion.ical.ical4j.internal.TaskConverters;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Strings;
import com.openexchange.version.VersionService;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ICal4JEmitter implements ICalEmitter {

    @Override
    public String writeTasks(final List<Task> tasks,
        final List<ConversionError> errors,
        final List<ConversionWarning> warnings, final Context ctx) {
        final Calendar calendar = new Calendar();
        final Mode mode = new SimpleMode(ZoneInfo.FULL);
        initCalendar(calendar, mode.getMethod());
        int i = 0;
        for (final Task task : tasks) {
            final VToDo vtodo;
            try {
                vtodo = createEvent(mode, i++, task, ctx, warnings);
                calendar.getComponents().add(vtodo);
            } catch (ConversionError conversionError) {
                errors.add( conversionError );
            }
        }
        return calendar.toString();
    }

    /**
     * Converts a task object into an iCal event.
     * @param mode defines the mode how the iCal emitter should work.
     * @param task task to convert.
     * @return the iCal event representing the task.
     */
    private VToDo createEvent(final Mode mode, final int index, final Task task, final Context ctx, final List<ConversionWarning> warnings) throws ConversionError {
        final VToDo vtodo = new VToDo();
        for (final AttributeConverter<VToDo, Task> converter : TaskConverters.ALL) {
            if (converter.isSet(task)) {
                converter.emit(mode, index, task, vtodo, warnings, ctx);
            }
        }
        return vtodo;
    }

    @Override
    public ICalSession createSession(final Mode mode) {
        final ICal4jSession retval = new ICal4jSession(mode);
        initCalendar(retval.getCalendar(), mode.getMethod());
        return retval;
    }

    @Override
    public ICalSession createSession() {
        return createSession(new SimpleMode(ZoneInfo.FULL));
    }

    protected boolean addVTimeZone(ZoneInfo zoneInfo, Calendar calendar, Appointment appointment) {
        return addVTimeZone(zoneInfo, calendar, appointment.getTimezone());
    }

    @SuppressWarnings("unchecked")
    private boolean addVTimeZone(ZoneInfo zoneInfo, Calendar calendar, String tzid) {
        if (Strings.isNotEmpty(tzid)) {
            for (Object o : calendar.getComponents(Component.VTIMEZONE)) {
                Component vtzComponent = (Component) o;
                if (vtzComponent.getProperty(Property.TZID).getValue().equalsIgnoreCase(tzid)) {
                    return false;
                }
            }
            TimeZone timeZone = new EmitterTools(zoneInfo).getTimeZoneRegistry().getTimeZone(tzid);
            if (null != timeZone) {
                VTimeZone vTimeZone = timeZone.getVTimeZone();
                calendar.getComponents().add(0, vTimeZone);
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeSession(final ICalSession session, final OutputStream stream) throws ConversionError {
        final Calendar calendar = getCalendar(session);
        final CalendarOutputter outputter = new CalendarOutputter(false);
        try {
            outputter.output(calendar, stream);
        } catch (IOException e) {
            throw new ConversionError(-1, Code.WRITE_PROBLEM, e);
        } catch (ValidationException e) {
            throw new ConversionError(-1, Code.VALIDATION, e);
        }
    }

    @Override
    public void flush(final ICalSession session, final OutputStream stream) throws ConversionError {
        final Calendar calendar = getCalendar(session);
        final CalendarOutputter outputter = new CalendarOutputter(false);
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        try {
            outputter.output(calendar, temp);
            String icalPart = removeTimezoneData(new String(temp.toByteArray(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
            writer.write("\n");
            writer.write(icalPart);
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            throw new ConversionError(-1, Code.WRITE_PROBLEM, e);
        } catch (ValidationException e) {
            throw new ConversionError(-1, Code.VALIDATION, e);
        }

        if (!(session instanceof ICal4jSession)) {
        	throw new ConversionError(-1, Code.INVALID_SESSION, session.getClass().getName());
        }
        Calendar newCal = new Calendar();
        ((ICal4jSession) session).setCalendar(newCal);

    }

    private String removeTimezoneData(String string) {
		return Pattern
			.compile("\n?BEGIN:VTIMEZONE.+?\nEND:VTIMEZONE\n?",
				  Pattern.CASE_INSENSITIVE
				| Pattern.DOTALL
				| Pattern.MULTILINE)
			.matcher(string).replaceAll("");
	}

	@Override
    public ICalItem writeTask(final ICalSession session, final Task task,
        final Context context, final List<ConversionError> errors,
        final List<ConversionWarning> warnings) throws ConversionError {
        final Calendar calendar = getCalendar(session);
        final VToDo vToDo = createEvent(session.getMode(), getAndIncreaseIndex(session),task, context, warnings);
        calendar.getComponents().add(vToDo);
        return new ICal4jItem(vToDo);
    }

    protected void initCalendar(Calendar calendar, String method) {
        PropertyList properties = calendar.getProperties();
        ProdId prodId = new ProdId();
        prodId.setValue(VersionService.NAME);
        properties.add(prodId);
        properties.add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
        properties.add(CalScale.GREGORIAN);
        if (null != method) {
            replaceMethod(calendar, new Method(method));
        }
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

    protected void replaceMethod(Calendar calendar, Method newMethod) {
        if (newMethod != null) {

            Property oldMethod = calendar.getProperty("METHOD");
            while (oldMethod != null) {
                calendar.getProperties().remove(oldMethod);
                oldMethod = calendar.getProperty("METHOD");
            }
            calendar.getProperties().add(newMethod);
        }
    }
}
