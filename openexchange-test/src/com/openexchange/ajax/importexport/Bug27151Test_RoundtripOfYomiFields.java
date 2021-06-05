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
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.VCardExportRequest;
import com.openexchange.ajax.importexport.actions.VCardExportResponse;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.ContactTestManager;

public class Bug27151Test_RoundtripOfYomiFields extends AbstractManagedContactTest {

    private Contact contact;

    @Test
    public void testShouldImportXPhoneticAsYomiField() throws Exception, Exception, Exception {
        contact = ContactTestManager.generateFullContact(folderID);
        contact.setYomiFirstName("YomiFirstName1");
        contact.setYomiLastName("YomiLastName1");
        cotm.newAction(contact);

        // Back...
        VCardExportResponse exportResponse = getClient().execute(new VCardExportRequest(folderID, true));
        String vCard = exportResponse.getVCard();

        assertTrue(vCard.contains("X-PHONETIC-FIRST-NAME"));
        assertTrue(vCard.contains("X-PHONETIC-LAST-NAME"));
        assertTrue(vCard.contains("YomiFirstName1"));
        assertTrue(vCard.contains("YomiLastName1"));

        // (...clean up...)
        cotm.deleteAction(contact);

        //...and forth!
        VCardImportRequest importRequest = new VCardImportRequest(folderID, new ByteArrayInputStream(vCard.getBytes()));
        VCardImportResponse importResponse = cotm.getClient().execute(importRequest);

        JSONArray response = (JSONArray) importResponse.getData();
        assertEquals("Precondition: Should only find one contact in there", 1, response.length());
        JSONObject jsonObject = response.getJSONObject(0);
        Contact actual = cotm.getAction(folderID, jsonObject.getInt("id"));
        assertEquals("YomiFirstName1", actual.getYomiFirstName());
        assertEquals("YomiLastName1", actual.getYomiLastName());
    }
}
