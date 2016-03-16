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

import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.tasks.mapping.ActualCosts;
import com.openexchange.groupware.tasks.mapping.ActualDuration;
import com.openexchange.groupware.tasks.mapping.Filename;
import com.openexchange.groupware.tasks.mapping.ObjectID;
import com.openexchange.groupware.tasks.mapping.Priority;
import com.openexchange.groupware.tasks.mapping.RecurrenceCount;
import com.openexchange.groupware.tasks.mapping.Status;
import com.openexchange.groupware.tasks.mapping.TargetCosts;
import com.openexchange.groupware.tasks.mapping.TargetDuration;
import com.openexchange.groupware.tasks.mapping.UID;
import com.openexchange.sql.tools.SQLTools;

/**
 * This class contains the methods for mapping object attributes to database columns and vice versa.
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
     * Mapping array for all object attributes.
     */
    public static final Mapper<? extends Object>[] MAPPERS = new Mapper<?>[] {
        UID.SINGLETON,
        Filename.SINGLETON,
        new Mapper<Boolean>() {
            @Override
            public int getId() {
                return CommonObject.PRIVATE_FLAG;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsPrivateFlag();
            }
            @Override
            public String getDBColumnName() {
                return "private";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setBoolean(pos, task.getPrivateFlag());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                // NOT NULL constraint
                task.setPrivateFlag(result.getBoolean(pos));
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getPrivateFlag() == task2.getPrivateFlag();
            }
            @Override
            public Boolean get(final Task task) {
                return Boolean.valueOf(task.getPrivateFlag());
            }
            @Override
            public void set(final Task task, final Boolean value) {
                task.setPrivateFlag(value.booleanValue());
            }
        },
        new Mapper<Date>() {
            @Override
            public int getId() {
                return DataObject.CREATION_DATE;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsCreationDate();
            }
            @Override
            public String getDBColumnName() {
                return "creating_date";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setTimestamp(pos, SQLTools.toTimestamp(task.getCreationDate()));
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                // NOT NULL constraint
                task.setCreationDate(result.getTimestamp(pos));
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getCreationDate(),
                    task2.getCreationDate());
            }
            @Override
            public Date get(final Task task) {
                return task.getCreationDate();
            }
            @Override
            public void set(final Task task, final Date value) {
                task.setCreationDate(value);
            }
        },
        new Mapper<Date>() {
            @Override
            public int getId() {
                return DataObject.LAST_MODIFIED;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsLastModified();
            }
            @Override
            public String getDBColumnName() {
                return "last_modified";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setLong(pos, task.getLastModified().getTime());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                // NOT NULL constraint
                task.setLastModified(new Date(result.getLong(pos)));
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getLastModified(),
                    task2.getLastModified());
            }
            @Override
            public Date get(final Task task) {
                return task.getLastModified();
            }
            @Override
            public void set(final Task task, final Date value) {
                task.setLastModified(value);
            }
        },
        new Mapper<Integer>() {
            @Override
            public int getId() {
                return DataObject.CREATED_BY;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsCreatedBy();
            }
            @Override
            public String getDBColumnName() {
                return "created_from";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getCreatedBy());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                // NOT NULL constraint
                task.setCreatedBy(result.getInt(pos));
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getCreatedBy() == task2.getCreatedBy();
            }
            @Override
            public Integer get(final Task task) {
                return Integer.valueOf(task.getCreatedBy());
            }
            @Override
            public void set(final Task task, final Integer value) {
                task.setCreatedBy(value.intValue());
            }
        },
        new Mapper<Integer>() {
            @Override
            public int getId() {
                return DataObject.MODIFIED_BY;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsModifiedBy();
            }
            @Override
            public String getDBColumnName() {
                return "changed_from";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getModifiedBy());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int changedBy = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setModifiedBy(changedBy);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getModifiedBy() == task2.getModifiedBy();
            }
            @Override
            public Integer get(final Task task) {
                return Integer.valueOf(task.getModifiedBy());
            }
            @Override
            public void set(final Task task, final Integer value) {
                task.setModifiedBy(value.intValue());
            }
        },
        new Mapper<Date>() {
            @Override
            public int getId() {
                return CalendarObject.START_DATE;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsStartDate();
            }
            @Override
            public String getDBColumnName() {
                return "start";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getStartDate()) {
                    stmt.setNull(pos, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(pos, new Timestamp(task.getStartDate()
                        .getTime()));
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final Date start = result.getTimestamp(pos);
                if (!result.wasNull()) {
                    task.setStartDate(start);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getStartDate(),
                    task2.getStartDate());
            }
            @Override
            public Date get(final Task task) {
                return task.getStartDate();
            }
            @Override
            public void set(final Task task, final Date value) {
                task.setStartDate(value);
            }
        },
        new Mapper<Date>() {
            @Override
            public int getId() {
                return CalendarObject.END_DATE;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsEndDate();
            }
            @Override
            public String getDBColumnName() {
                return "end";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getEndDate()) {
                    stmt.setNull(pos, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(pos, new Timestamp(task.getEndDate()
                        .getTime()));
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final Date end = result.getTimestamp(pos);
                if (!result.wasNull()) {
                    task.setEndDate(end);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getEndDate(), task2.getEndDate());
            }
            @Override
            public Date get(final Task task) {
                return task.getEndDate();
            }
            @Override
            public void set(final Task task, final Date value) {
                task.setEndDate(value);
            }
        },
        new Mapper<Date>() {
            @Override
            public int getId() {
                return Task.DATE_COMPLETED;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsDateCompleted();
            }
            @Override
            public String getDBColumnName() {
                return "completed";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getDateCompleted()) {
                    stmt.setNull(pos, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(pos, new Timestamp(task.getDateCompleted()
                        .getTime()));
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final Date completed = result.getTimestamp(pos);
                if (!result.wasNull()) {
                    task.setDateCompleted(completed);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getDateCompleted(),
                    task2.getDateCompleted());
            }
            @Override
            public Date get(final Task task) {
                return task.getDateCompleted();
            }
            @Override
            public void set(final Task task, final Date value) {
                task.setDateCompleted(value);
            }
        },
        new Mapper<String>() {
            @Override
            public int getId() {
                return CalendarObject.TITLE;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsTitle();
            }
            @Override
            public String getDBColumnName() {
                return "title";
            }
            @Override
            public String getDisplayName() {
                return AttributeNames.TITLE;
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getTitle()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getTitle());
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String title = result.getString(pos);
                if (!result.wasNull()) {
                    task.setTitle(title);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getTitle(), task2.getTitle());
            }
            @Override
            public String get(final Task task) {
                return task.getTitle();
            }
            @Override
            public void set(final Task task, final String value) {
                task.setTitle(value);
            }
        },
        new Mapper<String>() {
            @Override
            public int getId() {
                return CalendarObject.NOTE;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsNote();
            }
            @Override
            public String getDBColumnName() {
                return "description";
            }
            @Override
            public String getDisplayName() {
                return AttributeNames.DESCRIPTION;
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getNote()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getNote());
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String description = result.getString(pos);
                if (!result.wasNull()) {
                    task.setNote(description);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getNote(), task2.getNote());
            }
            @Override
            public String get(final Task task) {
                return task.getNote();
            }
            @Override
            public void set(final Task task, final String value) {
                task.setNote(value);
            }
        },
        Status.SINGLETON,
        Priority.SINGLETON,
        new Mapper<Integer>() {
            @Override
            public int getId() {
                return Task.PERCENT_COMPLETED;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsPercentComplete();
            }
            @Override
            public String getDBColumnName() {
                return "progress";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getPercentComplete());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int progress = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setPercentComplete(progress);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getPercentComplete() == task2.getPercentComplete();
            }
            @Override
            public Integer get(final Task task) {
                return Integer.valueOf(task.getPercentComplete());
            }
            @Override
            public void set(final Task task, final Integer value) {
                task.setPercentComplete(value.intValue());
            }
        },
        new Mapper<String>() {
            @Override
            public int getId() {
                return CommonObject.CATEGORIES;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsCategories();
            }
            @Override
            public String getDBColumnName() {
                return "categories";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getCategories()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getCategories());
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String categories = result.getString(pos);
                if (!result.wasNull()) {
                    task.setCategories(categories);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getCategories(),
                    task2.getCategories());
            }
            @Override
            public String get(final Task task) {
                return task.getCategories();
            }
            @Override
            public void set(final Task task, final String value) {
                task.setCategories(value);
            }
        },
        new Mapper<Integer>() {
            @Override
            public int getId() {
                return Task.PROJECT_ID;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsProjectID();
            }
            @Override
            public String getDBColumnName() {
                return "project";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getProjectID());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int project = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setProjectID(project);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getProjectID() == task2.getProjectID();
            }
            @Override
            public Integer get(final Task task) {
                return Integer.valueOf(task.getProjectID());
            }
            @Override
            public void set(final Task task, final Integer value) {
                task.setProjectID(value.intValue());
            }
        },
        TargetDuration.SINGLETON,
        ActualDuration.SINGLETON,
        TargetCosts.SINGLETON,
        ActualCosts.SINGLETON,
        new Mapper<String>() {
            @Override
            public int getId() {
                return Task.CURRENCY;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsCurrency();
            }
            @Override
            public String getDBColumnName() {
                return "currency";
            }
            @Override
            public String getDisplayName() {
                return AttributeNames.CURRENCY;
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getCurrency()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getCurrency());
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String currency = result.getString(pos);
                if (!result.wasNull()) {
                    task.setCurrency(currency);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getCurrency(),
                    task2.getCurrency());
            }
            @Override
            public String get(final Task task) {
                return task.getCurrency();
            }
            @Override
            public void set(final Task task, final String value) {
                task.setCurrency(value);
            }
        },
        new Mapper<String>() {
            @Override
            public int getId() {
                return Task.TRIP_METER;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsTripMeter();
            }
            @Override
            public String getDBColumnName() {
                return "trip_meter";
            }
            @Override
            public String getDisplayName() {
                return AttributeNames.TRIP_METER;
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getTripMeter()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getTripMeter());
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String tripMeter = result.getString(pos);
                if (!result.wasNull()) {
                    task.setTripMeter(tripMeter);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getTripMeter(),
                    task2.getTripMeter());
            }
            @Override
            public String get(final Task task) {
                return task.getTripMeter();
            }
            @Override
            public void set(final Task task, final String value) {
                task.setTripMeter(value);
            }
        },
        new Mapper<String>() {
            @Override
            public int getId() {
                return Task.BILLING_INFORMATION;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsBillingInformation();
            }
            @Override
            public String getDBColumnName() {
                return "billing";
            }
            @Override
            public String getDisplayName() {
                return AttributeNames.BILLING_INFORMATION;
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getBillingInformation()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getBillingInformation());
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String billing = result.getString(pos);
                if (!result.wasNull()) {
                    task.setBillingInformation(billing);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getBillingInformation(),
                    task2.getBillingInformation());
            }
            @Override
            public String get(final Task task) {
                return task.getBillingInformation();
            }
            @Override
            public void set(final Task task, final String value) {
                task.setBillingInformation(value);
            }
        },
        new Mapper<String>() {
            @Override
            public int getId() {
                return Task.COMPANIES;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsCompanies();
            }
            @Override
            public String getDBColumnName() {
                return "companies";
            }
            @Override
            public String getDisplayName() {
                return AttributeNames.COMPANIES;
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getCompanies()) {
                    stmt.setNull(pos, Types.VARCHAR);
                } else {
                    stmt.setString(pos, task.getCompanies());
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final String companies = result.getString(pos);
                if (!result.wasNull()) {
                    task.setCompanies(companies);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getCompanies(),
                    task2.getCompanies());
            }
            @Override
            public String get(final Task task) {
                return task.getCompanies();
            }
            @Override
            public void set(final Task task, final String value) {
                task.setCompanies(value);
            }
        },
        new Mapper<Integer>() {
            @Override
            public int getId() {
                return CommonObject.COLOR_LABEL;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsLabel();
            }
            @Override
            public String getDBColumnName() {
                return "color_label";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getLabel());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int colorLabel = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setLabel(colorLabel);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getLabel() == task2.getLabel();
            }
            @Override
            public Integer get(final Task task) {
                return Integer.valueOf(task.getLabel());
            }
            @Override
            public void set(final Task task, final Integer value) {
                task.setLabel(value.intValue());
            }
        },
        new Mapper<Integer>() {
            @Override
            public int getId() {
                return CalendarObject.RECURRENCE_TYPE;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsRecurrenceType();
            }
            @Override
            public String getDBColumnName() {
                return "recurrence_type";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getRecurrenceType());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                // NOT NULL constraint
                task.setRecurrenceType(result.getInt(pos));
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getRecurrenceType() == task2.getRecurrenceType();
            }
            @Override
            public Integer get(final Task task) {
                return Integer.valueOf(task.getRecurrenceType());
            }
            @Override
            public void set(final Task task, final Integer value) {
                task.setRecurrenceType(value.intValue());
            }
        },
        new Mapper<Integer>() {
            @Override
            public int getId() {
                return CalendarObject.INTERVAL;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsInterval();
            }
            @Override
            public String getDBColumnName() {
                return "recurrence_interval";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getInterval());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int interval = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setInterval(interval);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getInterval() == task2.getInterval();
            }
            @Override
            public Integer get(final Task task) {
                return Integer.valueOf(task.getInterval());
            }
            @Override
            public void set(final Task task, final Integer value) {
                task.setInterval(value.intValue());
            }
        },
        new Mapper<Integer>() {
            @Override
            public int getId() {
                return CalendarObject.DAYS;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsDays();
            }
            @Override
            public String getDBColumnName() {
                return "recurrence_days";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (0 == task.getDays()) {
                    stmt.setNull(pos, Types.INTEGER);
                } else {
                    stmt.setInt(pos, task.getDays());
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int days = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setDays(days);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getDays() == task2.getDays();
            }
            @Override
            public Integer get(final Task task) {
                return Integer.valueOf(task.getDays());
            }
            @Override
            public void set(final Task task, final Integer value) {
                task.setDays(value.intValue());
            }
        },
        new Mapper<Integer>() {
            @Override
            public int getId() {
                return CalendarObject.DAY_IN_MONTH;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsDayInMonth();
            }
            @Override
            public String getDBColumnName() {
                return "recurrence_dayinmonth";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getDayInMonth());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int dayInMonth = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setDayInMonth(dayInMonth);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getDayInMonth() == task2.getDayInMonth();
            }
            @Override
            public Integer get(final Task task) {
                return Integer.valueOf(task.getDayInMonth());
            }
            @Override
            public void set(final Task task, final Integer value) {
                task.setDayInMonth(value.intValue());
            }
        },
        new Mapper<Integer>() {
            @Override
            public int getId() {
                return CalendarObject.MONTH;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsMonth();
            }
            @Override
            public String getDBColumnName() {
                return "recurrence_month";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getMonth());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int month = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setMonth(month);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getMonth() == task2.getMonth();
            }
            @Override
            public Integer get(final Task task) {
                return Integer.valueOf(task.getMonth());
            }
            @Override
            public void set(final Task task, final Integer value) {
                task.setMonth(value.intValue());
            }
        },
        new Mapper<Date>() {
            @Override
            public int getId() {
                return CalendarObject.UNTIL;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsUntil();
            }
            @Override
            public String getDBColumnName() {
                return "recurrence_until";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                if (null == task.getUntil()) {
                    stmt.setNull(pos, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(pos,
                        new Timestamp(task.getUntil().getTime()));
                }
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final Date until = result.getTimestamp(pos);
                if (!result.wasNull()) {
                    task.setUntil(until);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return Mapping.equals(task1.getUntil(), task2.getUntil());
            }
            @Override
            public Date get(final Task task) {
                return task.getUntil();
            }
            @Override
            public void set(final Task task, final Date value) {
                task.setUntil(value);
            }
        },
        RecurrenceCount.SINGLETON,
        new Mapper<Integer>() {
            @Override
            public int getId() {
                return CommonObject.NUMBER_OF_ATTACHMENTS;
            }
            @Override
            public boolean isSet(final Task task) {
                return task.containsNumberOfAttachments();
            }
            @Override
            public String getDBColumnName() {
                return "number_of_attachments";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(final PreparedStatement stmt, final int pos,
                final Task task) throws SQLException {
                stmt.setInt(pos, task.getNumberOfAttachments());
            }
            @Override
            public void fromDB(final ResultSet result, final int pos,
                final Task task) throws SQLException {
                final int numAttachments = result.getInt(pos);
                if (!result.wasNull()) {
                    task.setNumberOfAttachments(numAttachments);
                }
            }
            @Override
            public boolean equals(final Task task1, final Task task2) {
                return task1.getNumberOfAttachments() == task2
                    .getNumberOfAttachments();
            }
            @Override
            public Integer get(final Task task) {
                return Integer.valueOf(task.getNumberOfAttachments());
            }
            @Override
            public void set(final Task task, final Integer value) {
                task.setNumberOfAttachments(value.intValue());
            }
        },
        new Mapper<Boolean>() {
            @Override
            public int getId() {
                return CalendarObject.FULL_TIME;
            }
            @Override
            public boolean isSet(Task task) {
                return task.containsFullTime();
            }
            @Override
            public String getDBColumnName() {
                return "full_time";
            }
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void toDB(PreparedStatement stmt, int pos, Task task) throws SQLException {
                stmt.setBoolean(pos, task.getFullTime());
            }
            @Override
            public void fromDB(ResultSet result, int pos, Task task) throws SQLException {
                // NOT NULL constraint
                task.setFullTime(result.getBoolean(pos));
            }
            @Override
            public boolean equals(Task task1, Task task2) {
                return task1.getFullTime() == task2.getFullTime();
            }
            @Override
            public Boolean get(Task task) {
                return Boolean.valueOf(task.getFullTime());
            }
            @Override
            public void set(Task task, Boolean value) {
                task.setFullTime(value.booleanValue());
            }
        }
    };

    public static final Mapper<String>[] STRING_MAPPERS;

    /**
     * Implements equal with check if one of the objects is <code>null</code>.
     * @param obj1 first object.
     * @param obj2 second object.
     * @return <code>true</code> if both objects are <code>null</code> or
     * are equal.
     */
    public static boolean equals(final Object obj1, final Object obj2) {
        return (null == obj1) ? (null == obj2) : obj1.equals(obj2);
    }

    /**
     * This map does the mapping from the unique field identifier to the
     * according mapper.
     */
    private static final TIntObjectMap<Mapper<?>> ID_MAPPING;

    /**
     * This set contains all possible fields that can be used with tasks.
     */
    private static final TIntSet ALL_ATTRIBUTES;

    /**
     * @param attributeId identifier of the attribute.
     * @return the mapper implementation for the given attribute.
     */
    public static Mapper<?> getMapping(final int attributeId) {
        return ID_MAPPING.get(attributeId);
    }
    /**
     * returns all known field mappers
     */
    public static Collection<Mapper<?>> getAllFieldMappers() {
        return ID_MAPPING.valueCollection();
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
        final TIntObjectMap<Mapper<?>> tmp = new TIntObjectHashMap<Mapper<?>>();
        for (final Mapper<?> mapper : MAPPERS) {
            tmp.put(mapper.getId(), mapper);
        }
        final Mapper<Integer> identifier = new ObjectID();
        tmp.put(identifier.getId(), identifier);
        ID_MAPPING = TCollections.unmodifiableMap(tmp);
        final TIntSet tmp2 = new TIntHashSet();
        tmp2.addAll(ID_MAPPING.keySet());
        tmp2.add(CalendarObject.PARTICIPANTS);
        tmp2.add(FolderChildObject.FOLDER_ID);
        tmp2.add(CalendarObject.ALARM);
        ALL_ATTRIBUTES = TCollections.unmodifiableSet(tmp2);
        final List<Mapper<String>> tmp3 = new ArrayList<Mapper<String>>();
        for (final Mapper<? extends Object> mapper : Mapping.MAPPERS) {
            for (final Type t : mapper.getClass().getGenericInterfaces()) {
                if (t instanceof ParameterizedType) {
                    final ParameterizedType pt = ((ParameterizedType) t);
                    for (final Type u : pt.getActualTypeArguments()) {
                        if (String.class.equals(u)) {
                            tmp3.add((Mapper<String>) mapper);
                        }
                    }
                }
            }
        }
        STRING_MAPPERS = tmp3.toArray((Mapper<String>[]) new Mapper[tmp3.size()]);
    }
}
