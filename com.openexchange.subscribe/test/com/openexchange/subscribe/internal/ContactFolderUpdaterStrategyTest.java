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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import junit.framework.TestCase;


/**
 * {@link ContactFolderUpdaterStrategyTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactFolderUpdaterStrategyTest extends TestCase {
    
    private FolderUpdaterStrategy<ContactObject> strategy;
    
    public void setUp() {
        this.strategy = new ContactFolderUpdaterStrategy();
    }
    
    public void testHandles() {
        FolderObject contactFolder = new FolderObject();
        contactFolder.setModule(FolderObject.CONTACT);
        
        FolderObject infostoreFolder = new FolderObject();
        infostoreFolder.setModule(FolderObject.INFOSTORE);
        
        assertTrue("Should handle contact folders", strategy.handles(contactFolder));
        assertFalse("Should not handle infostore folders", strategy.handles(infostoreFolder));
    }
    
    public void testScoring() throws AbstractOXException {
        // First name is not enough
        ContactObject contact = new ContactObject();
        contact.setGivenName("Hans");
        contact.setSurName("Dampf");
        
        ContactObject contact2 = new ContactObject();
        contact2.setGivenName("Hans");
        contact2.setSurName("Wurst");

        int score = strategy.calculateSimilarityScore(contact, contact2, null);
        
        assertTrue("First name should not be enough", score < strategy.getThreshhold(null));

        // First Name and Last Name is enough
        contact2.setSurName("Dampf");
        
        score = strategy.calculateSimilarityScore(contact, contact2, null);
        assertTrue("First name and last name is not enough", score > strategy.getThreshhold(null));
        
        // Prefer first name, last name and birth date
        contact.setBirthday(new Date(2));
        contact2.setBirthday(new Date(2));
        
        int newScore = strategy.calculateSimilarityScore(contact, contact2, null);
        assertTrue("Similarity score for matching birthdays should be bigger", newScore > score);
        score = newScore;
        
        // To discuss: Email Addresses
    }
    
}
