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

/**
 * {@link Bug32635Test}
 *
 * auto complete broken for contacts
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug32635Test extends AbstractManagedContactTest {

    /**
     * Initializes a new {@link Bug32635Test}.
     *
     * @param name The test name
     */
    public Bug32635Test() {
        super();
    }

    @Test
    public void testAutocomplete() throws Exception {
        /*
         * create contact
         */
        Contact contact = super.generateContact("Preu\u00df");
        contact.setGivenName("Stefan");
        contact.setDisplayName("Preu\u00df, Stefan");
        contact = cotm.newAction(contact);
        /*
         * check auto-complete
         */
        String parentFolderID = String.valueOf(contact.getParentFolderID());
        AutocompleteRequest request = new AutocompleteRequest("Stefan Preu\u00df", false, parentFolderID, Contact.ALL_COLUMNS, true);
        CommonSearchResponse response = getClient().execute(request);
        List<Contact> contacts = cotm.transform((JSONArray) response.getResponse().getData(), Contact.ALL_COLUMNS);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 1, contacts.size());
        assertEquals(contact.getDisplayName(), contacts.get(0).getDisplayName());
    }

}
