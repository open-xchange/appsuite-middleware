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

package com.openexchange.contact.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.contact.Data;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link CreateTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CreateTest extends ContactStorageTest {

    @Test
    public void testCreateSimple() throws Exception {
        /*
         * create contact
         */
        final String folderId = "500002";
        final Contact contact = new Contact();
        contact.setCreatedBy(getUserID());
        contact.setDisplayName("Otto Maier");
        contact.setGivenName("Otto");
        contact.setSurName("Maier");
        contact.setEmail1("otto.maier@example.com");
        contact.setUid(UUID.randomUUID().toString());
        getStorage().create(getSession(), folderId, contact);
        super.rememberForCleanUp(contact);
        /*
         * verify contact
         */
        Contact savedContact = super.findContact(contact.getUid(), folderId);
        assertNotNull("contact not found", savedContact);
        assertEquals("display name wrong", contact.getDisplayName(), savedContact.getDisplayName());
        assertEquals("surname wrong", contact.getSurName(), savedContact.getSurName());
        assertEquals("givenname wrong", contact.getGivenName(), savedContact.getGivenName());
        assertEquals("email1 wrong", contact.getEmail1(), savedContact.getEmail1());
    }

    @Test
    public void testCreateWithImage() throws Exception {
        /*
         * create contact
         */
        final String folderId = "500004";
        final Contact contact = new Contact();
        contact.setCreatedBy(getUserID());
        contact.setDisplayName("Dirk Dampf");
        contact.setGivenName("Dirk");
        contact.setSurName("Dampf");
        contact.setEmail1("dirk.dampf@example.com");
        contact.setUid(UUID.randomUUID().toString());
        contact.setImage1(Data.image);
        contact.setImageContentType(Data.CONTENT_TYPE);
        getStorage().create(getSession(), folderId, contact);
        super.rememberForCleanUp(contact);
        /*
         * verify contact
         */
        Contact savedContact = super.findContact(contact.getUid(), folderId);
        assertNotNull("contact not found", savedContact);
        assertNotNull("no image found", savedContact.getImage1());
        assertEquals("number of images wrong", 1, savedContact.getNumberOfImages());
        assertTrue("image wrong", Arrays.equals(contact.getImage1(), savedContact.getImage1()));
        assertEquals("image content type wrong", contact.getImageContentType(), savedContact.getImageContentType());
    }

    @Test
    public void testCreateDistList() throws Exception {
        /*
         * create contact
         */
        final String folderId = "500003";
        final Contact contact = new Contact();
        contact.setCreatedBy(getUserID());
        contact.setSurName("Distributionlist 77");
        contact.setUid(UUID.randomUUID().toString());
        contact.setDistributionList(new DistributionListEntryObject[] {
            new DistributionListEntryObject("Horst Otto", "horst.otto@example.com", 0),
            new DistributionListEntryObject("Werner Otto", "werner.otto@example.com", 0),
            new DistributionListEntryObject("Dieter Otto", "dieter.otto@example.com", 0),
            new DistributionListEntryObject("Klaus Otto", "klaus.otto@example.com", 0),
            new DistributionListEntryObject("Kurt Otto", "kurt.otto@example.com", 0),
        });
        getStorage().create(getSession(), folderId, contact);
        super.rememberForCleanUp(contact);
        /*
         * verify contact
         */
        Contact savedContact = super.findContact(contact.getUid(), folderId);
        assertNotNull("contact not found", savedContact);
        assertTrue("not marked as distribution list", savedContact.getMarkAsDistribtuionlist());
        assertNotNull("distribution list not found", savedContact.getDistributionList());
        assertEquals("number of distribution list members wrong", 5, savedContact.getNumberOfDistributionLists());
        assertEquals("number of distribution list members wrong", 5, savedContact.getDistributionList().length);
        assertTrue("distribution list wrong", Arrays.equals(contact.getDistributionList(), savedContact.getDistributionList()));
    }

    @Test
    public void testCreateSpecialChars() throws Exception {
        /*
         * create contact
         */
        final String folderId = "500001";
        final Contact contact = new Contact();
        contact.setCreatedBy(getUserID());
        contact.setDisplayName("Ren\u00e9 M\u00fcller");
        contact.setGivenName("Ren\u00e9");
        contact.setSurName("M\u00fcller");
        contact.setEmail1("rene.mueller@example.com");
        contact.setUid(UUID.randomUUID().toString());
        getStorage().create(getSession(), folderId, contact);
        super.rememberForCleanUp(contact);
        /*
         * verify contact
         */
        Contact savedContact = super.findContact(contact.getUid(), folderId);
        assertNotNull("contact not found", savedContact);
        assertEquals("display name wrong", contact.getDisplayName(), savedContact.getDisplayName());
        assertEquals("surname wrong", contact.getSurName(), savedContact.getSurName());
        assertEquals("givenname wrong", contact.getGivenName(), savedContact.getGivenName());
        assertEquals("email1 wrong", contact.getEmail1(), savedContact.getEmail1());
    }

    @Test
    public void testCreateMany() throws Exception {
        /*
         * create contacts
         */
        final Map<String, List<Contact>> contactsInFolders = new HashMap<String, List<Contact>>();
        for (int i = 500004; i <= 500005; i++) {
            contactsInFolders.put(Integer.toString(i), new ArrayList<Contact>());
            for (int j = 1; j <= 33; j++) {
                final Contact contact = new Contact();
                contact.setCreatedBy(getUserID());
                contact.setDisplayName("Kontakt_" + i + " Test_" + j);
                contact.setGivenName("Kontakt_" + i);
                contact.setSurName("Test_" + j);
                contact.setEmail1("kontakt" + i + ".test" + j + "@example.com");
                contact.setUid(UUID.randomUUID().toString());
                getStorage().create(getSession(), Integer.toString(i), contact);
                contactsInFolders.get(Integer.toString(i)).add(contact);
                super.rememberForCleanUp(contact);
            }
        }
        /*
         * verify contacts
         */
        for (final Map.Entry<String, List<Contact>> entry : contactsInFolders.entrySet()) {
            String folderId = entry.getKey();
            for (final Contact contact : entry.getValue()) {
                final Contact savedContact = getStorage().get(getSession(), folderId, Integer.toString(contact.getObjectID()), ContactField.values());
                assertNotNull("contact not found", savedContact);
                assertEquals("uid wrong", contact.getUid(), savedContact.getUid());
            }
        }
    }

}
