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

package com.openexchange.calendar;

import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link CalendarField}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public enum CalendarField {

    ID(Appointment.OBJECT_ID, AppointmentFields.ID),
    CREATED_BY(Appointment.CREATED_BY, AppointmentFields.CREATED_BY),
    MODIFIED_BY(Appointment.MODIFIED_BY, AppointmentFields.MODIFIED_BY),
    CREATION_DATE(Appointment.CREATION_DATE, AppointmentFields.CREATION_DATE),
    LAST_MODIFIED(Appointment.LAST_MODIFIED, AppointmentFields.LAST_MODIFIED),
    LAST_MODIFIED_UTC(Appointment.LAST_MODIFIED_UTC, AppointmentFields.LAST_MODIFIED_UTC),
    FOLDER_ID(Appointment.FOLDER_ID, AppointmentFields.FOLDER_ID),
    CATEGORIES(Appointment.CATEGORIES, AppointmentFields.CATEGORIES),
    IDPRIVATE_FLAG(Appointment.PRIVATE_FLAG, AppointmentFields.PRIVATE_FLAG),
    IDCOLOR_LABEL(Appointment.COLOR_LABEL, AppointmentFields.COLORLABEL),
    NUMBER_OF_ATTACHMENTS(Appointment.NUMBER_OF_ATTACHMENTS, AppointmentFields.NUMBER_OF_ATTACHMENTS),
    LAST_MODIFIED_OF_NEWEST_ATTACHMENT(Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, AppointmentFields.LAST_MODIFIED_OF_NEWEST_ATTACHMENT_UTC),
    TITLE(Appointment.TITLE, AppointmentFields.TITLE),
    START_DATE(Appointment.START_DATE, AppointmentFields.START_DATE),
    END_DATE(Appointment.END_DATE, AppointmentFields.END_DATE),
    NOTE(Appointment.NOTE, AppointmentFields.NOTE),
    ALARM(Appointment.ALARM, AppointmentFields.ALARM),
    RECURRENCE_ID(Appointment.RECURRENCE_ID, AppointmentFields.RECURRENCE_ID),
    RECURRENCE_POSITION(Appointment.RECURRENCE_POSITION, AppointmentFields.RECURRENCE_POSITION),
    RECURRENCE_DATE_POSITION(Appointment.RECURRENCE_DATE_POSITION, AppointmentFields.RECURRENCE_DATE_POSITION),
    RECURRENCE_TYPE(Appointment.RECURRENCE_TYPE, AppointmentFields.RECURRENCE_TYPE),
    RECURRENCE_START(Appointment.RECURRENCE_START, AppointmentFields.RECURRENCE_START),
    CHANGE_EXCEPTIONS(Appointment.CHANGE_EXCEPTIONS, AppointmentFields.CHANGE_EXCEPTIONS),
    DELETE_EXCEPTIONS(Appointment.DELETE_EXCEPTIONS, AppointmentFields.DELETE_EXCEPTIONS),
    DAYS(Appointment.DAYS, AppointmentFields.DAYS),
    DAY_IN_MONTH(Appointment.DAY_IN_MONTH, AppointmentFields.DAY_IN_MONTH),
    MONTH(Appointment.MONTH, AppointmentFields.MONTH),
    INTERVAL(Appointment.INTERVAL, AppointmentFields.INTERVAL),
    UNTIL(Appointment.UNTIL, AppointmentFields.UNTIL),
    RECURRENCE_COUNT(Appointment.RECURRENCE_COUNT, AppointmentFields.OCCURRENCES),
    NOTIFICATION(Appointment.NOTIFICATION, AppointmentFields.NOTIFICATION),
    RECURRENCE_CALCULATOR(Appointment.RECURRENCE_CALCULATOR, AppointmentFields.RECURRENCE_CALCULATOR),
    PARTICIPANTS(Appointment.PARTICIPANTS, AppointmentFields.PARTICIPANTS),
    USERS(Appointment.USERS, AppointmentFields.USERS),
    CONFIRMATIONS(Appointment.CONFIRMATIONS, AppointmentFields.CONFIRMATIONS),
    ORGANIZER(Appointment.ORGANIZER, AppointmentFields.ORGANIZER),
    ORGANIZER_ID(Appointment.ORGANIZER_ID, AppointmentFields.ORGANIZER_ID),
    PRINCIPAL(Appointment.PRINCIPAL, AppointmentFields.PRINCIPAL),
    PRINCIPAL_ID(Appointment.PRINCIPAL_ID, AppointmentFields.PRINCIPAL_ID),
    UID(Appointment.UID, AppointmentFields.UID),
    SEQUENCE(Appointment.SEQUENCE, AppointmentFields.SEQUENCE),
    LOCATION(Appointment.LOCATION, AppointmentFields.LOCATION),
    FULL_TIME(Appointment.FULL_TIME, AppointmentFields.FULL_TIME),
    SHOWN_AS(Appointment.SHOWN_AS, AppointmentFields.SHOW_AS),
    TIMEZONE(Appointment.TIMEZONE, AppointmentFields.TIMEZONE);

    private int columnNumber;

    private String jsonName;

    private CalendarField(final int columnNumber, final String jsonName) {
        this.columnNumber = columnNumber;
        this.jsonName = jsonName;
    }

    public static CalendarField getByColumn(final int column) {
        for (final CalendarField field: values()) {
            if (field.getColumnNumber() == column) {
                return field;
            }
        }

        return null;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public String getJsonName() {
        return jsonName;
    }
}
