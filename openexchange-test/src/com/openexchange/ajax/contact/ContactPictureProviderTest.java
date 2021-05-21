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

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ContactsResponse;

/**
 * {@link ContactPictureProviderTest} - Tests retrieving contact pictures for a different contact provider (i.e com.openexchange.contact.provider.test)
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 */
public class ContactPictureProviderTest extends ContactProviderTest {

    private static final String CONTACT_COLUMNS = "1,501,502,555,606";

    static class TestContact {

        String id;
        String folderId;
        String givenName;
        String surName;
        String email1;
        String image1_url;
    }

    /**
     * Internal method to get the contacts within a given contact folder
     *
     * @param folderId The ID of the folder to get the contacts from
     * @return A list of contacts retrieved from the folder
     * @throws ApiException
     */
    private List<TestContact> getContactsInFolder(String folderId) throws ApiException {
        ContactsResponse contactResponse = contactsApi.getAllContacts(folderId, CONTACT_COLUMNS, null, null, null);
        ArrayList<ArrayList<Object>> contacts = (ArrayList<ArrayList<Object>>) checkResponse(contactResponse.getError(), contactResponse.getErrorDesc(), contactResponse.getData());
        return contacts.stream().map(c -> {
            TestContact contact = new TestContact();
            contact.folderId = folderId;
            contact.id = (String) c.get(0);
            contact.givenName = (String) c.get(1);
            contact.surName = (String) c.get(2);
            contact.email1 = (String) c.get((3));
            contact.image1_url = (String) c.get((4));
            return contact;
        }).collect(Collectors.toList());
    }

    /**
     * Reduces the given list of contacts to only those contacts who have contact pictures assigned
     *
     * @param contacts A list of contacts
     * @return A sub set of the given list with only those contacts who have a contact picture assigned
     */
    private List<TestContact> getContactsWithImages(List<TestContact> contacts) {
        return contacts.stream().filter(c -> Strings.isNotEmpty(c.image1_url)).collect(Collectors.toList());
    }

    /**
     * Asserts that it is possible to retrieve a contact picture for every contact in the given list by ID
     *
     * @param contactsWithImages The list of contacts to get the images for
     * @throws ApiException
     */
    private void assertGetPictureById(List<TestContact> contactsWithImages) throws ApiException {
        for (TestContact c : contactsWithImages) {
            byte[] contactPicture = contactsApi.getContactPicture(null, null, c.id, c.folderId, null, null, null, null, null, null, null, null, null, null);
            assertThat("The contact picture must be present", contactPicture, notNullValue());
            assertThat("The contact picture must not be empty", I(contactPicture.length), greaterThan(I(0)));
        }
    }

    /**
     * Asserts that it is possible to retrieve a contact picture for every contact in the given list by email
     *
     * @param contactsWithImages The list of contacts to get the images for
     * @throws ApiException
     */
    private void assertGetPictureByEmail(List<TestContact> contactsWithImages) throws ApiException {
        for (TestContact c : contactsWithImages) {
            byte[] contactPicture = contactsApi.getContactPicture(null, null, null, null, c.email1, null, null, null, null, null, null, null, null, null);
            assertThat("The contact picture must be present", contactPicture, notNullValue());
            assertThat("The contact picture must not be empty", I(contactPicture.length), greaterThan(I(0)));
        }
    }

    @Test
    public void testGetImageById() throws Exception {
        String folderId = getAccountFolderId();
        List<TestContact> contactsWithImages = getContactsWithImages(getContactsInFolder(folderId));
        assertGetPictureById(contactsWithImages);
    }

    @Test
    public void testGetImageByEmail() throws Exception {
        String folderId = getAccountFolderId();
        List<TestContact> contactsWithImages = getContactsWithImages(getContactsInFolder(folderId));
        assertGetPictureByEmail(contactsWithImages);
    }
}
