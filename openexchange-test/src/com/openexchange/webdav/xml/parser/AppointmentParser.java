/*
 *
 *    OPEN-XCHANGE - "the communication and information enviroment"
 *
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all
 *    other brand and product names are or may be trademarks of, and are
 *    used to identify products or services of, their respective owners.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original code will still remain
 *    copyrighted by the copyright holder(s) or original author(s).
 *
 *
 *     Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 *     mail:	                 info@netline-is.de
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License as published by the Free
 *     Software Foundation; either version 2 of the License, or (at your option)
 *     any later version.
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
 *
 */

package com.openexchange.webdav.xml.parser;

import org.jdom.Element;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.webdav.xml.XmlServlet;
import com.openexchange.webdav.xml.fields.AppointmentFields;

/**
 * AppointmentParser
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class AppointmentParser extends CalendarParser {
	
	public AppointmentParser() {
		
	}
	
	protected void parse(AppointmentObject appointmentObj, Element eProp) throws Exception {
		if (hasElement(eProp.getChild(AppointmentFields.SHOW_AS, XmlServlet.NS))) {
			appointmentObj.setShownAs(getValueAsInt(eProp.getChild(AppointmentFields.SHOW_AS, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(AppointmentFields.FULL_TIME, XmlServlet.NS))) {
			appointmentObj.setFullTime(getValueAsBoolean(eProp.getChild(AppointmentFields.FULL_TIME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(AppointmentFields.LOCATION, XmlServlet.NS))) {
			appointmentObj.setLocation(getValue(eProp.getChild(AppointmentFields.LOCATION, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(AppointmentFields.ALARM, XmlServlet.NS))) {
			appointmentObj.setAlarm(getValueAsInt(eProp.getChild(AppointmentFields.ALARM, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(AppointmentFields.ALARM_FLAG, XmlServlet.NS))) {
			appointmentObj.setAlarmFlag(getValueAsBoolean(eProp.getChild(AppointmentFields.ALARM_FLAG, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(AppointmentFields.IGNORE_CONFLICTS, XmlServlet.NS))) {
			appointmentObj.setIgnoreConflicts(getValueAsBoolean(eProp.getChild(AppointmentFields.IGNORE_CONFLICTS, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(AppointmentFields.DELETE_EXCEPTIONS, XmlServlet.NS))) {
			String[] tmp = getValue(eProp.getChild(AppointmentFields.DELETE_EXCEPTIONS, XmlServlet.NS)).split(",");
			java.util.Date[] delete_exceptions = new java.util.Date[tmp.length];
			
			for (int a = 0; a < delete_exceptions.length; a++) {
				delete_exceptions[a] = new java.util.Date(Long.valueOf(tmp[a]));
			}
			
			appointmentObj.setDeleteExceptions(delete_exceptions);
		}

		if (hasElement(eProp.getChild(AppointmentFields.CHANGE_EXCEPTIONS, XmlServlet.NS))) {
			String[] tmp = getValue(eProp.getChild(AppointmentFields.CHANGE_EXCEPTIONS, XmlServlet.NS)).split(",");
			java.util.Date[] change_exceptions = new java.util.Date[tmp.length];

			
			for (int a = 0; a < change_exceptions.length; a++) {
				change_exceptions[a] = new java.util.Date(Long.valueOf(tmp[a]));
			}
			
			appointmentObj.setChangeExceptions(change_exceptions);
		}

		
		parseElementCalendar(appointmentObj, eProp);
	}
}




