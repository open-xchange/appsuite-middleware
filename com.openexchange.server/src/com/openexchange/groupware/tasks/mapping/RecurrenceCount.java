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
import java.sql.SQLException;
import java.sql.Types;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Task;

/**
 * Methods for dealing with the recurrence count of tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RecurrenceCount implements Mapper<Integer> {

    /**
     * Singleton instance.
     */
    public static final Mapper<Integer> SINGLETON = new RecurrenceCount();

    /**
     * Prevent instantiation.
     */
    protected RecurrenceCount() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return CalendarObject.RECURRENCE_COUNT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSet(Task task) {
        return task.containsOccurrence();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDBColumnName() {
        return "recurrence_count"; // TODO rename this
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toDB(PreparedStatement stmt, int pos, Task task) throws SQLException {
        if (0 == task.getOccurrence()) {
            stmt.setNull(pos, Types.INTEGER);
        } else {
            stmt.setInt(pos, task.getOccurrence());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fromDB(ResultSet result, int pos, Task task) throws SQLException {
        int occurence = result.getInt(pos);
        if (!result.wasNull()) {
            task.setOccurrence(occurence);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Task task1, Task task2) {
        return task1.getOccurrence() == task2.getOccurrence();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer get(Task task) {
        return Integer.valueOf(task.getOccurrence());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(Task task, Integer value) {
        task.setOccurrence(value.intValue());
    }
}
