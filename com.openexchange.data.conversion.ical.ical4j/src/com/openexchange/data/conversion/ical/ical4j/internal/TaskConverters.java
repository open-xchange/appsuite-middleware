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

package com.openexchange.data.conversion.ical.ical4j.internal;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Alarm;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Attach;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Categories;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.CreatedAndDTStamp;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Duration;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Klass;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.LastModified;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Note;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Recurrence;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Start;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Title;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Uid;
import com.openexchange.data.conversion.ical.ical4j.internal.task.DateCompleted;
import com.openexchange.data.conversion.ical.ical4j.internal.task.DueDate;
import com.openexchange.data.conversion.ical.ical4j.internal.task.PercentComplete;
import com.openexchange.data.conversion.ical.ical4j.internal.task.Priority;
import com.openexchange.data.conversion.ical.ical4j.internal.task.State;
import com.openexchange.groupware.tasks.Task;
import net.fortuna.ical4j.model.component.VToDo;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class TaskConverters {

    public static final AttributeConverter<VToDo, Task>[] ALL;

    /**
     * Prevent instantiation.
     */
    private TaskConverters() {
        super();
    }

    static {
        final List<AttributeConverter<VToDo, Task>> tmp = new ArrayList<AttributeConverter<VToDo, Task>>();
        tmp.add(new Title<VToDo, Task>());
        tmp.add(new Note<VToDo, Task>());
        tmp.add(new Start<VToDo, Task>());
        tmp.add(new Duration<VToDo, Task>());
        tmp.add(new DueDate());
        tmp.add(new Klass<VToDo, Task>());
        tmp.add(new DateCompleted());
        tmp.add(new Participants<VToDo, Task>());
        tmp.add(new Categories<VToDo, Task>());
        tmp.add(new Recurrence<VToDo, Task>());
        tmp.add(new Alarm<VToDo, Task>());
        tmp.add(new State());
        tmp.add(new PercentComplete());
        tmp.add(new Priority());
        tmp.add(new Uid<VToDo, Task>());
        tmp.add(new CreatedAndDTStamp<VToDo, Task>());
        tmp.add(new LastModified<VToDo, Task>());
        tmp.add(new Attach<VToDo, Task>());
        ALL = tmp.toArray(new AttributeConverter[tmp.size()]);
    }
}
