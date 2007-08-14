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

package com.openexchange.groupware.calendar;

import com.openexchange.groupware.container.AppointmentObject;

/**
 * This class is meant for translating the different names and IDs
 * used in different modules.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public enum CalendarField {
	OBJECTID ( AppointmentObject.OBJECT_ID , "ObjectID" ),
	CREATEDBY ( AppointmentObject.CREATED_BY , "CreatedBy" ),
	CREATIONDATE ( AppointmentObject.CREATION_DATE , "CreationDate" ),
	MODIFIEDBY ( AppointmentObject.MODIFIED_BY , "ModifiedBy" ),
	LASTMODIFIED ( AppointmentObject.LAST_MODIFIED , "LastModified" ),
	PARENTFOLDERID ( AppointmentObject.FOLDER_ID , "ParentFolderID" ),
	TITLE ( AppointmentObject.TITLE , "Title" ),
	STARTDATE ( AppointmentObject.START_DATE , "StartDate" ),
	ENDDATE ( AppointmentObject.END_DATE , "EndDate" ),
	SHOWNAS ( AppointmentObject.SHOWN_AS , "ShownAs" ),
	LOCATION ( AppointmentObject.LOCATION , "Location" ),
	CATEGORIES ( AppointmentObject.CATEGORIES , "Categories" ),
	LABEL ( AppointmentObject.COLOR_LABEL , "Label" ),
	PRIVATEFLAG ( AppointmentObject.PRIVATE_FLAG , "PrivateFlag" ),
	FULLTIME ( AppointmentObject.FULL_TIME , "FullTime" ),
	NOTE ( AppointmentObject.NOTE , "Note" ),
	RECURRENCETYPE ( AppointmentObject.RECURRENCE_TYPE , "RecurrenceType" ),
	INTERVAL ( AppointmentObject.INTERVAL , "Interval" ),
	DAYS ( AppointmentObject.DAYS , "Days" ),
	DAYINMONTH ( AppointmentObject.DAY_IN_MONTH , "DayInMonth" ),
	MONTH ( AppointmentObject.MONTH , "Month" ),
	UNTIL ( AppointmentObject.UNTIL , "Until" ),
	OCCURRENCE ( AppointmentObject.RECURRING_OCCURRENCE , "Occurrence" ),
	RECURRENCEDATEPOSITION ( AppointmentObject.RECURRENCE_DATE_POSITION , "RecurrenceDatePosition" ),
	RECURRENCEPOSITION ( AppointmentObject.RECURRENCE_POSITION , "RecurrencePosition" ),
	TIMEZONE ( AppointmentObject.TIMEZONE , "Timezone" );
	
	
	private int appointmentObjectID; //this is the ID of AppointmentObject
	private String name; 			//this is the name of the internal variable (as used by setters & getters)
	
	
	private CalendarField(int appointmentObjectID, String name){
		this.appointmentObjectID = appointmentObjectID;
		this.name = name;
	}
	
	public static CalendarField getByAppointmentObjectId(int id){
		for(CalendarField field : values()){
			if( id == field.getAppointmentObjectID() ){
				return field;
			}
		}
		return null;
	}
	
	public static CalendarField getByName(String name){
		for(CalendarField field : values()){
			if( name.equals( field.getName() ) ){
				return field;
			}
		}
		return null;
	}

	
	public int getAppointmentObjectID() {
		return appointmentObjectID;
	}

	public void setAppointmentObjectID(int appointmentObjectID) {
		this.appointmentObjectID = appointmentObjectID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getICalElement(){
		return name; //TODO get real ICAL element name
	}
}
