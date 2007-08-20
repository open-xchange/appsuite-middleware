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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.openexchange.groupware.tasks.mapping.Status;

/**
 * This class contains the methods for mapping object attributes to database+
 * columns and vice versa.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Mapping {

    /**
     * Prevent instantiation
     */
    private Mapping() {
        super();
    }

    /**
     * This interface will be used to map object attributes to database columns.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public interface Mapper<T> {

        /**
         * @return the unique identifier of the field.
         */
        int getId();

        /**
         * @return The name of the database column.
         */
        String getDBColumnName();

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
        void toDB(PreparedStatement stmt, int pos, Task task)
            throws SQLException;

        /**
         * Sets the attribute in the object if the column in the table is not
         * filled with SQL NULL.
         * @param result ResultSet.
         * @param pos Position of the column in the ResultSet.
         * @param task Task object.
         * @throws SQLException if an error occurs while reading the column.
         */
        void fromDB(ResultSet result, int pos, Task task)
            throws SQLException;

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

    /**
     * Mapping array for all object attributes.
     */
    static final Mapper[] MAPPERS = new Mapper[] {
        new Mapper<Boolean>() {
            public int getId() {
                return Task.PRIVATE_FLAG;
            }
            public boolean isSet(final Task task) {
                return task.containsPrivateFlag();
            }
            public String getDBColumnName() {
                return "private";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setBoolean(pos, get(task));
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                // NOT NULL constraint
                task.setPrivateFlag(result.getBoolean(pos));
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getPrivateFlag() == task2.getPrivateFlag();
            }
            public Boolean get(final Task task) {
                return task.getPrivateFlag();
            }
            public void set(final Task task, final Boolean value) {
                task.setPrivateFlag(value);
            }
        },
        new Mapper<Date>() {
            public int getId() {
                return Task.CREATION_DATE;
            }
            public boolean isSet(final Task task) {
                return task.containsCreationDate();
            }
            public String getDBColumnName() {
                return "creating_date";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setTimestamp(pos, new Timestamp(task.getCreationDate()
                    .getTime()));
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                // NOT NULL constraint
                task.setCreationDate(result.getTimestamp(pos));
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getCreationDate(),
                    task2.getCreationDate());
            }
            public Date get(final Task task) {
                return task.getCreationDate();
            }
            public void set(final Task task, final Date value) {
                task.setCreationDate(value);
            }
        },
        new Mapper<Date>() {
            public int getId() {
                return Task.LAST_MODIFIED;
            }
            public boolean isSet(final Task task) {
                return task.containsLastModified();
            }
            public String getDBColumnName() {
                return "last_modified";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setLong(pos, task.getLastModified().getTime());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                // NOT NULL constraint
                task.setLastModified(new Date(result.getLong(pos)));
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getLastModified(),
                    task2.getLastModified());
            }
            public Date get(final Task task) {
                return task.getLastModified();
            }
            public void set(final Task task, final Date value) {
                task.setLastModified(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.CREATED_BY;
            }
            public boolean isSet(final Task task) {
                return task.containsCreatedBy();
            }
            public String getDBColumnName() {
                return "created_from";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getCreatedBy());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                // NOT NULL constraint
                task.setCreatedBy(result.getInt(pos));
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getCreatedBy() == task2.getCreatedBy();
            }
            public Integer get(final Task task) {
                return task.getCreatedBy();
            }
            public void set(final Task task, final Integer value) {
                task.setCreatedBy(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.MODIFIED_BY;
            }
            public boolean isSet(final Task task) {
                return task.containsModifiedBy();
            }
            public String getDBColumnName() {
                return "changed_from";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getModifiedBy());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int changedBy = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setModifiedBy(changedBy);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getModifiedBy() == task2.getModifiedBy();
            }
            public Integer get(final Task task) {
                return task.getModifiedBy();
            }
            public void set(final Task task, final Integer value) {
                task.setModifiedBy(value);
            }
        },
        new Mapper<Date>() {
            public int getId() {
                return Task.START_DATE;
            }
            public boolean isSet(final Task task) {
                return task.containsStartDate();
            }
            public String getDBColumnName() {
                return "start";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getStartDate()) {
                    stmt.setNull(pos, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(pos, new Timestamp(task.getStartDate()
                        .getTime()));
                }
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final Date start = result.getTimestamp(pos);
                if (!result.wasNull()) {
                    task.setStartDate(start);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getStartDate(),
                    task2.getStartDate());
            }
            public Date get(final Task task) {
                return task.getStartDate();
            }
            public void set(final Task task, final Date value) {
                task.setStartDate(value);
            }
        },
        new Mapper<Date>() {
            public int getId() {
                return Task.END_DATE;
            }
            public boolean isSet(final Task task) {
                return task.containsEndDate();
            }
            public String getDBColumnName() {
                return "end";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getEndDate()) {
                    stmt.setNull(pos, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(pos, new Timestamp(task.getEndDate()
                        .getTime()));
                }
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final Date end = result.getTimestamp(pos);
                if (!result.wasNull()) {
                    task.setEndDate(end);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getEndDate(), task2.getEndDate());
            }
            public Date get(final Task task) {
                return task.getEndDate();
            }
            public void set(final Task task, final Date value) {
                task.setEndDate(value);
            }
        },
        new Mapper<Date>() {
            public int getId() {
                return Task.DATE_COMPLETED;
            }
            public boolean isSet(final Task task) {
                return task.containsDateCompleted();
            }
            public String getDBColumnName() {
                return "completed";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getDateCompleted()) {
                    stmt.setNull(pos, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(pos, new Timestamp(task.getDateCompleted()
                        .getTime()));
                }
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final Date completed = result.getTimestamp(pos);
                if (!result.wasNull()) {
                    task.setDateCompleted(completed);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getDateCompleted(),
                    task2.getDateCompleted());
            }
            public Date get(final Task task) {
                return task.getDateCompleted();
            }
            public void set(final Task task, final Date value) {
                task.setDateCompleted(value);
            }
        },
        new Mapper<String>() {
            public int getId() {
                return Task.TITLE;
            }
            public boolean isSet(final Task task) {
                return task.containsTitle();
            }
            public String getDBColumnName() {
                return "title";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getTitle()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getTitle());
                }
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String title = result.getString(pos);
                if (!result.wasNull()) {
                    task.setTitle(title);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getTitle(), task2.getTitle());
            }
            public String get(final Task task) {
                return task.getTitle();
            }
            public void set(final Task task, final String value) {
                task.setTitle(value);
            }
        },
        new Mapper<String>() {
            public int getId() {
                return Task.NOTE;
            }
            public boolean isSet(final Task task) {
                return task.containsNote();
            }
            public String getDBColumnName() {
                return "description";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getNote()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getNote());
                }
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String description = result.getString(pos);
                if (!result.wasNull()) {
                    task.setNote(description);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getNote(), task2.getNote());
            }
            public String get(final Task task) {
                return task.getNote();
            }
            public void set(final Task task, final String value) {
                task.setNote(value);
            }
        },
        Status.SINGLETON,
        new Mapper<Integer>() {
            public int getId() {
                return Task.PRIORITY;
            }
            public boolean isSet(final Task task) {
                return task.containsPriority();
            }
            public String getDBColumnName() {
                return "priority";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getPriority());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int priority = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setPriority(priority);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getPriority() == task2.getPriority();
            }
            public Integer get(final Task task) {
                return task.getPriority();
            }
            public void set(final Task task, final Integer value) {
                task.setPriority(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.PERCENT_COMPLETED;
            }
            public boolean isSet(final Task task) {
                return task.containsPercentComplete();
            }
            public String getDBColumnName() {
                return "progress";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getPercentComplete());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int progress = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setPercentComplete(progress);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getPercentComplete() == task2.getPercentComplete();
            }
            public Integer get(final Task task) {
                return task.getPercentComplete();
            }
            public void set(final Task task, final Integer value) {
                task.setPercentComplete(value);
            }
        },
        new Mapper<String>() {
            public int getId() {
                return Task.CATEGORIES;
            }
            public boolean isSet(final Task task) {
                return task.containsCategories();
            }
            public String getDBColumnName() {
                return "categories";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getCategories()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getCategories());
                }
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String categories = result.getString(pos);
                if (!result.wasNull()) {
                    task.setCategories(categories);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getCategories(),
                    task2.getCategories());
            }
            public String get(final Task task) {
                return task.getCategories();
            }
            public void set(final Task task, final String value) {
                task.setCategories(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.PROJECT_ID;
            }
            public boolean isSet(final Task task) {
                return task.containsProjectID();
            }
            public String getDBColumnName() {
                return "project";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getProjectID());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int project = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setProjectID(project);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getProjectID() == task2.getProjectID();
            }
            public Integer get(final Task task) {
                return task.getProjectID();
            }
            public void set(final Task task, final Integer value) {
                task.setProjectID(value);
            }
        },
        new Mapper<Long>() {
            public int getId() {
                return Task.TARGET_DURATION;
            }
            public boolean isSet(final Task task) {
                return task.containsTargetDuration();
            }
            public String getDBColumnName() {
                return "target_duration";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setLong(pos, task.getTargetDuration());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final long targetDuration = result.getLong(pos);
                if (!result.wasNull()) {
                    task.setTargetDuration(targetDuration);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getTargetDuration() == task2.getTargetDuration();
            }
            public Long get(final Task task) {
                return task.getTargetDuration();
            }
            public void set(final Task task, final Long value) {
                task.setTargetDuration(value);
            }
        },
        new Mapper<Long>() {
            public int getId() {
                return Task.ACTUAL_DURATION;
            }
            public boolean isSet(final Task task) {
                return task.containsActualDuration();
            }
            public String getDBColumnName() {
                return "actual_duration";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setLong(pos, task.getActualDuration());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final long actualDuration = result.getLong(pos);
                if (!result.wasNull()) {
                    task.setActualDuration(actualDuration);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getActualDuration() == task2.getActualDuration();
            }
            public Long get(final Task task) {
                return task.getActualDuration();
            }
            public void set(final Task task, final Long value) {
                task.setActualDuration(value);
            }
        },
        new Mapper<Float>() {
            public int getId() {
                return Task.TARGET_COSTS;
            }
            public boolean isSet(final Task task) {
                return task.containsTargetCosts();
            }
            public String getDBColumnName() {
                return "target_costs";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setDouble(pos, task.getTargetCosts());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final float targetCosts = result.getFloat(pos);
                if (!result.wasNull()) {
                    task.setTargetCosts(targetCosts);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getTargetCosts() == task2.getTargetCosts();
            }
            public Float get(final Task task) {
                return task.getTargetCosts();
            }
            public void set(final Task task, final Float value) {
                task.setTargetCosts(value);
            }
        },
        new Mapper<Float>() {
            public int getId() {
                return Task.ACTUAL_COSTS;
            }
            public boolean isSet(final Task task) {
                return task.containsActualCosts();
            }
            public String getDBColumnName() {
                return "actual_costs";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setDouble(pos, task.getActualCosts());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final float actualCosts = result.getFloat(pos);
                if (!result.wasNull()) {
                    task.setActualCosts(actualCosts);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getActualCosts() == task2.getActualCosts();
            }
            public Float get(final Task task) {
                return task.getActualCosts();
            }
            public void set(final Task task, final Float value) {
                task.setActualCosts(value);
            }
        },
        new Mapper<String>() {
            public int getId() {
                return Task.CURRENCY;
            }
            public boolean isSet(final Task task) {
                return task.containsCurrency();
            }
            public String getDBColumnName() {
                return "currency";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getCurrency()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getCurrency());
                }
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String currency = result.getString(pos);
                if (!result.wasNull()) {
                    task.setCurrency(currency);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getCurrency(),
                    task2.getCurrency());
            }
            public String get(final Task task) {
                return task.getCurrency();
            }
            public void set(final Task task, final String value) {
                task.setCurrency(value);
            }
        },
        new Mapper<String>() {
            public int getId() {
                return Task.TRIP_METER;
            }
            public boolean isSet(final Task task) {
                return task.containsTripMeter();
            }
            public String getDBColumnName() {
                return "trip_meter";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getTripMeter()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getTripMeter());
                }
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String tripMeter = result.getString(pos);
                if (!result.wasNull()) {
                    task.setTripMeter(tripMeter);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getTripMeter(),
                    task2.getTripMeter());
            }
            public String get(final Task task) {
                return task.getTripMeter();
            }
            public void set(final Task task, final String value) {
                task.setTripMeter(value);
            }
        },
        new Mapper<String>() {
            public int getId() {
                return Task.BILLING_INFORMATION;
            }
            public boolean isSet(final Task task) {
                return task.containsBillingInformation();
            }
            public String getDBColumnName() {
                return "billing";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getBillingInformation()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getBillingInformation());
                }
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String billing = result.getString(pos);
                if (!result.wasNull()) {
                    task.setBillingInformation(billing);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getBillingInformation(),
                    task2.getBillingInformation());
            }
            public String get(final Task task) {
                return task.getBillingInformation();
            }
            public void set(final Task task, final String value) {
                task.setBillingInformation(value);
            }
        },
        new Mapper<String>() {
            public int getId() {
                return Task.COMPANIES;
            }
            public boolean isSet(final Task task) {
                return task.containsCompanies();
            }
            public String getDBColumnName() {
                return "companies";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getCompanies()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getCompanies());
                }
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String companies = result.getString(pos);
                if (!result.wasNull()) {
                    task.setCompanies(companies);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getCompanies(),
                    task2.getCompanies());
            }
            public String get(final Task task) {
                return task.getCompanies();
            }
            public void set(final Task task, final String value) {
                task.setCompanies(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.COLOR_LABEL;
            }
            public boolean isSet(final Task task) {
                return task.containsLabel();
            }
            public String getDBColumnName() {
                return "color_label";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getLabel());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int colorLabel = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setLabel(colorLabel);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getLabel() == task2.getLabel();
            }
            public Integer get(final Task task) {
                return task.getLabel();
            }
            public void set(final Task task, final Integer value) {
                task.setLabel(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.RECURRENCE_TYPE;
            }
            public boolean isSet(final Task task) {
                return task.containsRecurrenceType();
            }
            public String getDBColumnName() {
                return "recurrence_type";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getRecurrenceType());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                // NOT NULL constraint
                task.setRecurrenceType(result.getInt(pos));
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getRecurrenceType() == task2.getRecurrenceType();
            }
            public Integer get(final Task task) {
                return task.getRecurrenceType();
            }
            public void set(final Task task, final Integer value) {
                task.setRecurrenceType(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.INTERVAL;
            }
            public boolean isSet(final Task task) {
                return task.containsInterval();
            }
            public String getDBColumnName() {
                return "recurrence_interval";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getInterval());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int interval = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setInterval(interval);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getInterval() == task2.getInterval();
            }
            public Integer get(final Task task) {
                return task.getInterval();
            }
            public void set(final Task task, final Integer value) {
                task.setInterval(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.DAYS;
            }
            public boolean isSet(final Task task) {
                return task.containsDays();
            }
            public String getDBColumnName() {
                return "recurrence_days";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getDays());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int days = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setDays(days);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getDays() == task2.getDays();
            }
            public Integer get(final Task task) {
                return task.getDays();
            }
            public void set(final Task task, final Integer value) {
                task.setDays(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.DAY_IN_MONTH;
            }
            public boolean isSet(final Task task) {
                return task.containsDayInMonth();
            }
            public String getDBColumnName() {
                return "recurrence_dayinmonth";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getDayInMonth());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int dayInMonth = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setDayInMonth(dayInMonth);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getDayInMonth() == task2.getDayInMonth();
            }
            public Integer get(final Task task) {
                return task.getDayInMonth();
            }
            public void set(final Task task, final Integer value) {
                task.setDayInMonth(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.MONTH;
            }
            public boolean isSet(final Task task) {
                return task.containsMonth();
            }
            public String getDBColumnName() {
                return "recurrence_month";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getMonth());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int month = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setMonth(month);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getMonth() == task2.getMonth();
            }
            public Integer get(final Task task) {
                return task.getMonth();
            }
            public void set(final Task task, final Integer value) {
                task.setMonth(value);
            }
        },
        new Mapper<Date>() {
            public int getId() {
                return Task.UNTIL;
            }
            public boolean isSet(final Task task) {
                return task.containsUntil();
            }
            public String getDBColumnName() {
                return "recurrence_until";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getUntil()) {
                    stmt.setNull(pos, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(pos,
                        new Timestamp(task.getUntil().getTime()));
                }
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final Date until = result.getTimestamp(pos);
                if (!result.wasNull()) {
                    task.setUntil(until);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getUntil(), task2.getUntil());
            }
            public Date get(final Task task) {
                return task.getUntil();
            }
            public void set(final Task task, final Date value) {
                task.setUntil(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.RECURRING_OCCURRENCE;
            }
            public boolean isSet(final Task task) {
                return task.containsOccurrence();
            }
            public String getDBColumnName() {
                return "recurrence_count"; // TODO rename this
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getOccurrence());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int occurence = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setOccurrence(occurence);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getOccurrence() == task2.getOccurrence();
            }
            public Integer get(final Task task) {
                return task.getOccurrence();
            }
            public void set(final Task task, final Integer value) {
                task.setOccurrence(value);
            }
        },
        new Mapper<Integer>() {
            public int getId() {
                return Task.NUMBER_OF_ATTACHMENTS;
            }
            public boolean isSet(final Task task) {
                return task.containsNumberOfAttachments();
            }
            public String getDBColumnName() {
                return "number_of_attachments";
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getNumberOfAttachments());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int numAttachments = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setNumberOfAttachments(numAttachments);
                }
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getNumberOfAttachments() == task2
                    .getNumberOfAttachments();
            }
            public Integer get(final Task task) {
                return task.getNumberOfAttachments();
            }
            public void set(final Task task, final Integer value) {
                task.setNumberOfAttachments(value);
            }
        }
    };

    /**
     * Implements equal with check if one of the objects is <code>null</code>.
     * @param obj1 first object.
     * @param obj2 second object.
     * @return <code>true</code> if both objects are <code>null</code> or
     * are equal.
     */
    private static boolean equals(final Object obj1, final Object obj2) {
        return (null == obj1) ? (null == obj2) : obj1.equals(obj2);
    }

    /**
     * This map does the mapping from the unique field identifier to the
     * according mapper.
     */
    private static final Map<Integer, Mapper> ID_MAPPING;

    /**
     * This set contains all possible fields that can be used with tasks.
     */
    private static final Set<Integer> ALL_ATTRIBUTES;

    /**
     * @param attributeId identifier of the attribute.
     * @return the mapper implementation for the given attribute.
     */
    public static Mapper getMapping(final int attributeId) {
        return ID_MAPPING.get(attributeId);
    }

    /**
     * Checks if the requested attributes are implemented for tasks.
     * @param attributes requested attributes.
     * @return <code>true</code> if the attributes are implemented, otherwise
     * <code>false</code>.
     */
    static boolean implemented(final int[] attributes) {
        boolean retval = true;
        for (int i = 0; i < attributes.length && retval; i++) {
            retval = ALL_ATTRIBUTES.contains(attributes[i]);
        }
        return retval;
    }

    static {
        Map<Integer, Mapper> tmp = new HashMap<Integer, Mapper>();
        for (Mapper mapper : MAPPERS) {
            tmp.put(mapper.getId(), mapper);
        }
        Mapper identifier = new Mapper<Integer>() {
            public int getId() {
                return Task.OBJECT_ID;
            }
            public String getDBColumnName() {
                return "id";
            }
            public boolean isSet(final Task task) {
                return task.containsObjectID();
            }
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getObjectID());
            }
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                // NOT NULL constraint
                task.setObjectID(result.getInt(pos));
            }
            public boolean equals(final Task task1, final Task task2) {
                return task1.getObjectID() == task2.getObjectID();
            }
            public Integer get(final Task task) {
                return task.getObjectID();
            }
            public void set(final Task task, final Integer value) {
                task.setObjectID(value);
            }
        };
        tmp.put(identifier.getId(), identifier);
        ID_MAPPING = Collections.unmodifiableMap(tmp);
        Set<Integer> tmp2 = new HashSet<Integer>();
        tmp2.addAll(ID_MAPPING.keySet());
        tmp2.add(Task.PARTICIPANTS);
        tmp2.add(Task.FOLDER_ID);
        tmp2.add(Task.ALARM);
        ALL_ATTRIBUTES = Collections.unmodifiableSet(tmp2);
    }
}
