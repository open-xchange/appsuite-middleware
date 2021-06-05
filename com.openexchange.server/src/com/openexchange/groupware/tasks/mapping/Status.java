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
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Task;

/**
 * Methods for dealing with the status attribute of tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Status implements Mapper<Integer> {

    /**
     * Singleton
     */
    public static final Mapper<Integer> SINGLETON = new Status();

    /**
     * Prevent instantiation
     */
    private Status() {
        super();
    }

    @Override
    public int getId() {
        return Task.STATUS;
    }

    @Override
    public boolean isSet(final Task task) {
        return task.containsStatus();
    }

    @Override
    public String getDBColumnName() {
        return "state";
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void toDB(final PreparedStatement stmt, final int pos,
        final Task task) throws SQLException {
        stmt.setInt(pos, task.getStatus());
    }

    @Override
    public void fromDB(final ResultSet result, final int pos,
        final Task task) throws SQLException {
        final int status = result.getInt(pos);
        if (!result.wasNull()) {
            task.setStatus(status);
        }
    }

    @Override
    public boolean equals(final Task task1, final Task task2) {
        return task1.getStatus() == task2.getStatus();
    }

    @Override
    public Integer get(final Task task) {
        return Integer.valueOf(task.getStatus());
    }

    @Override
    public void set(final Task task, final Integer value) {
        task.setStatus(value.intValue());
    }
}
