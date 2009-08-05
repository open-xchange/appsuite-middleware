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

package com.openexchange.ajax.contact;

import java.util.Date;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.ContactTestManager;



/**
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ManagedContactTests extends AbstractAJAXSession {

    private ContactTestManager manager;
    private int folderID;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        manager = new ContactTestManager(getClient());
        folderID = getClient().getValues().getPrivateContactFolder();
    }

    @Override
    public void tearDown() throws Exception {
        manager.cleanUp();
        super.tearDown();
    }

    public ManagedContactTests(String name) {
        super(name);
    }
    
    private Contact generateContact(){
        Contact contact = new Contact();
        contact.setSurName("Surname");
        contact.setGivenName("Given name");
        contact.setParentFolderID(folderID);
        return contact;
    }
    
    public void testCreateAndGetContact(){
        Contact expected = generateContact();
        
        manager.newAction(expected);
        
        Contact actual = manager.getAction(folderID, expected.getObjectID());
        
        assertEquals("Surname should match", expected.getSurName(), actual.getSurName());
        assertEquals("Given name should match", expected.getGivenName(), actual.getGivenName());
    }
    
    public void testDeleteContact(){
        Contact expected = generateContact();
        
        manager.newAction(expected);
        
        manager.deleteAction(expected);
        
        try {
            manager.getAction(folderID, expected.getObjectID());
            fail("Should throw assertion exception");
        } catch(AssertionError e) {
            
        }
    }
    
    public void testGetAllContacts(){
        int numberBefore = manager.allAction(folderID).length;
        
        Contact expected = generateContact();
        
        manager.newAction(expected);
        
        Contact[] allContactsOnServer = manager.allAction(folderID);
        
        assertEquals("Should find exactly one more contact", numberBefore+ 1, allContactsOnServer.length);
    }
    
    
    public void testGetAllContactsWithColumns(){
        int numberBefore = manager.allAction(folderID).length;

        Contact expected = generateContact();
        
        manager.newAction(expected);
        
        Contact[] allContactsOnServer = manager.allAction(folderID, new int[]{1,4,5,20});
        
        assertEquals("Should find exactly one more contact", numberBefore+ 1, allContactsOnServer.length);
        
        Contact actual = allContactsOnServer[0];
        assertTrue("Should contain field #1",  actual.contains(1));
        // assertTrue("Should contain field #4",  actual.contains(4));
        //assertTrue("Should contain field #5",  actual.contains(5));
        assertTrue("Should contain field #20", actual.contains(20));
    }
    
    public void testUpdateContactAndGetUpdates(){
        Contact expected = generateContact();
        
        manager.newAction(expected);
        
        Date updateDate = new Date();
        expected.setDisplayName("Display name");
        
        manager.updateAction(expected);
        
        Contact[] updated = manager.updatesAction(folderID, updateDate);
        
        assertEquals("Only one contact should have been updated", 1, updated.length);
        
        Contact actual = updated[0];
        assertEquals("Display name should have been updated", expected.getDisplayName(), actual.getDisplayName());
    }
        
}
