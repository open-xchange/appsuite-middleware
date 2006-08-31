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

import com.openexchange.api.OXTask;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.XmlServlet;
import com.sun.java_cup.internal.parser;
import org.jdom.Element;

/**
 * TaskParser
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class TaskParser extends CalendarParser {
	
	public TaskParser() {
		
	}
	
	protected void parse(Task taskObj, Element eProp) throws Exception {
		if (hasElement(eProp.getChild(OXTask.STATUS, XmlServlet.NS))) {
			taskObj.setStatus(getValueAsInt(eProp.getChild(OXTask.STATUS, XmlServlet.NS)));
		} 

		if (hasElement(eProp.getChild(OXTask.PERCENT_COMPLETE, XmlServlet.NS))) {
			taskObj.setPercentComplete(getValueAsInt(eProp.getChild(OXTask.PERCENT_COMPLETE, XmlServlet.NS)));
		} 
		
		if (hasElement(eProp.getChild(OXTask.PRIORITY, XmlServlet.NS))) {
			taskObj.setPriority(getValueAsInt(eProp.getChild(OXTask.PRIORITY, XmlServlet.NS)));
		} 
		
		if (hasElement(eProp.getChild(OXTask.TARGET_DURATION, XmlServlet.NS))) {
			taskObj.setTargetDuration(getValueAsLong(eProp.getChild(OXTask.TARGET_DURATION, XmlServlet.NS)));
		} 
		
		if (hasElement(eProp.getChild(OXTask.TARGET_COSTS, XmlServlet.NS))) {
			taskObj.setTargetCosts(getValueAsFloat(eProp.getChild(OXTask.TARGET_COSTS, XmlServlet.NS)));
		} 
		
		if (hasElement(eProp.getChild(OXTask.ACTUAL_DURATION, XmlServlet.NS))) {
			taskObj.setActualDuration(getValueAsLong(eProp.getChild(OXTask.ACTUAL_DURATION, XmlServlet.NS)));
		} 

		if (hasElement(eProp.getChild(OXTask.DURATION_TYPE, XmlServlet.NS))) {
			taskObj.setDurationType(getValueAsInt(eProp.getChild(OXTask.DURATION_TYPE, XmlServlet.NS)));
		} 
		
		if (hasElement(eProp.getChild(OXTask.ACTUAL_COSTS, XmlServlet.NS))) {
			taskObj.setActualCosts(getValueAsFloat(eProp.getChild(OXTask.ACTUAL_COSTS, XmlServlet.NS)));
		} 
		
		if (hasElement(eProp.getChild(OXTask.DATE_COMPLETED, XmlServlet.NS))) {
			taskObj.setDateCompleted(getValueAsDate(eProp.getChild(OXTask.DATE_COMPLETED, XmlServlet.NS)));
		} 
		
		if (hasElement(eProp.getChild(OXTask.ALARM, XmlServlet.NS))) {
			taskObj.setAlarm(getValueAsDate(eProp.getChild(OXTask.ALARM, XmlServlet.NS)));
		} 
		
		if (hasElement(eProp.getChild(OXTask.CURRENCY, XmlServlet.NS))) {
			taskObj.setCurrency(getValue(eProp.getChild(OXTask.CURRENCY, XmlServlet.NS)));
		} 		
		
		if (hasElement(eProp.getChild(OXTask.BILLING_INFORMATION, XmlServlet.NS))) {
			taskObj.setBillingInformation(getValue(eProp.getChild(OXTask.BILLING_INFORMATION, XmlServlet.NS)));
		} 	
		
		if (hasElement(eProp.getChild(OXTask.COMPANIES, XmlServlet.NS))) {
			taskObj.setCompanies(getValue(eProp.getChild(OXTask.COMPANIES, XmlServlet.NS)));
		} 		

		if (hasElement(eProp.getChild(OXTask.TRIP_METER, XmlServlet.NS))) {
			taskObj.setTripMeter(getValue(eProp.getChild(OXTask.TRIP_METER, XmlServlet.NS)));
		} 		

		parseElementCalendar(taskObj, eProp);
	}
}
