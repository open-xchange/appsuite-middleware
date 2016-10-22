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

package com.openexchange.groupware.calendar;

import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.i18n.Localizable;
import com.openexchange.i18n.LocalizableArgument;

/**
 * This class is meant for translating the different names and IDs
 * used in different modules.
 *
 * TODO: This is not a complete overview, currently it only maps
 * the number set in AppointmentObject and the name of the
 * setter and getter methods (the set... and get... being cut off).
 * It might be extended by the names displayed by the GUI so
 * users might understand error messages better.
 *
 * @see ContactField for a fully implemented version
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public enum CalendarField {

    OBJECTID ( Appointment.OBJECT_ID , "ObjectID", "intfield01" ),
    CREATEDBY ( Appointment.CREATED_BY , "CreatedBy", "created_from" ),
    CREATIONDATE ( Appointment.CREATION_DATE , "CreationDate", "creating_date" ),
    MODIFIEDBY ( Appointment.MODIFIED_BY , "ModifiedBy", "changed_from" ),
    LASTMODIFIED ( Appointment.LAST_MODIFIED , "LastModified", "changing_date" ),
    PARENTFOLDERID ( Appointment.FOLDER_ID , "ParentFolderID", "fid" ),
    TITLE ( Appointment.TITLE , CalendarFieldStrings.TITLE, "field01" ),
    STARTDATE ( Appointment.START_DATE , "StartDate", "timestampfield01"),
    ENDDATE ( Appointment.END_DATE , "EndDate", "timestampfield02" ),
    SHOWNAS ( Appointment.SHOWN_AS , "ShownAs", "intfield06" ),
    LOCATION ( Appointment.LOCATION , CalendarFieldStrings.LOCATION, "field02" ),
    CATEGORIES ( Appointment.CATEGORIES , CalendarFieldStrings.CATEGORIES, "field09" ),
    LABEL ( Appointment.COLOR_LABEL , CalendarFieldStrings.LABEL, "intfield03" ),
    PRIVATEFLAG ( Appointment.PRIVATE_FLAG , "PrivateFlag", "pflag" ),
    FULLTIME ( Appointment.FULL_TIME , "FullTime", "intfield07" ),
    NOTE ( Appointment.NOTE , CalendarFieldStrings.NOTE, "field04" ),
    RECURRENCETYPE ( Appointment.RECURRENCE_TYPE , "RecurrenceType", (String[]) null),
    INTERVAL ( Appointment.INTERVAL , "Interval", (String[]) null ),
    DAYS ( Appointment.DAYS , "Days", (String[]) null ),
    DAYINMONTH ( Appointment.DAY_IN_MONTH , "DayInMonth", (String[]) null ),
    MONTH ( Appointment.MONTH , "Month", (String[]) null ),
    UNTIL ( Appointment.UNTIL , "Until", (String[]) null ),
    OCCURRENCE ( Appointment.RECURRENCE_COUNT , "Occurrence", (String[]) null ),
    RECURRENCEDATEPOSITION ( Appointment.RECURRENCE_DATE_POSITION , "RecurrenceDatePosition", (String[]) null ),
    RECURRENCEPOSITION ( Appointment.RECURRENCE_POSITION , "RecurrencePosition", (String[]) null ),
    TIMEZONE ( Appointment.TIMEZONE , CalendarFieldStrings.TIMEZONE, "timezone" ),
    CHANGEEXCEPTION ( Appointment.CHANGE_EXCEPTIONS, "ChangeExceptions", "field08" ),
    DELETEEXCEPTION ( Appointment.DELETE_EXCEPTIONS, "DeleteExceptions", "field07" ),
    PARTICIPANTS ( Appointment.PARTICIPANTS, CalendarFieldStrings.PARTICIPANTS, "reason", "displayName", "mailAddress" ),
    USERS ( Appointment.USERS, "Users", (String[]) null ),
    RECURRENCECALCULATOR ( Appointment.RECURRENCE_CALCULATOR, "RecurrenceCalculator", "intfield04"),
    ALARM ( Appointment.ALARM, "Alarm", (String[]) null);

    private final int appointmentObjectID; //this is the ID of AppointmentObject
    private final String name;             //this is the name of the internal variable (as used by setters & getters)
    private final String[] dbFields;

    /**
     * Initializes a new {@link CalendarField}.
     */
    private CalendarField(int appointmentObjectID, String name, String...dbFields){
        this.appointmentObjectID = appointmentObjectID;
        this.name = name;
        this.dbFields = dbFields;
    }

    public static CalendarField getByAppointmentObjectId(final int id){
        for(final CalendarField field : values()){
            if( id == field.getAppointmentObjectID() ){
                return field;
            }
        }
        return null;
    }

    public static CalendarField getByName(final String name){
        for(final CalendarField field : values()){
            if( name.equals( field.getName() ) ){
                return field;
            }
        }
        return null;
    }

    public static CalendarField getByDbField(String dbField) {
        if (null == dbField) {
            return null;
        }
        for (CalendarField field : values()) {
            if (null != field.dbFields) {
                for (String s : field.dbFields) {
                    if (dbField.equals(s)) {
                        return field;
                    }
                }
            }
        }
        return null;
    }

    public int getAppointmentObjectID() {
        return appointmentObjectID;
    }

    public String getName() {
        return name;
    }

    public Localizable getLocalizable() {
        return new LocalizableArgument(getName());
    }

    public String getICalElement(){
        return name; //TODO get real ICAL element name
    }

    public String[] getdbFields() {
        return dbFields;
    }
}
