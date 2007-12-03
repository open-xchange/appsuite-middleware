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

package com.openexchange.tools.versit.filetokenizer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.openexchange.tools.versit.ICalendar;
import com.openexchange.tools.versit.VCard;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.old.VCalendar10;
import com.openexchange.tools.versit.old.VCard21;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class VCardTokenizerTest extends TestCase {

	public static final String VCARD21 = "BEGIN:VCARD\nVERSION:2.1\nN:Prinz;Tobias;;;\nFN:Tobias Prinz\nEMAIL;INTERNET:top@synapps.de\nEMAIL;INTERNET;WORK:top@synapps.de\nTEL:+375293917117\nTEL;CELL:016091408095\nTEL;MAIN:023587192\nADR;HOME:;;Am Piwitt 18;Meinerzhagen;;58540;Deutschland\nCATEGORIES:Nicht abgelegt\nEND:VCARD\n";
	public static final String VCARD30 = "BEGIN:VCARD\nVERSION:3.0\nN:;Svetlana;;;\nFN:Svetlana\nTEL;type=CELL;type=pref:6670373\nCATEGORIES:Nicht abgelegt\nX-ABUID:CBC739E8-694E-4589-8651-8C30E1A6E724\\:ABPerson\nEND:VCARD\n";
	public static final String VCALENDAR10 = "BEGIN:VCALENDAR\nVERSION:1.0\nBEGIN:VEVENT\nSUMMARY:Sample vCalendar event\nDTSTART:20031228T080000\nDTEND:20031228T083000\nEND:VEVENT\nEND:VCALENDAR\n";
	public static final String ICALENDAR = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//hacksw/handcal//NONSGML v1.0//EN\nBEGIN:VEVENT\nDTSTART:19970714T170000Z\nDTEND:19970715T035959Z\nSUMMARY:Bastille Day Party\nEND:VEVENT\nEND:VCALENDAR";
	public static final String MULTI_MONDO_TEST = VCARD21 + VCALENDAR10 + VCARD30 + ICALENDAR;
	public static final String MULTI_MONDO_TEST2 = VCARD21 + VCALENDAR10 + VCARD30 + ICALENDAR + "\n";
	public static final String BUG_7248 = "BEGIN:VCARD\nVERSION:2.1\nN:Colombara;Robert\nFN:Robert Colombara\nADR;WORK:;;;;;;DE\nADR;HOME:;;;;;- / -\nEND:VCARD";
	public static final String BUG_7250 = "BEGIN:VCARD\nVERSION:2.1\nN:Cölömbärä;Röbört\nEND:VCARD";
	public static final String UTF8 = "UTF-8";
	public static final String WIN = "Cp1252";
	
	public void testVCardRecognition() throws IOException{
		prepareTest(VCARD21,1,1, new VersitDefinition[]{VCard21.definition}, new String[]{VCARD21}, UTF8);
		prepareTest(VCARD30,1,1, new VersitDefinition[]{VCard.definition}, new String[]{VCARD30}, UTF8);
		
		prepareTest(VCARD21+VCARD30,2,2, new VersitDefinition[]{VCard21.definition,VCard.definition}, new String[]{VCARD21, VCARD30}, UTF8);
	}
	public void testVCalendarRecognition() throws IOException{
		prepareTest(VCALENDAR10,1,1, new VersitDefinition[]{VCalendar10.definition}, new String[]{VCALENDAR10}, UTF8);
		prepareTest(ICALENDAR,1,1, new VersitDefinition[]{ICalendar.definition}, new String[]{ICALENDAR}, UTF8);
	}
	public void testAll() throws IOException{
		//without newline
		prepareTest(MULTI_MONDO_TEST,4,4, new VersitDefinition[]{
			VCard21.definition,
			VCalendar10.definition,
			VCard.definition,
			ICalendar.definition
		}, new String[]{
			VCARD21,
			VCALENDAR10,
			VCARD30,
			ICALENDAR}, 
		UTF8);
		//with newline on end
		prepareTest(MULTI_MONDO_TEST2,4,4, new VersitDefinition[]{
				VCard21.definition,
				VCalendar10.definition,
				VCard.definition,
				ICalendar.definition
			}, new String[]{
				VCARD21,
				VCALENDAR10,
				VCARD30,
				ICALENDAR}, 
			UTF8);
	}
	
	public void test7248() throws IOException{
		prepareTest(BUG_7248, 1, 1, new VersitDefinition[]{VCard21.definition}, new String[]{BUG_7248}, UTF8);
	}
	
	public void test7250() throws IOException{
		prepareTest(BUG_7250, 1, 1, new VersitDefinition[]{VCard21.definition}, new String[]{BUG_7250}, WIN);
	}

	
	private void prepareTest(String content, int entriesFound, int entriesRecognized, VersitDefinition[] compDefs, String[] expectedContent, String encoding) throws IOException{
		VCardTokenizer chunky = new VCardTokenizer(new ByteArrayInputStream( content.getBytes(encoding) ));
		List<VCardFileToken> l = chunky.split();
		assertTrue("Correct number of entries", chunky.getNumberOfEntriesFound() == entriesFound);
		assertTrue("Correct number of recognized entries", chunky.getNumberOfEntriesRecognized() == entriesRecognized);
		for(int i = 0; i < l.size(); i++){
			assertEquals("Correct type recognized at entry #"+i+"?" , compDefs[i] , l.get(i).getVersitDefinition());
			assertEquals("Contains expected content for entry #"+i+"?", expectedContent[i].trim(), new String(l.get(i).getContent(), encoding).trim());
		}
	}
}
