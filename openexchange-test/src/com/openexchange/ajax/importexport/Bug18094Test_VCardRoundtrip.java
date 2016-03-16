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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.VCardExportRequest;
import com.openexchange.ajax.importexport.actions.VCardExportResponse;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.test.ContactTestManager;

/**
 * This test is related to bug 18094: When exporting a VCard, then importing
 * it again, several fields are lost. That is not much of a surprise, since
 * the OX data format and the VCard format don't match perfectly. This test
 * ensures that at least a big amount is transfered.
 *
 * @author tobiasp
 *
 */
public class Bug18094Test_VCardRoundtrip extends AbstractManagedContactTest {

	private Contact contact;

	public Bug18094Test_VCardRoundtrip(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		contact = ContactTestManager.generateFullContact(folderID);
		manager.newAction(contact);
	}

	public void testFullVCardRoundtrip() throws Exception{
		VCardExportRequest exportRequest = new VCardExportRequest(folderID, false);
		VCardExportResponse exportResponse = manager.getClient().execute(exportRequest);

		String vcard = exportResponse.getVCard();
		manager.deleteAction(contact);

		VCardImportRequest importRequest = new VCardImportRequest(folderID, new ByteArrayInputStream(vcard.getBytes()));
		VCardImportResponse importResponse = manager.getClient().execute(importRequest);

		JSONArray response = (JSONArray) importResponse.getData();
		assertEquals("Precondition: Should only find one contact in there", 1, response.length());

		JSONObject jsonObject = response.getJSONObject(0);

		Contact actual = manager.getAction(
				jsonObject.getInt("folder_id"),
				jsonObject.getInt("id"));

		Set<ContactField> excluded = new HashSet<ContactField>(){{
			add(ContactField.FOLDER_ID);
			add(ContactField.OBJECT_ID);
			add(ContactField.LAST_MODIFIED);
			add(ContactField.MODIFIED_BY);
			add(ContactField.CREATION_DATE);
			add(ContactField.CREATED_BY);
			add(ContactField.INTERNAL_USERID);
			add(ContactField.MARK_AS_DISTRIBUTIONLIST);
			add(ContactField.NUMBER_OF_ATTACHMENTS);
			add(ContactField.NUMBER_OF_DISTRIBUTIONLIST);
			add(ContactField.IMAGE1_URL);
		}};

		List<ContactField> mismatches = new LinkedList<ContactField>();

		for(ContactField field: ContactField.values()){
			if(excluded.contains(field)) {
                continue;
            }
			int number = field.getNumber();
			Object actualValue = actual.get(number);
			Object expectedValue = contact.get(number);

			if(expectedValue == null && actualValue == null) {
                continue;
            }

			if(expectedValue == null || !expectedValue.equals(actualValue)) {
                mismatches.add(field);
            }
		}

		java.util.Collections.sort(mismatches, new Comparator<ContactField>(){
			@Override
            public int compare(ContactField o1, ContactField o2) {
				return o1.toString().compareTo(o2.toString());
			}});
		String fields = Strings.join(mismatches," ");
		//System.out.println(fields);
		assertTrue("Too many ("+mismatches.size()+") fields not surviving the roundtrip: \n"+fields, mismatches.size() < 58);
	}

}
