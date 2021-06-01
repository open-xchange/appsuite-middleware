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
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;

public class SortingInJapanTest extends AbstractManagedContactTest {

    private String originalLocale;

    public SortingInJapanTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        originalLocale = getClient().execute(new GetRequest(Tree.Language)).getString();
        if (Strings.isEmpty(originalLocale)) {
            fail("no locale found");
        }
        getClient().execute(new SetRequest(Tree.Language, "ja-JP"));
    }

    @Test
    public void testCustomSortingForJapan() {
        /*
         * generate test contacts on server
         */
        Contact[] orderedContacts = new Contact[] { generateContact("\u30a1"), generateContact("\u30a3"), generateContact("\u30a6"), generateContact("\u30ac"), generateContact("#*+$&& ASCII Art"), generateContact("012345"), generateContact("AAAAA"), generateContact("Hans Dampf"), generateContact("Max Mustermann"),
        };
        List<Contact> unorderedContacts = new ArrayList<Contact>(Arrays.asList(orderedContacts));
        Collections.shuffle(unorderedContacts);
        cotm.newActionMultiple(unorderedContacts.toArray(new Contact[unorderedContacts.size()]));
        /*
         * get all contacts
         */
        Contact[] receivedContacts = cotm.allAction(folderID);
        assertNotNull("no contacts received", receivedContacts);
        assertEquals("wrong number of contacts received", orderedContacts.length, receivedContacts.length);
        /*
         * check sort order
         */
        for (int i = 0; i < receivedContacts.length; i++) {
            assertEquals("contact order wrong", orderedContacts[i].getSurName(), receivedContacts[i].getSurName());
        }
    }

}
