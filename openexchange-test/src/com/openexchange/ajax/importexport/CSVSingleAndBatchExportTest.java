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
import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.CSVExportRequest;
import com.openexchange.ajax.importexport.actions.CSVExportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.importexport.csv.CSVParser;


/**
 * {@link CSVSingleAndBatchExportTest}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10
 */
public class CSVSingleAndBatchExportTest extends AbstractManagedContactTest {

    @Test
    public void testCSVSingleExport() throws OXException, IOException, JSONException {
        generateContact("First Contact");
        Contact secondContact = generateContact("Second Contact");
        int secondId = cotm.newAction(secondContact).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, secondId));
        String body = array.toString();

        CSVExportResponse exportResponse = getClient().execute(new CSVExportRequest(-1, true, body));

        CSVParser parser = new CSVParser();
        List<List<String>> actual = parser.parse((String) exportResponse.getData());
        List<String> testList = actual.get(1);
        assertTrue(testList.toString().contains(secondContact.getSurName()));
        assertEquals("There should only be one exported Contact", 2, actual.size());
        assertFileName(exportResponse.getHttpResponse(), secondContact.getGivenName()+" "+secondContact.getSurName()+".csv");
    }

    @Test
    public void testCSVSingleDistributionListExport() throws OXException, JSONException, IOException {
        Contact list = generateContact("Distribution list");
        list.setDistributionList(new DistributionListEntryObject[] { new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
        });

        int distlistId = cotm.newAction(list).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, distlistId));
        String body = array.toString();

        CSVExportResponse exportResponse = getClient().execute(new CSVExportRequest(-1, true, body));

        CSVParser parser = new CSVParser();
        List<List<String>> actual = parser.parse((String) exportResponse.getData());
        List<String> testList = actual.get(1);
        assertTrue(testList.toString().contains("my displayname"));
        assertEquals("There should only be one exported Contact", 2, actual.size());
        assertFileName(exportResponse.getHttpResponse(), list.getDisplayName()+".csv");
    }

    @Test
    public void testCSVMultipleExport() throws JSONException, OXException, IOException {
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

        CSVExportResponse exportResponse = getClient().execute(new CSVExportRequest(-1, true, body));

        CSVParser parser = new CSVParser();
        List<List<String>> actual = parser.parse((String) exportResponse.getData());
        assertEquals("There should be three exported Contacts", 4, actual.size());
        List<String> testList = actual.get(1);
        assertTrue(testList.toString().contains(firstContact.getSurName()));
        testList = actual.get(2);
        assertTrue(testList.toString().contains(secondContact.getSurName()));
        testList = actual.get(3);
        assertTrue(testList.toString().contains(list.getDisplayName()));
        assertFileName(exportResponse.getHttpResponse(), folderName1+".csv");
    }

    @Test
    public void testCSVOldFolderExport() throws OXException, IOException, JSONException {
        Contact firstContact = generateContact("First Contact");
        cotm.newAction(firstContact).getObjectID();

        Contact secondContact = generateContact("Second Contact");
        cotm.newAction(secondContact).getObjectID();

        CSVExportResponse exportResponse = getClient().execute(new CSVExportRequest(folderID, true));

        CSVParser parser = new CSVParser();
        List<List<String>> actual = parser.parse((String) exportResponse.getData());
        assertEquals("There should be two exported Contacts", 3, actual.size());
        List<String> testList = actual.get(1);
        assertTrue(testList.toString().contains(firstContact.getSurName()));
        testList = actual.get(2);
        assertTrue(testList.toString().contains(secondContact.getSurName()));
        assertFileName(exportResponse.getHttpResponse(), folderName1+".csv");
    }

    @Test
    public void testCSVCrossFolderBatchExportTest() throws OXException, JSONException, IOException {
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

        CSVExportResponse exportResponse = getClient().execute(new CSVExportRequest(-1, true, body));

        CSVParser parser = new CSVParser();
        List<List<String>> actual = parser.parse((String) exportResponse.getData());
        assertEquals("There should be four exported Contacts", 5, actual.size());
        List<String> testList = actual.get(1);
        assertTrue(testList.toString().contains(firstContact.getSurName()));
        testList = actual.get(2);
        assertTrue(testList.toString().contains(secondContact.getSurName()));
        testList = actual.get(3);
        assertTrue(testList.toString().contains(thirdContact.getSurName()));
        testList = actual.get(4);
        assertTrue(testList.toString().contains(list.getDisplayName()));
        assertFileName(exportResponse.getHttpResponse(), "Contacts.csv");
    }

    @Test
    public void testInvalidFileNameExport() {
        // TODO Auto-generated method stub

    }

}
