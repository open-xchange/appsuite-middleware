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

package com.openexchange.subscribe.internal;

import java.util.Date;
import org.junit.Test;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;


/**
 * {@link ContactFolderUpdaterStrategyTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactFolderUpdaterStrategyTest extends TestCase {

    private FolderUpdaterStrategy<Contact> strategy;

    @Override
    public void setUp() {
        this.strategy = new ContactFolderUpdaterStrategy();
    }

    public void testHandles() {
        final FolderObject contactFolder = new FolderObject();
        contactFolder.setModule(FolderObject.CONTACT);

        final FolderObject infostoreFolder = new FolderObject();
        infostoreFolder.setModule(FolderObject.INFOSTORE);

        assertTrue("Should handle contact folders", strategy.handles(contactFolder));
        assertFalse("Should not handle infostore folders", strategy.handles(infostoreFolder));
    }

    public void testScoring() throws OXException {
        // First name is not enough
        final Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");

        final Contact contact2 = new Contact();
        contact2.setGivenName("Hans");
        contact2.setSurName("Wurst");

        int score = strategy.calculateSimilarityScore(contact, contact2, null);

        assertTrue("First name should not be enough", score < strategy.getThreshold(null));

        // First Name and Last Name is enough
        contact2.setSurName("Dampf");

        score = strategy.calculateSimilarityScore(contact, contact2, null);
        assertTrue("First name and last name is not enough", score > strategy.getThreshold(null));

        // Prefer first name, last name and birth date
        contact.setBirthday(new Date(2));
        contact2.setBirthday(new Date(2));

        final int newScore = strategy.calculateSimilarityScore(contact, contact2, null);
        assertTrue("Similarity score for matching birthdays should be bigger", newScore > score);

    }

    public void testTwoCompaniesDiffer() throws OXException {
        final Contact contact = new Contact();
        contact.setGivenName("");
        contact.setSurName("");
        contact.setCompany("Wunderwerk GmbH");

        final Contact contact2 = new Contact();
        contact2.setGivenName("");
        contact2.setSurName("");
        contact2.setCompany("Schokoladenfabrik Inc.");

        final int score = strategy.calculateSimilarityScore(contact, contact2, null);

        assertTrue("Empty names shouldn't be considered equal.", score < strategy.getThreshold(null));
    }

    public void testNameChangedButMailAdressStayedTheSame() throws OXException {
        // First name is not enough
        final Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        contact.setEmail1("hans@example.com");

        final Contact contact2 = new Contact();
        contact2.setGivenName("Hans");
        contact2.setSurName("Wurst");
        contact2.setEmail1("hans@example.com");

        final int score = strategy.calculateSimilarityScore(contact, contact2, null);

        assertTrue("First name and email address should suffice", score >= strategy.getThreshold(null));
    }

    public void testNullValuesShouldNotChangeResult() throws OXException {
        final Contact contact = new Contact();
        contact.setGivenName("Hans");
        contact.setEmail1(null);

        final Contact contact2 = new Contact();
        contact2.setGivenName("Hans");
        contact2.setEmail1(null);

        final int score = strategy.calculateSimilarityScore(contact, contact2, null);

        assertTrue("Two objects with similar content should match", score > strategy.getThreshold(null));
    }


    public void testTwoEmptyContactsAreTheSame() throws OXException {
        final Contact contact = new Contact();
        final Contact contact2 = new Contact();
        final int score = strategy.calculateSimilarityScore(contact, contact2, null);

        assertTrue("Two completely empty objects should match, too", score > strategy.getThreshold(null));
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

        int score = strategy.calculateSimilarityScore(contact, candidate, null);

        assertTrue("Score to low. CellularTelephone is equal.", score >= strategy.getThreshold(null));
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

        int score = strategy.calculateSimilarityScore(contact, candidate, null);

        assertTrue("Score to high. Only GivenName is equal.", score < strategy.getThreshold(null));
    }

}
