/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
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

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        contact = ContactTestManager.generateFullContact(folderID);
        cotm.newAction(contact);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testFullVCardRoundtrip() throws Exception {
        VCardExportRequest exportRequest = new VCardExportRequest(folderID, false);
        VCardExportResponse exportResponse = cotm.getClient().execute(exportRequest);

        String vcard = exportResponse.getVCard();
        cotm.deleteAction(contact);

        VCardImportRequest importRequest = new VCardImportRequest(folderID, new ByteArrayInputStream(vcard.getBytes()));
        VCardImportResponse importResponse = cotm.getClient().execute(importRequest);

        JSONArray response = (JSONArray) importResponse.getData();
        assertEquals("Precondition: Should only find one contact in there", 1, response.length());

        JSONObject jsonObject = response.getJSONObject(0);

        Contact actual = cotm.getAction(jsonObject.getInt("folder_id"), jsonObject.getInt("id"));

        @SuppressWarnings("serial") Set<ContactField> excluded = new HashSet<ContactField>() {

            {
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
            }
        };

        List<ContactField> mismatches = new LinkedList<ContactField>();

        for (ContactField field : ContactField.values()) {
            if (excluded.contains(field)) {
                continue;
            }
            int number = field.getNumber();
            Object actualValue = actual.get(number);
            Object expectedValue = contact.get(number);

            if (expectedValue == null && actualValue == null) {
                continue;
            }

            if (expectedValue == null || !expectedValue.equals(actualValue)) {
                mismatches.add(field);
            }
        }

        java.util.Collections.sort(mismatches, new Comparator<ContactField>() {

            @Override
            public int compare(ContactField o1, ContactField o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        String fields = Strings.join(mismatches, " ");
        //System.out.println(fields);
        assertTrue("Too many (" + mismatches.size() + ") fields not surviving the roundtrip: \n" + fields, mismatches.size() < 58);
    }

}
