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
 *    trademarks of the OX Software GmbH. group of companies.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.java.util.Pair;
import com.openexchange.testing.httpclient.models.ContactData;
import com.openexchange.testing.httpclient.models.ContactResponse;
import com.openexchange.testing.httpclient.models.ContactsResponse;
import com.openexchange.testing.httpclient.models.HaloInvestigationResponse;
import com.openexchange.testing.httpclient.modules.HaloApi;

/**
 * {@link ContactHaloProviderTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public class ContactHaloProviderTest extends ContactProviderTest {

    private HaloApi haloApi;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        haloApi = new HaloApi(getApiClient());
    }

    @Test
    public void testInvestigateByEmail() throws Exception {
        /*
         * lookup contact from test account, the investigate by email1 and check results
         */
        ContactData expecedContact = lookupContactFromAccount();
        HaloInvestigationResponse investigationResponse = haloApi.investigateContactHalo("com.openexchange.halo.contacts", "1,20", expecedContact.getEmail1(), null, null, null, null, null, null, null, null, null);
        checkInvestigateResponse(investigationResponse, expecedContact);
    }

    private ContactData lookupContactFromAccount() throws Exception {
        String folderId = getAccountFolderId();
        List<ContactData> allContacts = getAllContacts(folderId);
        assertTrue("no contacts in account for halo investigation found", null != allContacts && 0 < allContacts.size());
        return allContacts.get(0);
    }

    private void checkInvestigateResponse(HaloInvestigationResponse investigationResponse, ContactData expectedContact) throws Exception {
        ArrayList<ArrayList<Object>> listResponseData = (ArrayList<ArrayList<Object>>) checkResponse(investigationResponse.getError(), investigationResponse.getErrorDesc(), investigationResponse.getData());
        List<ContactData> foundContacts = loadContacts(parseIds(listResponseData, 1, 0));
        ContactData foundContact = findContact(foundContacts, expectedContact.getFolderId(), expectedContact.getId());
        assertNotNull("contact not found", foundContact);
    }

    private static ContactData findContact(List<ContactData> contacts, String folderId, String id) {
        if (null != contacts) {
            for (ContactData contact : contacts) {
                if (Objects.equals(folderId, contact.getFolderId()) && Objects.equals(id, contact.getId())) {
                    return contact;
                }
            }
        }
        return null;
    }

    private List<ContactData> getAllContacts(String folderId) throws Exception {
        ContactsResponse contactResponse = contactsApi.getAllContacts(folderId, "1,20", null, null, null);
        ArrayList<ArrayList<Object>> listResponseData = (ArrayList<ArrayList<Object>>) checkResponse(contactResponse.getError(), contactResponse.getErrorDesc(), contactResponse.getData());
        return loadContacts(parseIds(listResponseData, 1, 0));
    }

    private List<ContactData> loadContacts(List<Pair<String, String>> ids) throws Exception {
        List<ContactData> contacts = new ArrayList<ContactData>();
        for (Pair<String, String> id : ids) {
            ContactResponse getResponse = contactsApi.getContact(id.getSecond(), id.getFirst());
            contacts.add(checkResponse(getResponse.getError(), getResponse.getErrorDesc(), getResponse.getData()));
        }
        return contacts;
    }

    private static List<Pair<String, String>> parseIds(ArrayList<ArrayList<Object>> listResponseData, int folderIdIndex, int objectIdIndex) {
        List<Pair<String, String>> ids = new ArrayList<Pair<String, String>>();
        for (ArrayList<Object> list : listResponseData) {
            if (null != list.get(folderIdIndex) && null != list.get(objectIdIndex)) {
                ids.add(new Pair<String, String>(String.valueOf(list.get(folderIdIndex)), String.valueOf(list.get(objectIdIndex))));
            }
        }
        return ids;
    }

}
