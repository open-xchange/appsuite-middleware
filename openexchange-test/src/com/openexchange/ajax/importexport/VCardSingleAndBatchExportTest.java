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
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.VCardExportRequest;
import com.openexchange.ajax.importexport.actions.VCardExportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link VCardSingleAndBatchExportTest}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10
 */
public class VCardSingleAndBatchExportTest extends AbstractManagedContactTest {

    public VCardSingleAndBatchExportTest() {
        super();
    }

    @Test
    public void testVCardSingleExport() throws OXException, IOException, JSONException {
        Contact contact = generateContact("Singlecontact");
        int contactId = cotm.newAction(contact).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, contactId));
        String body = array.toString();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(-1, Boolean.TRUE, true, body));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("One vCard expected!", 1, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), contact.getGivenName()+" "+contact.getSurName()+".vcf");
    }

    @Test
    public void testVCardSingleDistributionListExport() throws OXException, IOException, JSONException {
        Contact list = generateContact("Distribution list");
        list.setDistributionList(new DistributionListEntryObject[] { new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
        });

        int distlistId = cotm.newAction(list).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, distlistId));
        String body = array.toString();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(-1, Boolean.TRUE, true, body));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("One vCard expected!", 1, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), list.getDisplayName()+".vcf");
    }

    @Test
    public void testVCardMultipleExport() throws OXException, IOException, JSONException {
        Contact firstContact = generateContact("First Contact");
        int firstId = cotm.newAction(firstContact).getObjectID();

        Contact secondContact = generateContact("Second Contact");
        int secondId = cotm.newAction(secondContact).getObjectID();

        Contact list = generateContact("Distribution list");
        list.setDistributionList(new DistributionListEntryObject[] { new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
        });
        int thirdId = cotm.newAction(list).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, firstId));
        array.put(addRequestIds(folderID, secondId));
        array.put(addRequestIds(folderID, thirdId));
        String body = array.toString();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(-1, Boolean.TRUE, true, body));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("Three vCard expected!", 3, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), folderName1+".vcf");
    }

    @Test
    public void testVCardOldFolderExport() throws OXException, IOException, JSONException {
        Contact firstContact = generateContact("First Contact");
        cotm.newAction(firstContact).getObjectID();

        Contact secondContact = generateContact("Second Contact");
        cotm.newAction(secondContact).getObjectID();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(folderID, false));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("Two vCards expected!", 2, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), folderName1+".vcf");
    }

    @Test
    public void testCrossFolderBatchExportTest() throws OXException, IOException, JSONException {
        Contact firstContact = generateContact("First Contact");
        int firstId = cotm.newAction(firstContact).getObjectID();

        Contact secondContact = generateContact("Second Contact");
        int secondId = cotm.newAction(secondContact).getObjectID();

        Contact thirdContact = generateContact("Third Contact", secondFolderID);
        int thirdId = cotm.newAction(thirdContact).getObjectID();

        Contact list = generateContact("Distribution list", secondFolderID);
        list.setDistributionList(new DistributionListEntryObject[] { new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
        });
        int fourthId = cotm.newAction(list).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, firstId));
        array.put(addRequestIds(folderID, secondId));
        array.put(addRequestIds(secondFolderID, thirdId));
        array.put(addRequestIds(secondFolderID, fourthId));
        String body = array.toString();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(-1, Boolean.TRUE, true, body));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("Four vCards expected!", 4, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), "Contacts.vcf");
    }

    @Test
    public void testInvalidFileNameExport() throws JSONException, OXException, IOException {
        Contact firstContact = generateContact("First \"Contact\" Contact");
        int firstId = cotm.newAction(firstContact).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, firstId));

        String body = array.toString();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(-1, Boolean.TRUE, true, body));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("One vCards expected!", 1, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), "Export.vcf");
    }

}
