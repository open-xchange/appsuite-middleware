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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import com.openexchange.tools.versit.ICalendar;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;

public class VersitParserTest extends TestCase {

	public List<VersitObject> parse(String data) throws IOException{
		InputStream is = new ByteArrayInputStream( data.getBytes() );
		List<VersitObject> ret = new LinkedList<VersitObject>();
		
		final VersitDefinition def = ICalendar.definition;
		final VersitDefinition.Reader versitReader;
		VersitObject rootVersitObject;
		
		boolean hasMoreObjects = true;
		
		versitReader = def.getReader(is, "UTF-8");
		rootVersitObject = def.parseBegin(versitReader);
		ret.add(rootVersitObject);
		
		while (hasMoreObjects) {
			VersitObject versitObject = null;
			versitObject=  def.parseChild(versitReader, rootVersitObject);
			if (versitObject == null) {
				hasMoreObjects = false;
				break;
			}
			ret.add(versitObject);
			
		}
		return ret;
	}
	
	/*
	 * Parsing of RRULE with COUNT ("only ten times") 
	 */
	public void test7732() throws IOException{
		String ical = 
			"BEGIN:VCALENDAR\n" +
			"PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\n" +
			"VERSION:2.0\n" +
			"METHOD:PUBLISH\n" +
				"BEGIN:VEVENT\n" +
				"CLASS:PUBLIC\n" +
				"CREATED:20070531T130514Z\n" +
				"DESCRIPTION:\n" +
				"DTEND:20070912T083000Z\n" +
				"DTSTAMP:20070531T130514Z\n" +
				"DTSTART:20070912T080000Z\n" +
				"LAST-MODIFIED:20070531T130514Z\n" +
				"LOCATION:loc\n" +
				"PRIORITY:5\n" +
				"RRULE:FREQ=DAILY;COUNT=10\n" +
				"SEQUENCE:0\n" +
				"SUMMARY;LANGUAGE=de:Daily iCal\n" +
				"TRANSP:OPAQUE\n" +
				"UID:040000008200E00074C5B7101A82E008000000005059CADA94A3C701000000000000000010000000A1B56CAC71BB0948833B0C11C333ADB0\n" +
				"END:VEVENT\n" +
			"END:VCALENDAR";
		List<VersitObject> list = parse(ical);
	}
	
	/*
	 * Parsing of RRULE with negative values 
	 * ("last sunday of april") 
	 *
	 */
	public void test7735() throws IOException{
		String ical = 
			"BEGIN:VCALENDAR\n" +
			"VERSION:2.0\n" +
			"PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\n" +
			"BEGIN:VEVENT\n" +
			"DTSTART:20070814T150000Z\n" +
			"DTEND:20070814T163000Z\n" +
			"LOCATION:Olpe\nSUMMARY:Komplizierte Intervalle\n" +
			"DESCRIPTION:Jeden ersten Sonntag im April\n" +
			"RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=4\n" +
			"END:VEVENT\n" +
			"END:VCALENDAR";
		List<VersitObject> list = parse(ical);
		assertEquals("Two elements in list?" , list.size() , 2);
		
	}
}
