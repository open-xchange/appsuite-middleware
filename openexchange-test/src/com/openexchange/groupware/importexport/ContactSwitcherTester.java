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

package com.openexchange.groupware.importexport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactGetter;
import com.openexchange.groupware.contact.helpers.ContactSetter;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForBooleans;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForSimpleDateFormat;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForTimestamp;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.importexport.importers.OutlookCSVContactImporter;

/**
 * This tests setting and getting options of the Switchers used in ...contact.helpers.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ContactSwitcherTester extends TestCase {
	
	public void testSetStringValue() throws ContactException{
		// preparations
		ContactObject conObj = new ContactObject();
		ContactField field = ContactField.GIVEN_NAME;
		String value = "Prinz";
		
		//setting
		conObj = (ContactObject) field.doSwitch(new ContactSetter(), conObj, value);
		
		assertEquals("Setting of String value does work" , conObj.getGivenName(), value);
	}
	
	public void testSetMailValue() throws ContactException{
		// preparations
		ContactObject conObj = new ContactObject();
		ContactField field = ContactField.EMAIL1;
		String value = "prinz@example.invalid";
		
		//setting
		conObj = (ContactObject) field.doSwitch(new ContactSetter(), conObj, value);
		
		assertEquals("Setting of e-mail does work" , conObj.getEmail1(), value);
	}
	
	public void testSetDateValue() throws ContactException{
		// preparations
		ContactObject conObj = new ContactObject();
		ContactField field = ContactField.BIRTHDAY;
		Date value = new Date(System.currentTimeMillis());
		
		//preparing setter for a normal date
		conObj = (ContactObject) field.doSwitch(new ContactSetter(), conObj, value);
		
		assertEquals("Setting of Date value does work" , conObj.getBirthday(), value);
	}
	
	public void testSetDateValueViaTimestamp() throws ContactException{
		// preparations
		ContactObject conObj = new ContactObject();
		ContactField field = ContactField.BIRTHDAY;
		long value = System.currentTimeMillis();
		
		//setting up setter for Timestamp instead of date
		ContactSwitcherForTimestamp switcher = new ContactSwitcherForTimestamp();
		switcher.setDelegate(new ContactSetter());
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		
		assertEquals("Setting of date via timestamp (as long) does work" , conObj.getBirthday(), new Date(value));
		
		String value2 = new Long(value).toString();
		switcher.setDelegate(new ContactSetter());
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value2);
		
		assertEquals("Setting of date via timestamp (as String) does work" , conObj.getBirthday(), new Date(value));
	}
	
	public void testSetDateValueViaSimpleDate() throws ContactException, ParseException{
		// preparations
		ContactObject conObj = new ContactObject();
		ContactField field = ContactField.BIRTHDAY;
		String value = "1981/03/05";

		//setting up a proper setter for SimpleDateFormat
		final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final ContactSwitcherForSimpleDateFormat switcher = new ContactSwitcherForSimpleDateFormat();
		switcher.setDelegate(new ContactSetter());
		switcher.addDateFormat(sdf);

		//setting
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		
		assertEquals("Setting of date via Outlook-simple-date value does work" , conObj.getBirthday(), sdf.parse(value));
	}
	
	public void testGetDateAndName() throws ContactException{
		//preparations
		ContactObject conObj = new ContactObject();
		Date date = new Date(System.currentTimeMillis());
		String nickname = "Tierlieb";
		
		//preparing getter
		final ContactSwitcher switcher = new ContactGetter(); 
		conObj.setBirthday(date);
		conObj.setNickname(nickname);
		
		//reading
		Date compareDate = (Date) ContactField.BIRTHDAY.doSwitch(switcher, conObj, "");
		String compareNickname = (String) ContactField.NICKNAME.doSwitch(switcher, conObj, "");
		
		assertEquals("Checking date", date, compareDate);
		assertEquals("Checking nickname", nickname, compareNickname);
	}
	
	public void testDateSwitchingForBug7552() throws ParseException, ContactException{
		//preparations
		ContactObject conObj = new ContactObject();
		ContactField field = ContactField.BIRTHDAY;

		//setting up a proper setter for SimpleDateFormat
		final ContactSwitcherForSimpleDateFormat switcher = new ContactSwitcherForSimpleDateFormat();
		switcher.setDelegate(new ContactSetter());
		switcher.addDateFormat( OutlookCSVContactImporter.getAmericanDateNotation());
		switcher.addDateFormat( OutlookCSVContactImporter.getGermanDateNotation());

		//setting
		String value = "1981/03/05";
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting of date via Outlook-simple-date value does work" , conObj.getBirthday(), OutlookCSVContactImporter.getAmericanDateNotation().parse(value));
		
		value = "05.03.1981";
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting of date via Outlook-simple-date value does work" , conObj.getBirthday(), OutlookCSVContactImporter.getGermanDateNotation().parse(value));
	}
	
	public void testBooleanSwitchingForBug7710() throws ContactException{
		//preparations
		ContactObject conObj = new ContactObject();
		ContactField field = ContactField.PRIVATE_FLAG;

		//setting up a proper setter for SimpleDateFormat
		final ContactSwitcherForBooleans switcher = new ContactSwitcherForBooleans();
		switcher.setDelegate(new ContactSetter());

		//positive string tests
		String value = "true"; //english outlook
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does work" , true, conObj.getPrivateFlag());
		
		value = "1";
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does work" , true, conObj.getPrivateFlag());
		
		value = "yes";
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does work" , true, conObj.getPrivateFlag());
		
		value = "y";
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does work" , true, conObj.getPrivateFlag());

		value = "Priv\u00e9"; //french outlook
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does work" , true, conObj.getPrivateFlag());

		//negative string tests
		value = "no";
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does no work" , false, conObj.getPrivateFlag());
		
		value = "false"; //english outlook
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does not work" , false, conObj.getPrivateFlag());
		
		value = "wrong";
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does not work" , false, conObj.getPrivateFlag());
		
		value = "0";
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does not work" , false, conObj.getPrivateFlag());
		
		value = "normal"; //french outlook
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does not work" , false, conObj.getPrivateFlag());

		//positive object tests
		Object value2 = 1;
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does work" , true, conObj.getPrivateFlag());
		
		value2 = true;
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does work" , true, conObj.getPrivateFlag());
		
		value2 = new Boolean(true);
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does work" , true, conObj.getPrivateFlag());
		
		//negative object tests
		value2 = 0;
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does not work" , false, conObj.getPrivateFlag());
		
		value2 = false;
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does not work" , false, conObj.getPrivateFlag());
		
		value2 = new Boolean(false);
		conObj = (ContactObject) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does not work" , false, conObj.getPrivateFlag());
		
		
	}
}
