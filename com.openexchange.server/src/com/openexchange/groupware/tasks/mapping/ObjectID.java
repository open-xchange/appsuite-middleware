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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Task;

/**
 * Mapper implementation for the unique identifier of a task.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ObjectID implements Mapper<Integer> {

    public ObjectID() {
        super();
    }

    @Override
    public int getId() {
        return DataObject.OBJECT_ID;
    }

    @Override
    public String getDBColumnName() {
        return "id";
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSet(final Task task) {
        return task.containsObjectID();
    }

    @Override
    public void toDB(final PreparedStatement stmt, final int pos, final Task task) throws SQLException {
        stmt.setInt(pos, task.getObjectID());
    }

    @Override
    public void fromDB(final ResultSet result, final int pos, final Task task) throws SQLException {
        // NOT NULL constraint
        task.setObjectID(result.getInt(pos));
    }

    @Override
    public boolean equals(final Task task1, final Task task2) {
        return task1.getObjectID() == task2.getObjectID();
    }

    @Override
    public Integer get(final Task task) {
        return I(task.getObjectID());
    }

    @Override
    public void set(final Task task, final Integer value) {
        task.setObjectID(value.intValue());
    }
}
