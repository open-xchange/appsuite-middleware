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
 * Implementation to map task attribute targetCosts to database target_costs and back.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TargetCosts implements Mapper<BigDecimal> {

    public static final TargetCosts SINGLETON = new TargetCosts();

    protected TargetCosts() {
        super();
    }

    @Override
    public int getId() {
        return Task.TARGET_COSTS;
    }

    @Override
    public boolean isSet(Task task) {
        return task.containsTargetCosts();
    }

    @Override
    public String getDBColumnName() {
        return "target_costs";
    }

    @Override
    public String getDisplayName() {
        return AttributeNames.TARGET_COSTS;
    }

    @Override
    public void toDB(PreparedStatement stmt, int pos, Task task) throws SQLException {
        if (null == task.getTargetCosts()) {
            stmt.setNull(pos, Types.NUMERIC);
        } else {
            stmt.setBigDecimal(pos, task.getTargetCosts());
        }
    }

    @Override
    public void fromDB(ResultSet result, int pos, Task task) throws SQLException {
        BigDecimal targetCosts = result.getBigDecimal(pos);
        if (!result.wasNull()) {
            task.setTargetCosts(targetCosts);
        }
    }

    @Override
    public boolean equals(Task task1, Task task2) {
        if (task1.getTargetCosts() == null) {
            return (task2.getTargetCosts() == null);
        }
        if (task2.getTargetCosts() == null) {
            return (task1.getTargetCosts() == null);
        }
        return task1.getTargetCosts().equals(task2.getTargetCosts());
    }

    @Override
    public BigDecimal get(Task task) {
        return task.getTargetCosts();
    }

    @Override
    public void set(Task task, BigDecimal value) {
        task.setTargetCosts(value);
    }
}
