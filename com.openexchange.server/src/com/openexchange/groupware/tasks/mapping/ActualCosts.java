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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import com.openexchange.groupware.tasks.AttributeNames;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Task;

/**
 * Implementation to map task attribute actualCosts to database actual_costs and back.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ActualCosts implements Mapper<BigDecimal> {

    public static final ActualCosts SINGLETON = new ActualCosts();

    protected ActualCosts() {
        super();
    }

    @Override
    public int getId() {
        return Task.ACTUAL_COSTS;
    }

    @Override
    public boolean isSet(Task task) {
        return task.containsActualCosts();
    }

    @Override
    public String getDBColumnName() {
        return "actual_costs";
    }

    @Override
    public String getDisplayName() {
        return AttributeNames.ACTUAL_COSTS;
    }

    @Override
    public void toDB(PreparedStatement stmt, int pos, Task task) throws SQLException {
        if (null == task.getActualCosts()) {
            stmt.setNull(pos, Types.NUMERIC);
        } else {
            stmt.setBigDecimal(pos, task.getActualCosts());
        }
    }

    @Override
    public void fromDB(ResultSet result, int pos, Task task) throws SQLException {
        BigDecimal actualCosts = result.getBigDecimal(pos);
        if (!result.wasNull()) {
            task.setActualCosts(actualCosts);
        }
    }

    @Override
    public boolean equals(Task task1, Task task2) {
        if (task1.getActualCosts() == null) {
            return (task2.getActualCosts() == null);
        }

        if (task2.getActualCosts() == null) {
            return (task1.getActualCosts() == null);
        }
        return task1.getActualCosts().equals(task2.getActualCosts());
    }

    @Override
    public BigDecimal get(Task task) {
        return task.getActualCosts();
    }

    @Override
    public void set(Task task, BigDecimal value) {
        task.setActualCosts(value);
    }
}
