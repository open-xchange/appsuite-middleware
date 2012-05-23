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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.subscribe.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import junit.framework.TestCase;
import com.openexchange.api2.SimContactSQLImpl;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactUnificationState;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;


/**
 * {@link ContactFolderMultipleUpdaterStrategyTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactFolderMultipleUpdaterStrategyTest extends TestCase {

    private FolderUpdaterStrategy<Contact> strategy;

    private HashMap <Integer, Object> session;

    private SimContactSQLImpl contactStore = new SimContactSQLImpl();

    @Override
    public void setUp() {
        this.strategy = new ContactFolderMultipleUpdaterStrategy();
        this.session = new HashMap<Integer,Object>();
        this.session.put(Integer.valueOf(1), contactStore);
    }

    public void testHandles() {
        FolderObject contactFolder = new FolderObject();
        contactFolder.setModule(FolderObject.CONTACT);

        FolderObject infostoreFolder = new FolderObject();
        infostoreFolder.setModule(FolderObject.INFOSTORE);

        assertTrue("Should handle contact folders", strategy.handles(contactFolder));
        assertFalse("Should not handle infostore folders", strategy.handles(infostoreFolder));
    }

    public void testScoring() throws OXException {
        // First name is not enough
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact);

        Contact contact2 = new Contact();
        contact2.setGivenName("Hans");
        contact2.setSurName("Wurst");
        contact2.setUserField20(UUID.randomUUID().toString());

        contactStore.addContact(contact2);


        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("First name should not be enough", score < strategy.getThreshold(session));

        // First Name and Last Name is enough
        contact2.setSurName("Dampf");

        score = strategy.calculateSimilarityScore(contact, contact2, session);
        assertTrue("First name and last name is enough", score > strategy.getThreshold(session));

        // Prefer first name, last name and birth date
        contact.setBirthday(new Date(2));
        contact2.setBirthday(new Date(2));

        int newScore = strategy.calculateSimilarityScore(contact, contact2, session);
        assertTrue("Similarity score for matching birthdays should be bigger", newScore > score);

    }

    public void testTwoCompaniesDiffer() throws OXException {
        Contact contact = new Contact();
        contact.setGivenName("");
        contact.setSurName("");
        contact.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact);

        contact.setCompany("Wunderwerk GmbH");

        Contact contact2 = new Contact();
        contact2.setGivenName("");
        contact2.setSurName("");
        contact2.setCompany("Schokoladenfabrik Inc.");
        contact2.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact2);

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("Empty names shouldn't be considered equal.", score < strategy.getThreshold(session));
    }

    public void testNameChangedButMailAdressStayedTheSame() throws OXException {
        // First name is not enough
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");
        contact.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact);

        Contact contact2 = new Contact();
        contact2.setGivenName("Hans");
        contact2.setSurName("Wurst");
        contact2.setEmail1("hans@example.com");
        contact2.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact2);

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("First name and email address should suffice", score >= strategy.getThreshold(session));


    }

    public void notestTwoSimilarContactsWillGetAssociated() throws OXException {
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");
        contact.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact);

        Contact contact2 = new Contact();
        contact2.setGivenName("Hans");
        contact2.setSurName("Wurst");
        contact2.setEmail1("hans@example.com");
        contact2.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact2);

        strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("These two contacts should have been associated", contactStore.getAssociationBetween(contact, contact2).equals(ContactUnificationState.GREEN));
    }

    public void notestTwoSeparatedContactsWillNotBeMergedHoweverSimilarTheyAre() throws OXException {
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");
        contact.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact);

        Contact contact2 = new Contact();
        contact2.setGivenName("Hans");
        contact2.setSurName("Dampf");
        contact2.setEmail1("hans@example.com");
        contact2.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact2);

        contactStore.separateTwoContacts(contact, contact2);

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("These contacts were separated and should not score so high", score <= strategy.getThreshold(session));

    }

    public void notestTwoAssociatedContactsWillBeMergedRegardlessOfContent() throws OXException {
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");
        UUID uuid = UUID.randomUUID();
        contact.setUserField20(uuid.toString());
        contactStore.addContact(contact);

        Contact contact2 = new Contact();
        contact2.setGivenName("Peter");
        contact2.setSurName("Schmitt");
        contact2.setEmail2("peter@example.com");
        contact2.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact2);

        contactStore.associateTwoContacts(contact, contact2);

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("These contacts are associated and should score high regardless of similarity", score >= strategy.getThreshold(contact2));

        strategy.update(contact, contact2, session);
        Contact updatedContact = contactStore.getContactByUUID(uuid);

        assertTrue("These two contacts should have been merged, the first contact now containing the new alternative email-address", updatedContact.getEmail2().equals("peter@example.com"));
        assertTrue("These two contacts should have been merged, but the given name has to be kept because it was already filled", updatedContact.getGivenName().equals("Hans"));
        assertTrue("These two contacts should have been merged, but the last name has to be kept because it was already filled", updatedContact.getSurName().equals("Dampf"));
        assertTrue("These two contacts should have been merged, but the first email-address has to be kept because it was already filled", updatedContact.getEmail1().equals("hans@example.com"));
    }

    public void testWithoutUUIDNoMagicWillHappen() throws OXException {
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");
        contact.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact);

        Contact contact2 = new Contact();
        contact2.setGivenName("Peter");
        contact2.setSurName("Schmitt");
        contact2.setEmail2("peter@example.com");

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("These two contacts should not score higher than the treshhold", score < strategy.getThreshold(contact2));
    }

    public void testSecondContactHasUUIDButIsNotOnThisSystem() throws OXException {
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");
        contact.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact);

        Contact contact2 = new Contact();
        contact2.setGivenName("Peter");
        contact2.setSurName("Schmitt");
        contact2.setEmail2("peter@example.com");
        contact2.setUserField20(UUID.randomUUID().toString());

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("These two contacts should not score higher than the treshhold", score < strategy.getThreshold(contact2));
        assertTrue("These two contacts should not be associated as the second one is not even on this system. Trying to associate them would only produce errors", contactStore.getAssociationBetween(contact, contact2).equals(ContactUnificationState.UNDEFINED));
    }

    public void testTwoContactsAreSimilarButWillNotBeAssociatedBecauseOneIsNotOnTheSystem() throws OXException {
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");
        contact.setUserField20(UUID.randomUUID().toString());
        contactStore.addContact(contact);

        Contact contact2 = new Contact();
        contact2.setGivenName("Hans");
        contact2.setSurName("Dampf");
        contact2.setEmail2("hd@privat.com");
        contact2.setUserField20(UUID.randomUUID().toString());

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("These two contacts are similar and should be merged", score >= strategy.getThreshold(contact2));
        assertTrue("These two contacts should not be associated as the second one is not even on this system. Trying to associate them would only produce errors", contactStore.getAssociationBetween(contact, contact2).equals(ContactUnificationState.UNDEFINED));
    }
}
