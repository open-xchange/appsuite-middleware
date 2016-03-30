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
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

public class Bug20360Test_UmlautBreaksImport extends AbstractManagedContactTest {

	private final String vcard = 
		"BEGIN:VCARD\n" + 
		"VERSION:3.0\n" + 
		"N;CHARSET=UTF-8:T\u00e4st;\u00dcser\n" + 
		"FN;CHARSET=UTF-8:Str\u00e4to\n" + 
		"EMAIL;TYPE=PREF,INTERNET:schneider@str\u00e4to.de\n" +
		"EMAIL:schneider@strato.de\n" +
		"END:VCARD\n";

	public Bug20360Test_UmlautBreaksImport(String name) {
		super(name);
	}
	
	public void testUmlaut() throws IOException, JSONException, OXException{
		VCardImportRequest importRequest = new VCardImportRequest(folderID, new ByteArrayInputStream(vcard.getBytes("UTF-8")));
		VCardImportResponse importResponse = getClient().execute(importRequest);
		
		JSONArray data = (JSONArray) importResponse.getData();
		JSONObject jsonObject = data.getJSONObject(0);
		int objID = jsonObject.getInt("id");
		
		Contact actual = manager.getAction(folderID, objID);

		assertTrue(actual.containsEmail1());
		assertTrue(actual.containsEmail2());
		assertEquals("schneider@str\u00e4to.de", actual.getEmail1());
		assertEquals("schneider@strato.de", actual.getEmail2());

	}

}
