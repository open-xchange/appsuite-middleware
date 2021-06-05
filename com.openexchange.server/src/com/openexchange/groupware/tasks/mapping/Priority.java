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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Task;


/**
 * Methods for iterated processing of the priority of task objects.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.1
 */
public class Priority implements Mapper<Integer> {

    public static final Priority SINGLETON = new Priority();

    private Priority() {
        super();
    }

    @Override
    public int getId() {
        return Task.PRIORITY;
    }

    @Override
    public boolean isSet(Task task) {
        return task.containsPriority();
    }

    @Override
    public String getDBColumnName() {
        return "priority";
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void toDB(PreparedStatement stmt, int pos, Task task) throws SQLException {
        if (null == task.getPriority()) {
            stmt.setNull(pos, Types.INTEGER);
        } else {
            stmt.setInt(pos, i(task.getPriority()));
        }
    }

    @Override
    public void fromDB(ResultSet result, int pos, Task task) throws SQLException {
        final int priority = result.getInt(pos);
        if (!result.wasNull()) {
            task.setPriority(I(priority));
        }
    }

    @Override
    public boolean equals(Task task1, Task task2) {
        Integer prio1 = task1.getPriority();
        Integer prio2 = task2.getPriority();

        if (null == prio1) {
            return null == prio2;
        }
        if (null == prio2) {
            return false;
        }
        return prio1.intValue() == prio2.intValue();
    }

    @Override
    public Integer get(Task task) {
        return task.getPriority();
    }

    @Override
    public void set(Task task, Integer value) {
        task.setPriority(value);
    }
}
