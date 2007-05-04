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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.JUnit4TestAdapter;

import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.importexport.importers.OutlookCSVContactImporter;

public class OutlookCSVContactImportTest extends CSVContactImportTest{

	public static String DATE1 = "4/1/1981";
	public OutlookCSVContactImportTest(){
		super();
		doDebugging = false;
		imp = new OutlookCSVContactImporter();
		IMPORT_ONE = ContactField.GIVEN_NAME.getOutlookENName()+","+ContactField.EMAIL1.getOutlookENName()+","+ContactField.BIRTHDAY.getOutlookENName()+"\n"+NAME1+", "+EMAIL1+", "+DATE1;
		IMPORT_MULTIPLE = IMPORT_ONE + "\nLaguna, francisco.laguna@open-xchange.com, 3/3/1981\n";
		IMPORT_DUPLICATE = IMPORT_MULTIPLE + "Laguna, francisco.laguna@open-xchange.com, 3/3/1981\n";
	}
	
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(OutlookCSVContactImportTest.class);
	}

	@Override
	protected void checkFirstResult(int objectID ) throws OXException{
		final ContactObject co = new RdbContactSQLInterface(sessObj).getObjectById(objectID, folderId);
		assertEquals("Checking name" ,  NAME1 , co.getGivenName());
		assertEquals("Checking e-Mail" ,  EMAIL1 , co.getEmail1());
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Date compDate = null;
		try {
			compDate = sdf.parse(DATE1);
		} catch (ParseException e) {
			System.out.println("Setup error: Date format used for comparison sucks.");
		}
		assertDateEquals(compDate, co.getBirthday());
	}
	
	public void assertDateEquals(Date date1 , Date date2){
		Calendar c1 = new GregorianCalendar(), c2 = new GregorianCalendar();
		c1.setTime(date1);
		c2.setTime(date2);
		assertEquals("Day", c1.get(Calendar.DAY_OF_MONTH),c2.get(Calendar.DAY_OF_MONTH));
		assertEquals("Month", c1.get(Calendar.MONTH),c2.get(Calendar.MONTH));
		assertEquals("Year", c1.get(Calendar.YEAR),c2.get(Calendar.YEAR));
	}

	
}
