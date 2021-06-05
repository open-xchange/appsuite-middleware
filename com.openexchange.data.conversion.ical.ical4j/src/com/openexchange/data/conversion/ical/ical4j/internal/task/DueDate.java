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

import static com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools.toDate;
import static com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools.toDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Due;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class DueDate extends AbstractVerifyingAttributeConverter<VToDo, Task> {

    public DueDate() {
        super();
    }

    @Override
    public boolean isSet(final Task task) {
        return task.containsEndDate();
    }

    @Override
    public void emit(final Mode mode, final int index, final Task task, final VToDo vToDo, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        /*
         * emit as date or date-time depending on fulltime flag
         */
        Due due = new Due();
        if (task.getFullTime()) {
            due.setDate(toDate(task.getEndDate()));
        } else {
            due.setDate(toDateTime(mode.getZoneInfo(), task.getEndDate(), EmitterTools.extractTimezoneIfPossible(task)));
        }
        vToDo.getProperties().add(due);
    }

    @Override
    public boolean hasProperty(final VToDo vToDo) {
        return null != vToDo.getDue();
    }

    @Override
    public void parse(final int index, final VToDo vToDo, final Task task, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        if (task.containsEndDate()) {
            return;
        }
        final Due due = vToDo.getDue();
        if (null == due) {
            return;
        }
        final Date endDate = ParserTools.toDateConsideringDateType(due, timeZone);
        task.setEndDate(endDate);
        task.setFullTime(false == ParserTools.isDateTime(vToDo, due));
    }

}
