/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
