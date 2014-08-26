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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

}
