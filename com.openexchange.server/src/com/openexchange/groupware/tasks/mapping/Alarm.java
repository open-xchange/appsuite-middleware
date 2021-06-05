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

package com.openexchange.groupware.tasks.mapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Mapping;
import com.openexchange.groupware.tasks.Task;

/**
 * Mapper implementation for the alarm.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Alarm implements Mapper<Date>{

    public Alarm() {
        super();
    }

    @Override
    public boolean equals(final Task task1, final Task task2) {
        return Mapping.equals(task1.getAlarm(), task2.getAlarm());
    }

    @Override
    public void fromDB(final ResultSet result, final int pos, final Task task) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date get(final Task task) {
        return task.getAlarm();
    }

    @Override
    public String getDBColumnName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getId() {
        return CalendarObject.ALARM;
    }

    @Override
    public boolean isSet(final Task task) {
        return task.containsAlarm();
    }

    @Override
    public void set(final Task task, final Date value) {
        task.setAlarm(value);
    }

    @Override
    public void toDB(final PreparedStatement stmt, final int pos, final Task task) {
        throw new UnsupportedOperationException();
    }
}
