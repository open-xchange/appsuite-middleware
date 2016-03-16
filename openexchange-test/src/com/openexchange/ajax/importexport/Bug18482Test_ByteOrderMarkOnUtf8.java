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

package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.json.JSONArray;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.importexport.actions.CSVImportRequest;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.importexport.ContactTestData;

/**
 * This bug is concerned with data sent as UTF8 stream with a Byte Order Mark (BOM).
 * UTF8 does not need one (it would be pointless actually, since the main improvements
 * of UTF8 are dynamic length and backwards-compatibility to US-ASCII). It is actually
 * recommended not to use one, but of course, some Windows programs insist on using
 * one anyway. This test tries out several BOMs to prove that the import works even
 * in those weird situations.
 *
 * @author tobiasp
 *
 */
public class Bug18482Test_ByteOrderMarkOnUtf8 extends AbstractManagedContactTest {

	String csv = ContactTestData.IMPORT_MULTIPLE;

	public Bug18482Test_ByteOrderMarkOnUtf8(String name) {
		super(name);
	}

	public void testNone() throws Exception{
		testWithBOM();
	}

	public void testUTF8() throws Exception{
		testWithBOM(0xEF,0xBB,0xBF);
	}

	public void testUTF16LE() throws Exception{
		testWithBOM(0xFF,0xFE);
	}

	public void testUTF16BE() throws Exception{
		testWithBOM(0xFE, 0xFF);
	}

	public void testUTF32LE() throws Exception{
		testWithBOM(0xFF,0xFE, 0x00, 0x00);
	}

	public void testUTF32BE() throws Exception{
		testWithBOM(0x00, 0x00, 0xFE, 0xFF);
	}

	private void testWithBOM(int... bom) throws Exception{
		byte[] bytes = csv.getBytes(com.openexchange.java.Charsets.UTF_8);
		byte[] streambase = new byte[bom.length + bytes.length];
		for(int i = 0; i < bom.length; i++) {
            streambase[i] = (byte) bom[i];
        }
		for(int i = bom.length; i < streambase.length; i++) {
            streambase[i] = bytes[i - bom.length];
        }

		InputStream stream = new ByteArrayInputStream( streambase );
		CSVImportRequest importRequest = new CSVImportRequest(folderID, stream, false);
		AbstractAJAXResponse response = manager.getClient().execute(importRequest);

		assertFalse(response.hasError());
		assertFalse(response.hasConflicts());

		JSONArray data = (JSONArray) response.getData();
		assertEquals(2, data.length());

		Contact c1 = manager.getAction(folderID, data.getJSONObject(0).getInt("id"));
		Contact c2 = manager.getAction(folderID, data.getJSONObject(1).getInt("id"));
		assertTrue(c1.getGivenName().equals(ContactTestData.NAME1));
		assertTrue(c2.getGivenName().equals(ContactTestData.NAME2));
	}

}
