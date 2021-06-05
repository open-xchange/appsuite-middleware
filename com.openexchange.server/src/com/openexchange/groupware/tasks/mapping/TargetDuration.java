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

import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.l;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import com.openexchange.groupware.tasks.AttributeNames;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Task;

/**
 * Methods for iterated processing of the target duration of task objects.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class TargetDuration implements Mapper<Long> {

    /**
     * Singleton instance.
     */
    public static final TargetDuration SINGLETON = new TargetDuration();

    protected TargetDuration() {
        super();
    }

    @Override
    public int getId() {
        return Task.TARGET_DURATION;
    }

    @Override
    public boolean isSet(Task task) {
        return task.containsTargetDuration();
    }

    @Override
    public String getDBColumnName() {
        return "target_duration";
    }

    @Override
    public String getDisplayName() {
        return AttributeNames.TARGET_DURATION;
    }

    @Override
    public void toDB(PreparedStatement stmt, int pos, Task task) throws SQLException {
        if (null == task.getTargetDuration()) {
            stmt.setNull(pos, Types.BIGINT);
        } else {
            stmt.setLong(pos, l(task.getTargetDuration()));
        }
    }

    @Override
    public void fromDB(ResultSet result, int pos, Task task) throws SQLException {
        long targetDuration = result.getLong(pos);
        if (!result.wasNull()) {
            task.setTargetDuration(L(targetDuration));
        }
    }

    @Override
    public boolean equals(Task task1, Task task2) {
        if (task1.getTargetDuration() == null) {
            return (task2.getTargetDuration() == null);
        }
        if (task2.getTargetDuration() == null) {
            return (task1.getTargetDuration() == null);
        }
        return task1.getTargetDuration().equals(task2.getTargetDuration());
    }

    @Override
    public Long get(Task task) {
        return task.getTargetDuration();
    }

    @Override
    public void set(Task task, Long value) {
        task.setTargetDuration(value);
    }
}
