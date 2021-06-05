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
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.tasks.AttributeNames;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Mapping;
import com.openexchange.groupware.tasks.Task;

/**
 * Methods for dealing with the uid attribute of tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class UID implements Mapper<String> {

    public static final Mapper<String> SINGLETON = new UID();

    private UID() {
        super();
    }

    @Override
    public int getId() {
        return CommonObject.UID;
    }

    @Override
    public boolean isSet(final Task task) {
        return task.containsUid();
    }

    @Override
    public String getDBColumnName() {
        return "uid";
    }

    @Override
    public String getDisplayName() {
        return AttributeNames.UID;
    }

    @Override
    public void toDB(final PreparedStatement stmt, final int pos, final Task task) throws SQLException {
        stmt.setString(pos, task.getUid());
    }

    @Override
    public void fromDB(final ResultSet result, final int pos, final Task task) throws SQLException {
        final String uid = result.getString(pos);
        if (!result.wasNull()) {
            task.setUid(uid);
        }
    }

    @Override
    public boolean equals(final Task task1, final Task task2) {
        return Mapping.equals(task1.getUid(), task2.getUid());
    }

    @Override
    public String get(final Task task) {
        return task.getUid();
    }

    @Override
    public void set(final Task task, final String value) {
        task.setUid(value);
    }
}
