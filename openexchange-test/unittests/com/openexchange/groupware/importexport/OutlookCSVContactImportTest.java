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
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.importers.OutlookCSVContactImporter;

public class OutlookCSVContactImportTest extends AbstractContactTest{
	public String IMPORT_HEADERS = ContactField.GIVEN_NAME.getEnglishOutlookName()+","+ContactField.EMAIL1.getEnglishOutlookName()+","+ContactField.BIRTHDAY.getEnglishOutlookName()+"\n";
	public String IMPORT_ONE = IMPORT_HEADERS + NAME1+", "+EMAIL1+", "+DATE1;
	public static String DATE1 = "4/1/1981";

	public OutlookCSVContactImportTest(){
		super();
		defaultFormat = Format.OUTLOOK_CSV;
		imp = new OutlookCSVContactImporter();
	}
	
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(OutlookCSVContactImportTest.class);
	}

	protected void checkFirstResult(final int objectID ) throws OXException, ContextException {
		final ContactObject co = new RdbContactSQLInterface(sessObj).getObjectById(objectID, folderId);
		assertEquals("Checking name" ,  NAME1 , co.getGivenName());
		assertEquals("Checking e-Mail" ,  EMAIL1 , co.getEmail1());
		
		final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Date compDate = null;
		try {
			compDate = sdf.parse(DATE1);
		} catch (final ParseException e) {
			System.out.println("Setup error: Date format used for comparison sucks.");
		}
		assertDateEquals(compDate, co.getBirthday());
	}
	
	@Test
	public void importOneContact() throws NumberFormatException, Exception {
		final List<ImportResult> results = importStuff(IMPORT_ONE); 
		assertEquals("One result?" , (Integer) 1, (Integer) results.size());
		final ImportResult res = results.get(0);
		if(res.hasError()){
			res.getException().printStackTrace();
		}
		assertTrue( res.isCorrect() );

		//basic check: 1 entry in folder
		final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj);
		assertEquals("One contact in folder?", (Integer) 1, (Integer) contactSql.getNumberOfContacts(folderId));

		//detailed check:
		checkFirstResult(
			Integer.parseInt(
				res.getObjectId()));
		
	}
	
	@Test
	public void bug7105() throws NumberFormatException, Exception {
		final List<ImportResult> results = importStuff(IMPORT_ONE+"\n"+NAME2); 
		assertEquals("Two results?" , (Integer) 2 , (Integer) results.size());

		int i = 0;
		for(final ImportResult res : results){
			assertEquals("Entry " + (i++) + " is correct?" , null, res.getException());
		}
		
	}
	
	@Test
	public void bug7552() throws NumberFormatException, Exception {
		final List<ImportResult> results = importStuff(IMPORT_HEADERS + NAME1+", "+EMAIL1+", 1.4.1981"); 
		assertEquals("One result?" , (Integer) 1, (Integer) results.size());
		final ImportResult res = results.get(0);
		if(res.hasError()){
			res.getException().printStackTrace();
		}

		//check date set correctly though German style
		final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj);
		final Date birthday = contactSql.getObjectById( Integer.parseInt(res.getObjectId()) , Integer.parseInt(res.getFolder()) ).getBirthday();
		assertDateEquals(new SimpleDateFormat("dd.MM.yyyy").parse("1.4.1981") , birthday);

		//cleaning up
		contactSql.deleteContactObject(Integer.parseInt(res.getObjectId()), Integer.parseInt(res.getFolder()), res.getDate());
	}
	
	@Test
	public void bug6825_tooMuchInformation() throws ImportExportException, UnsupportedEncodingException {
		final List<ImportResult> results = importStuff(
				IMPORT_HEADERS + 
				"my name is definately too long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long"+
				", "
				+EMAIL1+
				", 1.4.1981"); 
		assertEquals("One result?" , (Integer) 1, (Integer) results.size());
		final ImportResult res = results.get(0);
		assertTrue("Has error" , res.hasError());
		final AbstractOXException dirk = res.getException();
		assertEquals("Is truncation error?" , Category.TRUNCATED , dirk.getCategory());
		assertEquals("GIVEN_NAME is too long?" , ContactField.GIVEN_NAME.getEnglishOutlookName() , dirk.getMessageArgs()[0]);
	}

	/*
	 * "private" flag is being set
	 */
	@Test public void bug7710() throws UnsupportedEncodingException, NumberFormatException, OXException, ContextException {
		String file = ContactField.SUR_NAME.getGermanOutlookName() + ", " + ContactField.PRIVATE_FLAG.getGermanOutlookName() + "\nTobias Prinz,PRIVAT";
		List<ImportResult> results = importStuff(file);
		assertEquals("Only one result", (Integer) 1, (Integer) results.size());
		ImportResult res = results.get(0);
		ContactObject conObj = getEntry( Integer.parseInt( res.getObjectId() ) );
		assertTrue("Is private?", conObj.getPrivateFlag());
		
		file = ContactField.SUR_NAME.getGermanOutlookName() + ", " + ContactField.PRIVATE_FLAG.getGermanOutlookName() + "\nTobias Prinz,√ñFFENTLICH";
		results = importStuff(file);
		assertEquals("Only one result", (Integer) 1, (Integer) results.size());
		res = results.get(0);
		conObj = getEntry( Integer.parseInt( res.getObjectId() ) );
		assertTrue("Is private?", !conObj.getPrivateFlag());
	}
	
	/*
	 * several fields are missing after the following round-trip: 
	 * OX Contact -> sync via OutlookOXtender -> CSV Export in Outlook -> CSV import with OX
	 * This test confirmed that it was simply missing translations for those fields.
	 */
	@Test public void bug9367_should_translate_several_more_fields() throws UnsupportedEncodingException, NumberFormatException, OXException, ContextException {
		final String file = "\"Anrede\",\"Vorname\",\"Weitere Vornamen\",\"Nachname\",\"Suffix\",\"Firma\",\"Abteilung\",\"Position\",\"Straße geschäftlich\",\"Straße geschäftlich 2\",\"Straße geschäftlich 3\",\"Ort geschäftlich\",\"Region geschäftlich\",\"Postleitzahl geschäftlich\",\"Land/Region geschäftlich\",\"Straße privat\",\"Straße privat 2\",\"Straße privat 3\",\"Ort privat\",\"Bundesland/Kanton privat\",\"Postleitzahl privat\",\"Land/Region privat\",\"Weitere Straße\",\"Weitere Straße 2\",\"Weitere Straße 3\",\"Weiterer Ort\",\"Weiteres/r Bundesland/Kanton\",\"Weitere Postleitzahl\",\"Weiteres/e Land/Region\",\"Telefon Assistent\",\"Fax geschäftlich\",\"Telefon geschäftlich\",\"Telefon geschäftlich 2\",\"Rückmeldung\",\"Autotelefon\",\"Telefon Firma\",\"Fax privat\",\"Telefon privat\",\"Telefon privat 2\",\"ISDN\",\"Mobiltelefon\",\"Weiteres Fax\",\"Weiteres Telefon\",\"Pager\",\"Haupttelefon\",\"Mobiltelefon 2\",\"Telefon für Hörbehinderte\",\"Telex\",\"Abrechnungsinformation\",\"Benutzer 1\",\"Benutzer 2\",\"Benutzer 3\",\"Benutzer 4\",\"Beruf\",\"Büro\",\"E-Mail-Adresse\",\"E-Mail-Typ\",\"E-Mail: Angezeigter Name\",\"E-Mail 2: Adresse\",\"E-Mail 2: Typ\",\"E-Mail 2: Angezeigter Name\",\"E-Mail 3: Adresse\",\"E-Mail 3: Typ\",\"E-Mail 3: Angezeigter Name\",\"Empfohlen von\",\"Geburtstag\",\"Geschlecht\",\"Hobby\",\"Initialen\",\"Internet-Frei/Gebucht\",\"Jahrestag\",\"Kategorien\",\"Kinder\",\"Konto\",\"Name Assistent\",\"Name des/der Vorgesetzten\",\"Notizen\",\"Organisationsnr.\",\"Ort\",\"Partner\",\"Postfach geschäftlich\",\"Postfach privat\",\"Priorität\",\"Privat\",\"Regierungsnr.\",\"Reisekilometer\",\"Sprache\",\"Stichwörter\",\"Vertraulichkeit\",\"Verzeichnisserver\",\"Webseite\",\"Weiteres Postfach\""+ 
		"\n\"Anrede\",\"Vorname\",\"Zweiter Vorname\",\"Nachname\",\"Namenszusatz\",\"Firma\",\"Abteilung\",\"Position\",\"Straße\",,,\"Stadt\",\"Bundesland\",\"PLZ\",\"Land\",\"Straße\",,,\" Stadt \",\"Bundesland\",\"PLZ\",\"Land\",\"Straße (weitere)\",,,\"Stadt (weitere)\",\"Bundesland (weiteres)\",\"PLZ\",\"Land (weiteres)\",,\"Fax (geschäftlich)\",\"Telefon (geschäftlich)\",\"Telefon (geschäftlich 2)\",,\"Autotelefon\",\"Telefon (Zentrale)\",\"Fax (privat)\",\"Telefon (privat)\",\"Telefon (privat 2)\",,\"Mobiltelefon\",\"Fax (weiteres)\",\"Telefon (weiteres)\",\"Pager\",,,\"Texttelefon\",\"Telex\",,,,,,\"Beruf\",\"Raumnummer\",\"email@geschaeftlich.tld\",\"SMTP\",\"Angezeigter Name (email@geschaeftlich.tld)\",\"email@privat.tld\",\"SMTP\",\"Angezeigter Name (email@privat.tld)\",\"E-Mail (weitere)\",\"SMTP\",\"Angezeigter Name (E-Mail (weitere))\",,\"10.9.2007\",\"Keine Angabe\",,,,\"9.9.2007\",\"Tag1\",,,\"Assistent\",\"Manager\",\"Anmerkungen\",,,\"Ehepartner\",,,\"Niedrig\",\"Ein\",,,,,\"Privat\",,\"URL\"";

		final List<ImportResult> results = importStuff(file, "cp1252");
		assertEquals("Only one result" , (Integer) 1 , (Integer) results.size() );
		final ImportResult res = results.get(0);
		final ContactObject conObj = getEntry( Integer.parseInt( res.getObjectId() ) );
		assertEquals("email@geschaeftlich.tld", conObj.getEmail1() );
		assertEquals("Position", conObj.getPosition() );
		assertEquals("Raumnummer", conObj.getRoomNumber() );
		assertEquals("Bundesland", conObj.getStateHome() );
		assertEquals("Land", conObj.getCountryHome() );
		assertEquals("Bundesland (weiteres)", conObj.getStateOther() );
		assertEquals("Land (weiteres)", conObj.getCountryOther() );
		assertEquals("E-Mail (weitere)", conObj.getEmail3() );
		assertEquals("Land", conObj.getStateBusiness() );
	}
	
	
	public void assertDateEquals(final Date date1 , final Date date2){
		final Calendar c1 = new GregorianCalendar(), c2 = new GregorianCalendar();
		c1.setTime(date1);
		c2.setTime(date2);
		assertEquals("Day", 
				(Integer) c1.get(Calendar.DAY_OF_MONTH),
				(Integer) c2.get(Calendar.DAY_OF_MONTH));
		assertEquals("Month", 
				(Integer) c1.get(Calendar.MONTH), 
				(Integer) c2.get(Calendar.MONTH));
		assertEquals("Year", 
				(Integer) c1.get(Calendar.YEAR), 
				(Integer) c2.get(Calendar.YEAR));
	}

	
}
