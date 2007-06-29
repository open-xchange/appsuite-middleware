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

package com.openexchange.tools.versit;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.versit.converter.ConverterPrivacyException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * This test was only written to test additions that I made to 
 * OXContainerConverter, but I was too lazy to implement tests
 * for all cases.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class OXContainerConverterTest extends TestCase {

	public static SessionObject getSession() throws Exception {
		Init.initDB();
		ContactConfig.init();
		ContextStorage.init();
		final UserStorage uStorage = UserStorage.getInstance(new ContextImpl(1));
		int userId = uStorage.getUserId( Init.getAJAXProperty("login") );
		SessionObject sessObj = SessionObjectWrapper.createSessionObject(userId, 1, "csv-tests");
		return sessObj;
	}

	/** 
	 * Test of the private flag.
	 * @throws Exception 
	 */
	public void test7472_forPrivate() throws Exception{
		String versitData = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Apple Computer\\, Inc//iCal 2.0//EN\nBEGIN:VEVENT\nCLASS:PRIVATE\nDTSTART:20070514T150000Z\nDTEND:20070514T163000Z\nLOCATION:Olpe\nSUMMARY:Simple iCal Appointment\nDESCRIPTION:Notes here...\nEND:VEVENT\nEND:VCALENDAR\n";
		isFlaggedAsPrivate(versitData);
	}
	
	/** 
	 * Test of the confidential flag.
	 * @throws Exception 
	 */

	public void test7472_forConfidential() {
		String versitData = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Apple Computer\\, Inc//iCal 2.0//EN\nBEGIN:VEVENT\nCLASS:CONFIDENTIAL\nDTSTART:20070514T150000Z\nDTEND:20070514T163000Z\nLOCATION:Olpe\nSUMMARY:Simple iCal Appointment\nDESCRIPTION:Notes here...\nEND:VEVENT\nEND:VCALENDAR\n";
		try {
			isFlaggedAsPrivate(versitData);
			fail("Should throw privacy exception");
		} catch (ConverterPrivacyException e){
			assertTrue("Should throw privacy exception" , true);
			return;
		} catch (Exception e) {
			fail("Wanted ConverterPrivacyException");
		}
	}

	/** 
	 * Test of the public flag.
	 * @throws Exception 
	 */
	public void test7472_forPublic() throws Exception{
		String versitData = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Apple Computer\\, Inc//iCal 2.0//EN\nBEGIN:VEVENT\nCLASS:PUBLIC\nDTSTART:20070514T150000Z\nDTEND:20070514T163000Z\nLOCATION:Olpe\nSUMMARY:Simple iCal Appointment\nDESCRIPTION:Notes here...\nEND:VEVENT\nEND:VCALENDAR\n";
		assertFalse(isFlaggedAsPrivate(versitData));
	}

	
	public boolean isFlaggedAsPrivate(String versitData) throws Exception{
		VersitDefinition def = ICalendar.definition;
		
		final VersitDefinition.Reader versitReader = def.getReader(
				new ByteArrayInputStream(versitData.getBytes("UTF-8")), "UTF-8");
		
		VersitObject rootVersitObject = def.parseBegin(versitReader);
		VersitObject versitObject = def.parseChild(versitReader, rootVersitObject);
		
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(getSession());
		
		final CalendarDataObject appointmentObj = oxContainerConverter.convertAppointment(versitObject);; 
		return appointmentObj.getPrivateFlag();
	}
	
}
