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

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.contact.Data;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link UpdateTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpdateTest extends ContactStorageTest {

    @Test
    public void testUpdateSimple() throws Exception {
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
        final Contact savedContact = super.findContact(contact.getUid(), folderId);
        assertNotNull("contact not found", savedContact);
        assertEquals("display name wrong", contact.getDisplayName(), savedContact.getDisplayName());
        assertEquals("surname wrong", contact.getSurName(), savedContact.getSurName());
        assertEquals("givenname wrong", contact.getGivenName(), savedContact.getGivenName());
        assertEquals("email1 wrong", contact.getEmail1(), savedContact.getEmail1());
        Date clientLastModified = savedContact.getLastModified();
        /*
         * update contact
         */
        savedContact.setDisplayName("Otto2 Maier2");
        savedContact.setGivenName("Otto2");
        savedContact.setSurName("Maier2");
        savedContact.setEmail1("otto2.maier2@example.com");
        String objectID = Integer.toString(savedContact.getObjectID());
        getStorage().update(getSession(), folderId, objectID, savedContact, clientLastModified);
        super.rememberForCleanUp(contact);
        /*
         * verify updated contact
         */
        final Contact updatedContact = super.findContact(savedContact.getUid(), folderId, clientLastModified);
        assertNotNull("contact not found", updatedContact);
        assertEquals("display name wrong", updatedContact.getDisplayName(), savedContact.getDisplayName());
        assertEquals("surname wrong", updatedContact.getSurName(), savedContact.getSurName());
        assertEquals("givenname wrong", updatedContact.getGivenName(), savedContact.getGivenName());
        assertEquals("email1 wrong", updatedContact.getEmail1(), savedContact.getEmail1());
    }

    @Test
    public void testUpdateImage() throws Exception {
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
        Date clientLastModified = savedContact.getLastModified();
        /*
         * update contact
         */
        Arrays.sort(savedContact.getImage1());
        final String objectID = Integer.toString(savedContact.getObjectID());
        getStorage().update(getSession(), folderId, objectID, savedContact, clientLastModified);
        super.rememberForCleanUp(contact);
        /*
         * verify updated contact
         */
        final Contact updatedContact = super.findContact(savedContact.getUid(), folderId, clientLastModified);
        assertNotNull("contact not found", updatedContact);
        assertNotNull("no image found", updatedContact.getImage1());
        assertEquals("number of images wrong", 1, updatedContact.getNumberOfImages());
        assertTrue("image wrong", Arrays.equals(savedContact.getImage1(), updatedContact.getImage1()));
        assertEquals("image content type wrong", savedContact.getImageContentType(), updatedContact.getImageContentType());
    }

    public void testUpdateDistList() throws Exception {
        /*
         * create contact
         */
        final String folderId = "500003";
        final Contact contact = new Contact();
        contact.setCreatedBy(getUserID());
        contact.setSurName("Distributionlist 52");
        contact.setUid(UUID.randomUUID().toString());
        contact.setDistributionList(new DistributionListEntryObject[] {
            new DistributionListEntryObject("Horst Hund", "horst.hund@example.com", 0),
            new DistributionListEntryObject("Werner Hund", "werner.hund@example.com", 0),
            new DistributionListEntryObject("Dieter Hund", "dieter.hund@example.com", 0),
            new DistributionListEntryObject("Klaus Hund", "klaus.hund@example.com", 0),
            new DistributionListEntryObject("Kurt Hund", "kurt.hund@example.com", 0),
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
        Date clientLastModified = savedContact.getLastModified();
        /*
         * update contact
         */
        savedContact.setDistributionList(new DistributionListEntryObject[] {
                new DistributionListEntryObject("Horst Klotz", "horst.klotz@example.com", 0),
                new DistributionListEntryObject("Werner Klotz", "werner.klotz@example.com", 0),
                new DistributionListEntryObject("Klaus Klotz", "klaus.klotz@example.com", 0),
                new DistributionListEntryObject("Kurt Klotz", "kurt.klotz@example.com", 0),
            });
        final String objectID = Integer.toString(savedContact.getObjectID());
        getStorage().update(getSession(), folderId, objectID, savedContact, clientLastModified);
        super.rememberForCleanUp(contact);
        /*
         * verify updated contact
         */
        Contact updatedContact = super.findContact(savedContact.getUid(), folderId, clientLastModified);
        assertNotNull("contact not found", updatedContact);
        assertTrue("not marked as distribution list", updatedContact.getMarkAsDistribtuionlist());
        assertNotNull("distribution list not found", updatedContact.getDistributionList());
        assertEquals("number of distribution list members wrong", 4, updatedContact.getNumberOfDistributionLists());
        assertEquals("number of distribution list members wrong", 4, updatedContact.getDistributionList().length);
        assertTrue("distribution list wrong", Arrays.equals(savedContact.getDistributionList(), updatedContact.getDistributionList()));
    }

    public void testAddImage() throws Exception {
        /*
         * create contact
         */
        final String folderId = "500007";
        final Contact contact = new Contact();
        contact.setCreatedBy(getUserID());
        contact.setDisplayName("Bernd Bein");
        contact.setGivenName("Bernd");
        contact.setSurName("Bein");
        contact.setEmail1("bernd.bein@example.com");
        contact.setUid(UUID.randomUUID().toString());
        getStorage().create(getSession(), folderId, contact);
        super.rememberForCleanUp(contact);
        /*
         * verify contact
         */
        final Contact savedContact = super.findContact(contact.getUid(), folderId);
        assertNotNull("contact not found", savedContact);
        assertEquals("display name wrong", contact.getDisplayName(), savedContact.getDisplayName());
        assertEquals("surname wrong", contact.getSurName(), savedContact.getSurName());
        assertEquals("givenname wrong", contact.getGivenName(), savedContact.getGivenName());
        assertEquals("email1 wrong", contact.getEmail1(), savedContact.getEmail1());
        Date clientLastModified = savedContact.getLastModified();
        /*
         * update contact
         */
        savedContact.setImage1(Data.image);
        savedContact.setImageContentType(Data.CONTENT_TYPE);
        final String objectID = Integer.toString(savedContact.getObjectID());
        getStorage().update(getSession(), folderId, objectID, savedContact, clientLastModified);
        super.rememberForCleanUp(contact);
        /*
         * verify updated contact
         */
        Contact updatedContact = super.findContact(contact.getUid(), folderId, clientLastModified);
        assertNotNull("contact not found", updatedContact);
        assertNotNull("no image found", updatedContact.getImage1());
        assertEquals("number of images wrong", 1, updatedContact.getNumberOfImages());
        assertTrue("image wrong", Arrays.equals(savedContact.getImage1(), updatedContact.getImage1()));
        assertEquals("image content type wrong", savedContact.getImageContentType(), updatedContact.getImageContentType());
    }

    public void testRemoveImage() throws Exception {
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
        Date clientLastModified = savedContact.getLastModified();
        /*
         * update contact
         */
        savedContact.setImage1(null);
        savedContact.setImageContentType(null);
        savedContact.setNumberOfImages(0);
        final String objectID = Integer.toString(savedContact.getObjectID());
        getStorage().update(getSession(), folderId, objectID, savedContact, clientLastModified);
        super.rememberForCleanUp(contact);
        /*
         * verify updated contact
         */
        Contact updatedContact = super.findContact(contact.getUid(), folderId, clientLastModified);
        assertNotNull("contact not found", updatedContact);
        assertNull("image still found", updatedContact.getImage1());
        assertEquals("number of images wrong", 0, updatedContact.getNumberOfImages());
        assertNull("image content type wrong", updatedContact.getImageContentType());
    }

}
