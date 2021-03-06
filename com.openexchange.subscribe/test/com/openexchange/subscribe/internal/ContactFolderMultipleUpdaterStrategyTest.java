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

package com.openexchange.subscribe.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;


/**
 * {@link ContactFolderMultipleUpdaterStrategyTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactFolderMultipleUpdaterStrategyTest {

    private FolderUpdaterStrategy<Contact> strategy;

    private HashMap <Integer, Object> session;

    @Before
    public void setUp() {
        this.strategy = new ContactFolderMultipleUpdaterStrategy();
        this.session = new HashMap<Integer,Object>();
    }

     @Test
     public void testHandles() {
        FolderObject contactFolder = new FolderObject();
        contactFolder.setModule(FolderObject.CONTACT);

        FolderObject infostoreFolder = new FolderObject();
        infostoreFolder.setModule(FolderObject.INFOSTORE);

        assertTrue("Should handle contact folders", strategy.handles(contactFolder));
        assertFalse("Should not handle infostore folders", strategy.handles(infostoreFolder));
    }

     @Test
     public void testScoring() throws OXException {
        // First name is not enough
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setUserField20(UUID.randomUUID().toString());

        Contact contact2 = new Contact();
        contact2.setGivenName("Hans");
        contact2.setSurName("Wurst");
        contact2.setUserField20(UUID.randomUUID().toString());

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

     @Test
     public void testTwoCompaniesDiffer() throws OXException {
        Contact contact = new Contact();
        contact.setGivenName("");
        contact.setSurName("");
        contact.setUserField20(UUID.randomUUID().toString());

        contact.setCompany("Wunderwerk GmbH");

        Contact contact2 = new Contact();
        contact2.setGivenName("");
        contact2.setSurName("");
        contact2.setCompany("Schokoladenfabrik Inc.");
        contact2.setUserField20(UUID.randomUUID().toString());

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("Empty names shouldn't be considered equal.", score < strategy.getThreshold(session));
    }

     @Test
     public void testNameChangedButMailAdressStayedTheSame() throws OXException {
        // First name is not enough
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");
        contact.setUserField20(UUID.randomUUID().toString());

        Contact contact2 = new Contact();
        contact2.setGivenName("Hans");
        contact2.setSurName("Wurst");
        contact2.setEmail1("hans@example.com");
        contact2.setUserField20(UUID.randomUUID().toString());

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("First name and email address should suffice", score >= strategy.getThreshold(session));
    }

     @Test
     public void testWithoutUUIDNoMagicWillHappen() throws OXException {
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");
        contact.setUserField20(UUID.randomUUID().toString());

        Contact contact2 = new Contact();
        contact2.setGivenName("Peter");
        contact2.setSurName("Schmitt");
        contact2.setEmail2("peter@example.com");

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("These two contacts should not score higher than the treshhold", score < strategy.getThreshold(contact2));
    }

     @Test
     public void testSecondContactHasUUIDButIsNotOnThisSystem() throws OXException {
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");
        contact.setUserField20(UUID.randomUUID().toString());

        Contact contact2 = new Contact();
        contact2.setGivenName("Peter");
        contact2.setSurName("Schmitt");
        contact2.setEmail2("peter@example.com");
        contact2.setUserField20(UUID.randomUUID().toString());

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("These two contacts should not score higher than the treshhold", score < strategy.getThreshold(contact2));
    }

     @Test
     public void testTwoContactsAreSimilarButWillNotBeAssociatedBecauseOneIsNotOnTheSystem() throws OXException {
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");
        contact.setUserField20(UUID.randomUUID().toString());

        Contact contact2 = new Contact();
        contact2.setGivenName("Hans");
        contact2.setSurName("Dampf");
        contact2.setEmail2("hd@privat.com");
        contact2.setUserField20(UUID.randomUUID().toString());

        int score = strategy.calculateSimilarityScore(contact, contact2, session);

        assertTrue("These two contacts are similar and should be merged", score >= strategy.getThreshold(contact2));
    }

     @Test
     public void testCalculateSimilarityScore_mobileEqual_increaseSimilarityScore() throws OXException {
        // First name is not enough
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setCellularTelephone1("0000-0000000");

        Contact candidate = new Contact();
        candidate.setGivenName("Hans");
        candidate.setEmail1("hans@example.com");
        candidate.setCellularTelephone1("0000-0000000");

        int score = strategy.calculateSimilarityScore(contact, candidate, session);

        assertTrue("Score to low. CellularTelephone is equal.", score >= strategy.getThreshold(session));
    }

     @Test
     public void testCalculateSimilarityScore_mobileDifferent_smallScore() throws OXException {
        // First name is not enough
        Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setCellularTelephone1("0000-0000000");

        Contact candidate = new Contact();
        candidate.setGivenName("Hans");
        candidate.setEmail1("hans@example.com");
        candidate.setCellularTelephone1("1111-1111111");

        int score = strategy.calculateSimilarityScore(contact, candidate, session);

        assertTrue("Score to high. Only GivenName is equal.", score < strategy.getThreshold(session));
    }
}
