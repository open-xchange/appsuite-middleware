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

package com.openexchange.groupware.vcard;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

import junit.framework.TestCase;

public class VCardTest extends TestCase {
	
	public final String vcard1 = "BEGIN:VCARD\nVERSION:3.0\nPRODID:OPEN-XCHANGE\nFN:Prinz\\, Tobias\nN:Prinz;Tobias;;;\nNICKNAME:Tierlieb\nBDAY:19810501\nADR;TYPE=work:;;;Meinerzhagen;NRW;58540;DE\nTEL;TYPE=home,voice:+49 2358 7192\nEMAIL:tobias.prinz@open-xchange.com\nORG:- deactivated -\nREV:20061204T160750.018Z\nURL:www.tobias-prinz.de\nUID:80@ox6.netline.de\nEND:VCARD\n";
	public final String vcard2 = "BEGIN:VCARD\nVERSION:3.0\nN:;Svetlana;;;\nFN:Svetlana\nTEL;type=CELL;type=pref:6670373\nCATEGORIES:Nicht abgelegt\nX-ABUID:CBC739E8-694E-4589-8651-8C30E1A6E724\\:ABPerson\nEND:VCARD";
	public final String mime1  = "text/x-vcard";
	public final String mime2  = "text/vcard";
	
	public void test6962() throws IOException, ConverterException{
		performTest("vCard 1 as " + mime1, vcard1, mime1);
		performTest("vCard 2 as " + mime1, vcard2, mime1);
		performTest("vCard 1 as " + mime2, vcard1, mime2);
		performTest("vCard 2 as " + mime2, vcard2, mime2);
	}
	
	public void performTest(String testName, String vcard, String mime) throws ConverterException, IOException{
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(null, null);
		final VersitDefinition def = Versit.getDefinition(mime);
		final VersitDefinition.Reader versitReader = def.getReader(new ByteArrayInputStream(vcard.getBytes("UTF-8")), "UTF-8");
		try {
			VersitObject versitObject = def.parse(versitReader);
			while (versitObject != null) {
				final ContactObject contactObj = oxContainerConverter.convertContact(versitObject);
				versitObject = def.parse(versitReader);
			}
			assertTrue(testName + " passed", true);
		} catch (VersitException e){
			fail(testName + " failed");
		}
	}
}
