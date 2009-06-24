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

package com.openexchange.ajax.contact.action;

import java.util.Calendar;
import java.util.Date;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.ContactTestManager;


/**
 * This contains some examples of tests created for ContactTestManager
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
 */
public class ExemplaryContactTestManagerTest extends AbstractAJAXSession {

	private ContactTestManager manager;
	private AJAXClient client; 
	Contact contactObject1;
	Contact contactObject2;
	
	public ExemplaryContactTestManagerTest(final String name) {
		super(name);
	}
	
	@Override
    public void setUp() throws Exception {
		super.setUp();
		client = getClient();
		manager = new ContactTestManager(getClient());
        //create a contact in the private folder
        contactObject1 = new Contact();
        contactObject1.setDisplayName("Herbert Meier");
        contactObject1.setEmail1("herbert.meier@example.com");
        contactObject1.setNote("created by ExemplaryContactTestManagerTest");
        contactObject1.setParentFolderID(client.getValues().getPrivateContactFolder());
        manager.insertContactOnServer(contactObject1);
        //create a second contact in the private folder
        contactObject2 = new Contact();
        contactObject2.setDisplayName("Herbert M\u00fcller");
        contactObject2.setEmail1("herbert.mueller@example.com");
        contactObject2.setParentFolderID(client.getValues().getPrivateContactFolder());
        contactObject2.setNote("created by ExemplaryContactTestManagerTest");
        manager.insertContactOnServer(contactObject2);
	}
	
	public void tearDown() throws Exception {
		manager.cleanUp();
	}
	
	public void testCreatedContactsAreReturnedByGetRequest () throws Exception {
		Contact co = manager.getContactFromServer(contactObject1.getParentFolderID(), contactObject1.getObjectID());
		assertEquals("The contact was not returned.", co.getDisplayName(), contactObject1.getDisplayName());
	}
	
	public void testCreatedContactsAppearInAllRequestForSameFolder () throws Exception {
		boolean found1 = false;
		boolean found2 = false;
		Contact [] allContacts = manager.getAllContactsOnServer(client.getValues().getPrivateContactFolder());
		for (int i=0; i<allContacts.length; i++) {
			Contact co = allContacts[i];
			if (co.getObjectID() == contactObject1.getObjectID()) found1=true;
			if (co.getObjectID() == contactObject2.getObjectID()) found2=true;
		}
		assertTrue("First contact was not found.", found1);
		assertTrue("Second contact was not found.", found2);
	}
	
	public void testCreatedContactsAppearInListRequest () throws Exception {
		boolean found1 = false;
		boolean found2 = false;
		int[] firstContact = new int [] {contactObject1.getParentFolderID(), contactObject1.getObjectID()};
		int[] secondContact = new int [] {contactObject2.getParentFolderID(), contactObject2.getObjectID()};
		Contact [] allContacts = manager.listContactsOnServer(firstContact, secondContact);
		for (int i=0; i<allContacts.length; i++) {
			Contact co = allContacts[i];
			if (co.getObjectID() == contactObject1.getObjectID()) found1=true;
			if (co.getObjectID() == contactObject2.getObjectID()) found2=true;
		}
		assertTrue("First contact was not found.", found1);
		assertTrue("Second contact was not found.", found2);
	}
	
	public void testCreatedContactsAppearInSearchRequestOverAllFolders () throws Exception {
		boolean found1 = false;
		boolean found2 = false;
		// folderId "-1" means searching in all folders
		Contact [] allContacts = manager.searchForContactsOnServer("Herbert", -1);
		for (int i=0; i<allContacts.length; i++) {
			Contact co = allContacts[i];
			if (co.getObjectID() == contactObject1.getObjectID()) found1=true;
			if (co.getObjectID() == contactObject2.getObjectID()) found2=true;
		}
		assertTrue("First contact was not found.", found1);
		assertTrue("Second contact was not found.", found2);
	}
	
	public void testCreatedContactsAppearAsUpdatedSinceYesterday () throws Exception {
		boolean found1 = false;
		boolean found2 = false;
		Date date = new Date();
		date.setDate(date.getDate()-1);
		Contact [] allContacts = manager.getUpdatedContactsOnServer(client.getValues().getPrivateContactFolder(), date);
		for (int i=0; i<allContacts.length; i++) {
			Contact co = allContacts[i];
			if (co.getObjectID() == contactObject1.getObjectID()) found1=true;
			if (co.getObjectID() == contactObject2.getObjectID()) found2=true;
		}
		assertTrue("First contact was not found.", found1);
		assertTrue("Second contact was not found.", found2);
	}
}
