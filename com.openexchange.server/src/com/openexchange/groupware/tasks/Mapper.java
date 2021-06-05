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

package com.openexchange.groupware.tasks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This interface will be used to map object attributes to database columns.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface Mapper<T extends Object> {

    /**
     * @return the unique identifier of the field.
     */
    int getId();

    /**
     * @return The name of the database column.
     */
    String getDBColumnName();

    /**
     * @return the name of the attribute that is shown in the UI.
     */
    String getDisplayName();

    /**
     * Checks if the attribute is defined in the task.
     * @param task Task object.
     * @return <code>true</code> if the attribute is defined.
     */
    boolean isSet(Task task);

    /**
     * Set the column in the PreparedStatement for inserting or updating the
     * task in the database.
     * @param stmt PreparedStatement.
     * @param pos Position of the column in the PreparedStatement.
     * @param task Task object.
     * @throws SQLException if an error occurs while setting the column.
     */
    void toDB(PreparedStatement stmt, int pos, Task task) throws SQLException;

    /**
     * Sets the attribute in the object if the column in the table is not
     * filled with SQL NULL.
     * @param result ResultSet.
     * @param pos Position of the column in the ResultSet.
     * @param task Task object.
     * @throws SQLException if an error occurs while reading the column.
     */
    void fromDB(ResultSet result, int pos, Task task) throws SQLException;

    /**
     * This method checks if the values of the attribute are equal in both
     * tasks. Beware that you have to check first if both tasks contain a
     * value for the attribute. This isn't done by this equals method.
     * @param task1 first task.
     * @param task2 second task.
     * @return <code>true</code> if the value of the attribute is equal in
     * both tasks and <code>false</code> otherwise.
     */
    boolean equals(Task task1, Task task2);

    /**
     * Reads the attribute from the task.
     * @param task task object to read the attribute from.
     * @return the attribute value.
     */
    T get(Task task);

    /**
     * Sets an attribute value in a task.
     * @param task task object to set the attribute-
     * @param value the value to set.
     */
    void set(Task task, T value);
}
