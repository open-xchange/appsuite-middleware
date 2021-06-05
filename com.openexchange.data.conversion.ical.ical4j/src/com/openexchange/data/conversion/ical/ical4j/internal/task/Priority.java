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

package com.openexchange.data.conversion.ical.ical4j.internal.task;

import static net.fortuna.ical4j.model.property.Priority.HIGH;
import static net.fortuna.ical4j.model.property.Priority.LOW;
import static net.fortuna.ical4j.model.property.Priority.MEDIUM;
import static net.fortuna.ical4j.model.property.Priority.UNDEFINED;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionWarning.Code;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import net.fortuna.ical4j.model.component.VToDo;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Priority extends AbstractVerifyingAttributeConverter<VToDo, Task> {

    public Priority() {
        super();
    }

    @Override
    public boolean isSet(final Task task) {
        return task.containsPriority();
    }

    @Override
    public void emit(final Mode mode, final int index, final Task task, final VToDo vToDo, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        final net.fortuna.ical4j.model.property.Priority prio;
        switch (task.getPriority().intValue()) {
        case Task.HIGH:
            prio = HIGH;
            break;
        case Task.NORMAL:
            prio = MEDIUM;
            break;
        case Task.LOW:
            prio = LOW;
            break;
        default:
                warnings.add(new ConversionWarning(index, Code.INVALID_PRIORITY, task.getPriority()));
            prio = UNDEFINED;
        }
        vToDo.getProperties().add(prio);
    }

    @Override
    public boolean hasProperty(final VToDo vToDo) {
        return vToDo.getPriority() != null;
    }

    @Override
    public void parse(final int index, final VToDo todo, final Task task, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        final float lowMed = (LOW.getLevel() + MEDIUM.getLevel()) >> 1;
        final float medHigh = (MEDIUM.getLevel() + HIGH.getLevel()) >> 1;
        final int priority = todo.getPriority().getLevel();
        if (priority >= lowMed) {
            task.setPriority(Integer.valueOf(Task.LOW));
        } else if (priority >= medHigh) {
            task.setPriority(Integer.valueOf(Task.NORMAL));
        } else if (priority >= HIGH.getLevel()) {
            task.setPriority(Integer.valueOf(Task.HIGH));
        } else if (priority == UNDEFINED.getLevel()) {
            task.setPriority(Integer.valueOf(Task.NORMAL)); // Default to normal Bug #10401
        } else {
            warnings.add(new ConversionWarning(index, Code.INVALID_PRIORITY, Integer.valueOf(priority)));
        }
    }
}
