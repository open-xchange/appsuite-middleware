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

package com.openexchange.ajax.contact;

import java.util.List;

import org.json.JSONArray;

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

    /**
     * Initializes a new {@link AutocompleteTest}.
     *
     * @param name The test name
     */
    public AutocompleteTest(String name) {
        super(name);
    }

	@Override
	public void setUp() throws Exception {
	    super.setUp();
	}

    public void testAutocompleteFirstAndLastname() throws Exception {
        /*
         * create contact
         */
        Contact contact = super.generateContact("Otto");
        contact.setGivenName("Heinz");
        contact.setDisplayName("Otto, Heinz");
        contact = manager.newAction(contact);
        /*
         * check different queries
         */
        String parentFolderID = String.valueOf(contact.getParentFolderID());
        String[] queries = new String[] { "Otto", "Heinz", "Heinz Otto", "\"Otto, Heinz\"" };
        for (String query : queries) {
            AutocompleteRequest request = new AutocompleteRequest(query, false, parentFolderID, Contact.ALL_COLUMNS, true);
            CommonSearchResponse response = client.execute(request);
            List<Contact> contacts = manager.transform((JSONArray) response.getResponse().getData(), Contact.ALL_COLUMNS);
            assertNotNull(contacts);
            assertEquals("wrong number of results", 1, contacts.size());
            assertEquals(contact.getDisplayName(), contacts.get(0).getDisplayName());
        }
    }

    public void testAutocompleteSpecificContact() throws Exception {
        /*
         * create contacts
         */
        Contact contact = super.generateContact("Otto");
        contact.setGivenName("Heinz");
        contact.setDisplayName("Otto, Heinz");
        contact = manager.newAction(contact);
        Contact contact2 = super.generateContact("Otto");
        contact2.setGivenName("Horst");
        contact2.setDisplayName("Otto, Horst");
        contact2 = manager.newAction(contact2);
        Contact contact3 = super.generateContact("Wurst");
        contact3.setGivenName("Heinz");
        contact3.setDisplayName("Wurst, Heinz");
        contact3 = manager.newAction(contact3);
        /*
         * check query
         */
        String parentFolderID = String.valueOf(contact.getParentFolderID());
        AutocompleteRequest request = new AutocompleteRequest("Heinz Otto", false, parentFolderID, Contact.ALL_COLUMNS, true);
        CommonSearchResponse response = client.execute(request);
        List<Contact> contacts = manager.transform((JSONArray) response.getResponse().getData(), Contact.ALL_COLUMNS);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 1, contacts.size());
        assertEquals(contact.getDisplayName(), contacts.get(0).getDisplayName());
    }
    
    
    public void testAutocompleteDistributionList() throws Exception {
        Contact distributionList = new Contact();
        distributionList.setParentFolderID(folderID);
        distributionList.setSurName("LisTe");
        distributionList.setGivenName("VerTeiLer");
        distributionList.setDisplayName(distributionList.getGivenName() + " " + distributionList.getSurName());
        distributionList.setDistributionList(new DistributionListEntryObject[] {
	        new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT),
	        new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT)
        });
        distributionList = manager.newAction(distributionList);
        /*
         * expect in auto-complete response
         */
        AutocompleteRequest request = new AutocompleteRequest(distributionList.getGivenName(), false, Integer.toString(folderID), Contact.ALL_COLUMNS, true);
        CommonSearchResponse response = client.execute(request);
        List<Contact> contacts = manager.transform((JSONArray) response.getResponse().getData(), Contact.ALL_COLUMNS);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 1, contacts.size());
        assertEquals(distributionList.getDisplayName(), contacts.get(0).getDisplayName());
    }

}
