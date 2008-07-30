package com.openexchange.data.conversion.ical;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.tasks.Task;

import java.util.TimeZone;
import java.util.List;
import java.util.Map;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface ICalParser {
    List<AppointmentObject> parseAppointments(String icalText, TimeZone defaultTZ, Context ctx);

    List<Task> parseTasks(String icalText, TimeZone defaultTZ, Context context);
}
