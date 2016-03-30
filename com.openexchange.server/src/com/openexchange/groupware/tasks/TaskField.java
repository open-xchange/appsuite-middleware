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

/**
 * This class lists different names used for fields of a Task. TODO: Remove this enumeration and use the {@link Mapper}.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public enum TaskField {
    OBJECTID(Task.OBJECT_ID, "ObjectID"),
    CREATEDBY(Task.CREATED_BY, "CreatedBy"),
    CREATIONDATE(Task.CREATION_DATE, "CreationDate"),
    MODIFIEDBY(Task.MODIFIED_BY, "ModifiedBy"),
    LASTMODIFIED(Task.LAST_MODIFIED, "LastModified"),
    PARENTFOLDERID(Task.FOLDER_ID, "ParentFolderID"),
    TITLE(Task.TITLE, "Title"),
    STARTDATE(Task.START_DATE, "StartDate"),
    ENDDATE(Task.END_DATE, "EndDate"),
    NOTE(Task.NOTE, "Note"),
    ACTUALCOSTS(Task.ACTUAL_COSTS, "ActualCosts"),
    ACTUALDURATION(Task.ACTUAL_DURATION, "ActualDuration"),
    BILLINGINFORMATION(Task.BILLING_INFORMATION, "BillingInformation"),
    CATEGORIES(Task.CATEGORIES, "Categories"),
    COMPANIES(Task.COMPANIES, "Companies"),
    CURRENCY(Task.CURRENCY, "Currency"),
    DATECOMPLETED(Task.DATE_COMPLETED, "DateCompleted"),
    PERCENTCOMPLETE(Task.PERCENT_COMPLETED, "PercentComplete"),
    PRIORITY(Task.PRIORITY, "Priority"),
    STATUS(Task.STATUS, "Status"),
    TARGETCOSTS(Task.TARGET_COSTS, "TargetCosts"),
    TARGETDURATION(Task.TARGET_DURATION, "TargetDuration"),
    TRIPMETER(Task.TRIP_METER, "TripMeter"),
    RECURRENCETYPE(Task.RECURRENCE_TYPE, "RecurrenceType"),
    LABEL(Task.COLOR_LABEL, "Label"),
    UID(Task.UID, "UID");

    private int taskID; // ID used in Task.*

    private String name; // name used for getters & setters

    private TaskField(final int taskID, final String name) {
        this.taskID = taskID;
        this.name = name;
    }

    public static TaskField getByTaskID(final int id) {
        for (final TaskField field : values()) {
            if (field.getTaskID() == id) {
                return field;
            }
        }
        return null;
    }

    public static TaskField getByName(final String name) {
        for (final TaskField field : values()) {
            if (name.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(final int taskID) {
        this.taskID = taskID;
    }

    public String getICalName() {
        return name; // TODO get real ICAL element name
    }
}
