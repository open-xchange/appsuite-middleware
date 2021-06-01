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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;

/**
 *
 * @author tobiasp
 *
 */
public class ManagedSearchTests extends AbstractManagedContactTest {

    public List<String> sinographs = Arrays.asList("\u963f", "\u6ce2", "\u6b21", "\u7684", "\u9e45", "\u5bcc", "\u54e5", "\u6cb3", "\u6d01", "\u79d1", "\u4e86", "\u4e48", "\u5462", "\u54e6", "\u6279", "\u4e03", "\u5982", "\u56db", "\u8e22", "\u5c4b", "\u897f", "\u8863", "\u5b50");

    public ManagedSearchTests() {
        super();
    }

    @Test
    public void testGuiLikeSearch() {
        List<ContactSearchObject> searches = new LinkedList<ContactSearchObject>();

        for (String name : sinographs) {
            //create
            Contact tmp = generateContact();
            tmp.setSurName(name);
            cotm.newAction(tmp);

            //prepare search
            ContactSearchObject search = new ContactSearchObject();
            search.addFolder(folderID);
            search.setGivenName(name);
            search.setSurname(name);
            search.setDisplayName(name);
            search.setEmail1(name);
            search.setEmail2(name);
            search.setEmail3(name);
            search.setCatgories(name);
            search.setYomiFirstname(name);
            search.setYomiLastName(name);
            search.setOrSearch(true);
            searches.add(search);
        }
        for (int i = 0; i < sinographs.size(); i++) {
            Contact[] results = cotm.searchAction(searches.get(i));

            assertEquals("#" + i + " Should find one contact", 1, results.length);
            assertEquals("#" + i + " Should find the right contact", sinographs.get(i), results[0].getSurName());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSearchPattern() {
        for (String name : sinographs) {
            Contact tmp = generateContact();
            tmp.setSurName(name);
            cotm.newAction(tmp);
        }
        Contact[] contacts = cotm.searchAction("*", folderID, ContactField.SUR_NAME.getNumber(), Order.ASCENDING, "gb2312", Contact.ALL_COLUMNS);

        for (int i = 0; i < sinographs.size(); i++) {
            String name = contacts[i].getSurName();
            assertEquals("#" + i + " Should have the right order", sinographs.get(i), name);
        }
    }

}
