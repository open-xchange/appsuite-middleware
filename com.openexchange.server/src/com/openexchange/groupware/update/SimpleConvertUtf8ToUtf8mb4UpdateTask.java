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

package com.openexchange.groupware.update;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.google.common.collect.ImmutableList;

/**
 * {@link SimpleConvertUtf8ToUtf8mb4UpdateTask}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SimpleConvertUtf8ToUtf8mb4UpdateTask extends AbstractConvertUtf8ToUtf8mb4Task {

    private final List<String> tableNames;
    private final String[] dependencies;

    /**
     * Initializes a new {@link SimpleConvertUtf8ToUtf8mb4UpdateTask}.
     *
     * @param tableNames A {@link List} with table names to convert
     * @param dependencies An optional array of dependency update tasks
     * @param throws {@link IllegalArgumentException} if the tableNames
     *            {@link List} is <code>null</code>.
     */
    public SimpleConvertUtf8ToUtf8mb4UpdateTask(List<String> tableNames, String... dependencies) {
        super();
        if (tableNames == null) {
            throw new IllegalArgumentException("The table names must not be null");
        }
        this.tableNames = tableNames instanceof ImmutableList ? tableNames : ImmutableList.copyOf(tableNames);
        this.dependencies = dependencies == null ? NO_DEPENDENCIES : dependencies;
    }

    /**
     * Initializes a new {@link SimpleConvertUtf8ToUtf8mb4UpdateTask}.
     *
     * @param dependentUpdateTask The update task, which this conversion task depends on
     * @param tables The tables to consider
     */
    public SimpleConvertUtf8ToUtf8mb4UpdateTask(Class<? extends UpdateTaskV2> dependentUpdateTaskClass, String... tables) {
        super();
        if (null == tables) {
            throw new IllegalArgumentException("The table names must not be null");
        }
        this.tableNames = ImmutableList.copyOf(tables);
        this.dependencies = new String[] { dependentUpdateTaskClass.getName() };
    }

    @Override
    public String[] getDependencies() {
        return dependencies;
    }

    @Override
    protected List<String> tablesToConvert() {
        return tableNames;
    }

    @Override
    protected void before(PerformParameters params, Connection connection) throws SQLException {
        // no-op, override to implement
    }

    @Override
    protected void after(PerformParameters params, Connection connection) throws SQLException {
        // no-op, override to implement
    }

}
