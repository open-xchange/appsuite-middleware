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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.contact.action.AutocompleteRequest;
import com.openexchange.ajax.framework.CommonSearchResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link AutocompleteTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AutocompleteTest extends AbstractManagedContactTest {

    @Test
    public void testAutocompleteFirstAndLastname() throws Exception {
        /*
         * create contact
         */
        Contact contact = super.generateContact("Otto");
        contact.setGivenName("Heinz");
        contact.setDisplayName("Otto, Heinz");
        contact = cotm.newAction(contact);
        /*
         * check different queries
         */
        String parentFolderID = String.valueOf(contact.getParentFolderID());
        String[] queries = new String[] { "Otto", "Heinz", "Heinz Otto", "\"Otto, Heinz\"" };
        for (String query : queries) {
            AutocompleteRequest request = new AutocompleteRequest(query, false, parentFolderID, Contact.ALL_COLUMNS, true);
            CommonSearchResponse response = getClient().execute(request);
            List<Contact> contacts = cotm.transform((JSONArray) response.getResponse().getData(), Contact.ALL_COLUMNS);
            assertNotNull(contacts);
            assertEquals("wrong number of results", 1, contacts.size());
            assertEquals(contact.getDisplayName(), contacts.get(0).getDisplayName());
        }
    }

    @Test
    public void testAutocompleteSpecificContact() throws Exception {
        /*
         * create contacts
         */
        Contact contact = super.generateContact("Otto");
        contact.setGivenName("Heinz");
        contact.setDisplayName("Otto, Heinz");
        contact = cotm.newAction(contact);
        Contact contact2 = super.generateContact("Otto");
        contact2.setGivenName("Horst");
        contact2.setDisplayName("Otto, Horst");
        contact2 = cotm.newAction(contact2);
        Contact contact3 = super.generateContact("Wurst");
        contact3.setGivenName("Heinz");
        contact3.setDisplayName("Wurst, Heinz");
        contact3 = cotm.newAction(contact3);
        /*
         * check query
         */
        String parentFolderID = String.valueOf(contact.getParentFolderID());
        AutocompleteRequest request = new AutocompleteRequest("Heinz Otto", false, parentFolderID, Contact.ALL_COLUMNS, true);
        CommonSearchResponse response = getClient().execute(request);
        List<Contact> contacts = cotm.transform((JSONArray) response.getResponse().getData(), Contact.ALL_COLUMNS);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 1, contacts.size());
        assertEquals(contact.getDisplayName(), contacts.get(0).getDisplayName());
    }

    @Test
    public void testAutocompleteDistributionList() throws Exception {
        Contact distributionList = new Contact();
        distributionList.setParentFolderID(folderID);
        distributionList.setSurName("LisTe");
        distributionList.setGivenName("VerTeiLer");
        distributionList.setDisplayName(distributionList.getGivenName() + " " + distributionList.getSurName());
        distributionList.setDistributionList(new DistributionListEntryObject[] { new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT), new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT)
        });
        distributionList = cotm.newAction(distributionList);
        /*
         * expect in auto-complete response
         */
        AutocompleteRequest request = new AutocompleteRequest(distributionList.getGivenName(), false, Integer.toString(folderID), Contact.ALL_COLUMNS, true);
        CommonSearchResponse response = getClient().execute(request);
        List<Contact> contacts = cotm.transform((JSONArray) response.getResponse().getData(), Contact.ALL_COLUMNS);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 1, contacts.size());
        assertEquals(distributionList.getDisplayName(), contacts.get(0).getDisplayName());
    }

}
