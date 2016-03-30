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

package com.openexchange.groupware.importexport;

import com.openexchange.exception.OXException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.openexchange.ajax.fields.ExtendedContactFields;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactGetter;
import com.openexchange.groupware.contact.helpers.ContactSetter;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForBooleans;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForSimpleDateFormat;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForTimestamp;
import com.openexchange.groupware.contact.helpers.SplitBirthdayFieldsSetter;
import com.openexchange.groupware.container.Contact;

/**
 * This tests setting and getting options of the Switchers used in ...contact.helpers.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ContactSwitcherTester extends TestCase {

	public void testSetStringValue() throws OXException{
		// preparations
		Contact conObj = new Contact();
		final ContactField field = ContactField.GIVEN_NAME;
		final String value = "Prinz";

		//setting
		conObj = (Contact) field.doSwitch(new ContactSetter(), conObj, value);

		assertEquals("Setting of String value does work" , conObj.getGivenName(), value);
	}

	public void testSetMailValue() throws OXException{
		// preparations
		Contact conObj = new Contact();
		final ContactField field = ContactField.EMAIL1;
		final String value = "prinz@example.invalid";

		//setting
		conObj = (Contact) field.doSwitch(new ContactSetter(), conObj, value);

		assertEquals("Setting of e-mail does work" , conObj.getEmail1(), value);
	}

	public void testSetDateValue() throws OXException{
		// preparations
		Contact conObj = new Contact();
		final ContactField field = ContactField.BIRTHDAY;
		final Date value = new Date(System.currentTimeMillis());

		//preparing setter for a normal date
		conObj = (Contact) field.doSwitch(new ContactSetter(), conObj, value);

		assertEquals("Setting of Date value does work" , conObj.getBirthday(), value);
	}

	public void testSetDateValueViaTimestamp() throws OXException{
		// preparations
		Contact conObj = new Contact();
		final ContactField field = ContactField.BIRTHDAY;
		final long value = System.currentTimeMillis();

		//setting up setter for Timestamp instead of date
		final ContactSwitcherForTimestamp switcher = new ContactSwitcherForTimestamp();
		switcher.setDelegate(new ContactSetter());
		conObj = (Contact) field.doSwitch(switcher, conObj, value);

		assertEquals("Setting of date via timestamp (as long) does work" , conObj.getBirthday(), new Date(value));

		final String value2 = new Long(value).toString();
		switcher.setDelegate(new ContactSetter());
		conObj = (Contact) field.doSwitch(switcher, conObj, value2);

		assertEquals("Setting of date via timestamp (as String) does work" , conObj.getBirthday(), new Date(value));
	}

	public void testSetDateValueViaSimpleDate() throws OXException, ParseException{
		// preparations
		Contact conObj = new Contact();
		final ContactField field = ContactField.BIRTHDAY;
		final String value = "1981/03/05";

		//setting up a proper setter for SimpleDateFormat
		final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final ContactSwitcherForSimpleDateFormat switcher = new ContactSwitcherForSimpleDateFormat();
		switcher.setDelegate(new ContactSetter());
		switcher.addDateFormat(sdf);

		//setting
		conObj = (Contact) field.doSwitch(switcher, conObj, value);

		assertEquals("Setting of date via Outlook-simple-date value does work" , conObj.getBirthday(), sdf.parse(value));
	}

	public void testGetDateAndName() throws OXException{
		//preparations
		final Contact conObj = new Contact();
		final Date date = new Date(System.currentTimeMillis());
		final String nickname = "Tierlieb";

		//preparing getter
		final ContactSwitcher switcher = new ContactGetter();
		conObj.setBirthday(date);
		conObj.setNickname(nickname);

		//reading
		final Date compareDate = (Date) ContactField.BIRTHDAY.doSwitch(switcher, conObj, "");
		final String compareNickname = (String) ContactField.NICKNAME.doSwitch(switcher, conObj, "");

		assertEquals("Checking date", date, compareDate);
		assertEquals("Checking nickname", nickname, compareNickname);
	}

	public void testUnkownFieldHandling() throws OXException{
	    assertFalse("Should return false when getting unknown field", new ContactGetter()._unknownfield(null,"field","value"));
	    assertFalse("Should return false when setting unknown field", new ContactSetter()._unknownfield(null,"field","value"));
	}

	public void testSplitBirthdayFieldHandling() throws OXException{
	    SplitBirthdayFieldsSetter switcher = new SplitBirthdayFieldsSetter();
	    Contact contact = new Contact();
	    Integer day = 31, month = 12, year = 1970;
	    switcher._unknownfield(contact, ExtendedContactFields.BIRTHDAY_DAY, day);
	    switcher._unknownfield(contact, ExtendedContactFields.BIRTHDAY_MONTH, month);
	    switcher._unknownfield(contact, ExtendedContactFields.BIRTHDAY_YEAR, year);
	    Calendar expected = Calendar.getInstance();
	    expected.setTime(contact.getBirthday());

	    assertEquals("Day should match", day.intValue(), expected.get(Calendar.DAY_OF_MONTH));
	    assertEquals("Month should match", Calendar.DECEMBER, expected.get(Calendar.MONTH));
	    assertEquals("Year should match", year.intValue(), expected.get(Calendar.YEAR));
	}

	public void testDateSwitchingForBug7552() throws Exception{
	    Init.startServer();
		//preparations
		Contact conObj = new Contact();
		final ContactField field = ContactField.BIRTHDAY;

		//setting up a proper setter for SimpleDateFormat
		final ContactSwitcherForSimpleDateFormat switcher = new ContactSwitcherForSimpleDateFormat();
		switcher.setDelegate(new ContactSetter());
		SimpleDateFormat americanDateFormat = new SimpleDateFormat("yyyy/dd/MM");
		SimpleDateFormat germanDateFormat = new SimpleDateFormat("dd.MM.yyyy");
		switcher.addDateFormat( americanDateFormat);
		switcher.addDateFormat( germanDateFormat);

		//setting
		String value = "1981/03/05";
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting of date via Outlook-simple-date value does work" , conObj.getBirthday(), americanDateFormat.parse(value));

		value = "05.03.1981";
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting of date via Outlook-simple-date value does work" , conObj.getBirthday(), germanDateFormat.parse(value));
	}

	public void testBooleanSwitchingForBug7710() throws OXException{
		//preparations
		Contact conObj = new Contact();
		final ContactField field = ContactField.PRIVATE_FLAG;

		//setting up a proper setter for SimpleDateFormat
		final ContactSwitcherForBooleans switcher = new ContactSwitcherForBooleans();
		switcher.setDelegate(new ContactSetter());

		//positive string tests
		String value = "true"; //english outlook
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does work" , true, conObj.getPrivateFlag());

		value = "1";
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does work" , true, conObj.getPrivateFlag());

		value = "yes";
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does work" , true, conObj.getPrivateFlag());

		value = "y";
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does work" , true, conObj.getPrivateFlag());

		value = "Priv\u00e9"; //french outlook
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does work" , true, conObj.getPrivateFlag());

		//negative string tests
		value = "no";
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does no work" , false, conObj.getPrivateFlag());

		value = "false"; //english outlook
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does not work" , false, conObj.getPrivateFlag());

		value = "wrong";
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does not work" , false, conObj.getPrivateFlag());

		value = "0";
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does not work" , false, conObj.getPrivateFlag());

		value = "normal"; //french outlook
		conObj = (Contact) field.doSwitch(switcher, conObj, value);
		assertEquals("Setting private flag via "+value+" does not work" , false, conObj.getPrivateFlag());

		//positive object tests
		Object value2 = 1;
		conObj = (Contact) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does work" , true, conObj.getPrivateFlag());

		value2 = true;
		conObj = (Contact) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does work" , true, conObj.getPrivateFlag());

		value2 = new Boolean(true);
		conObj = (Contact) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does work" , true, conObj.getPrivateFlag());

		//negative object tests
		value2 = 0;
		conObj = (Contact) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does not work" , false, conObj.getPrivateFlag());

		value2 = false;
		conObj = (Contact) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does not work" , false, conObj.getPrivateFlag());

		value2 = new Boolean(false);
		conObj = (Contact) field.doSwitch(switcher, conObj, value2);
		assertEquals("Setting private flag via "+value2+" does not work" , false, conObj.getPrivateFlag());


	}
}
