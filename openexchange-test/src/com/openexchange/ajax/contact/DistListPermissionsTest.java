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

package com.openexchange.ajax.contact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.ContactTestManager;
import com.openexchange.test.FolderTestManager;

/**
 * {@link DistListPermissionsTest} - Checks the distribution list handling.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DistListPermissionsTest extends AbstractManagedContactTest {
	
    private ContactTestManager manager2;
    private AJAXClient client2;
    private FolderTestManager folderManager2;    
    
    private FolderObject sharedFolder;
    private Contact referencedContact1;
    private Contact referencedContact2;

    public DistListPermissionsTest(String name) {
        super(name);
    }
    
    @Override
	public void setUp() throws Exception {
        super.setUp();
        client2 = new AJAXClient(AJAXClient.User.User2);
        manager2 = new ContactTestManager(client2);
        folderManager2 = new FolderTestManager(client2);
    	/*
    	 * create a shared folder as user 2
    	 */
    	sharedFolder = folderManager.generateSharedFolder("DistListTest_" + UUID.randomUUID().toString(), Module.CONTACTS.getFolderConstant(), 
    			client2.getValues().getPrivateContactFolder(), new int[] { client2.getValues().getUserId(), client.getValues().getUserId() });
    	sharedFolder = folderManager2.insertFolderOnServer(sharedFolder);
    	/*
    	 * create two contacts in that folder
    	 */
	    referencedContact1 = super.generateContact("Test1");
	    referencedContact1.setEmail1("email1@example.com");
	    referencedContact1.setEmail2("email2@example.com");
	    referencedContact1.setEmail3("email3@example.com");
	    referencedContact1.setParentFolderID(sharedFolder.getObjectID());
	    referencedContact1 = manager2.newAction(referencedContact1);
	    referencedContact2 = super.generateContact("Test2");
	    referencedContact2.setEmail1("email1@example.org");
	    referencedContact2.setEmail2("email2@example.org");
	    referencedContact2.setEmail3("email3@example.org");
	    referencedContact2.setParentFolderID(sharedFolder.getObjectID());
	    referencedContact2 = manager2.newAction(referencedContact2);
    }
    
	@Override
	public void tearDown() throws Exception {
        manager2.cleanUp();
    	folderManager2.cleanUp();
	    super.tearDown();
	}

    public void testReferencedContactFromSharedFolder() throws OXException, IOException, JSONException {
    	/*
    	 * create distribution list
    	 */
        Contact distributionList = generateDistributionList(1, referencedContact1, referencedContact2);
    	distributionList = manager.newAction(distributionList);
    	/*
    	 * verify distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 2, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 2, distributionList.getDistributionList().length);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }

    public void testPrivatizeReferencedContactFromSharedFolder() throws OXException, IOException, JSONException {
    	/*
    	 * create distribution list as user 1
    	 */
        Contact distributionList = generateDistributionList(1, referencedContact1);
    	distributionList = manager.newAction(distributionList);
    	/*
    	 * verify distribution list as user 1
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertMatches(distributionList.getDistributionList(), referencedContact1);
    	/*
    	 * privatize member as user 2
    	 */
    	referencedContact1.setPrivateFlag(true);
    	referencedContact1 = manager2.updateAction(referencedContact1);
    	/*
    	 * verify distribution list as user 1 
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertEquals("member mail field wrong", DistributionListEntryObject.INDEPENDENT, 
    			distributionList.getDistributionList()[0].getEmailfield());
    	assertEquals("member email address", referencedContact1.getEmail1(), distributionList.getDistributionList()[0].getEmailaddress());
    }
    
    public void testUnPrivatizeReferencedContactFromSharedFolder() throws OXException, IOException, JSONException {
    	/*
    	 * create distribution list as user 1
    	 */
        Contact distributionList = generateDistributionList(1, referencedContact1);
    	distributionList = manager.newAction(distributionList);
    	/*
    	 * verify distribution list as user 1
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertMatches(distributionList.getDistributionList(), referencedContact1);
    	/*
    	 * privatize member as user 2
    	 */
    	referencedContact1.setPrivateFlag(true);
    	referencedContact1 = manager2.updateAction(referencedContact1);
    	/*
    	 * verify distribution list as user 1 
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertEquals("member mail field wrong", DistributionListEntryObject.INDEPENDENT, 
    			distributionList.getDistributionList()[0].getEmailfield());
    	assertEquals("member email address", referencedContact1.getEmail1(), distributionList.getDistributionList()[0].getEmailaddress());
    	/*
    	 * un-privatize member as user 2
    	 */
    	referencedContact1.setPrivateFlag(false);
    	referencedContact1 = manager2.updateAction(referencedContact1);
    	/*
    	 * verify distribution list as user 1
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertMatches(distributionList.getDistributionList(), referencedContact1);
    }
    
    public void testRemovePermissionsForReferencedContact() throws OXException, IOException, JSONException {
    	/*
    	 * create distribution list as user 1
    	 */
        Contact distributionList = generateDistributionList(1, referencedContact1);
    	distributionList = manager.newAction(distributionList);
    	/*
    	 * verify distribution list as user 1
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertMatches(distributionList.getDistributionList(), referencedContact1);
    	/*
    	 * update permissions of shared folder
    	 */
    	Iterator<OCLPermission> iterator = sharedFolder.getPermissions().iterator();
    	while (iterator.hasNext()) {
    		if (false == iterator.next().isFolderAdmin()) {
    			iterator.remove();
    		}
    	}
    	folderManager2.updateFolderOnServer(sharedFolder);
    	/*
    	 * verify distribution list as user 1 
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertEquals("member mail field wrong", DistributionListEntryObject.INDEPENDENT, 
    			distributionList.getDistributionList()[0].getEmailfield());
    	assertEquals("member email address", referencedContact1.getEmail1(), distributionList.getDistributionList()[0].getEmailaddress());
    }
    
    public void testGrantPermissionsForReferencedContact() throws OXException, IOException, JSONException {
    	/*
    	 * create distribution list as user 1
    	 */
        Contact distributionList = generateDistributionList(1, referencedContact1);
    	distributionList = manager.newAction(distributionList);
    	/*
    	 * verify distribution list as user 1
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertMatches(distributionList.getDistributionList(), referencedContact1);
    	/*
    	 * update permissions of shared folder temporary
    	 */
    	OCLPermission removedPermission = null;
    	Iterator<OCLPermission> iterator = sharedFolder.getPermissions().iterator();
    	while (iterator.hasNext()) {
    		OCLPermission permission = iterator.next();
    		if (false == permission.isFolderAdmin()) {
    			removedPermission = permission;
    			iterator.remove();
    		}
    	}
    	sharedFolder = folderManager2.updateFolderOnServer(sharedFolder);
    	sharedFolder.setLastModified(folderManager2.getLastResponse().getTimestamp());
    	/*
    	 * verify distribution list as user 1 
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertEquals("member mail field wrong", DistributionListEntryObject.INDEPENDENT, 
    			distributionList.getDistributionList()[0].getEmailfield());
    	assertEquals("member email address", referencedContact1.getEmail1(), distributionList.getDistributionList()[0].getEmailaddress());
    	/*
    	 * update permissions of shared folder back
    	 */
    	sharedFolder.getPermissions().add(removedPermission);
    	sharedFolder = folderManager2.updateFolderOnServer(sharedFolder);
    	/*
    	 * verify distribution list as user 1 
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertMatches(distributionList.getDistributionList(), referencedContact1);
    }
    

	private Contact generateDistributionList(int mailField, Contact...referencedContacts) throws OXException {
		/*
		 * create a distribtution list, referencing to the contacts
		 */
        Contact distributionList = super.generateContact("List");
        List<DistributionListEntryObject> members = new ArrayList<DistributionListEntryObject>();
        for (Contact referencedContact : referencedContacts) {
        	DistributionListEntryObject member = new DistributionListEntryObject();
        	member.setEntryID(referencedContact.getObjectID());
        	member.setEmailfield(mailField);
        	member.setFolderID(referencedContact.getParentFolderID());
        	member.setDisplayname(referencedContact.getDisplayName());
        	if (DistributionListEntryObject.EMAILFIELD1 == mailField) {
            	member.setEmailaddress(referencedContact.getEmail1());
        	} else if (DistributionListEntryObject.EMAILFIELD2 == mailField) {
            	member.setEmailaddress(referencedContact.getEmail2());
        	} else if (DistributionListEntryObject.EMAILFIELD3 == mailField) {
            	member.setEmailaddress(referencedContact.getEmail3());
        	} else {
        		throw new IllegalArgumentException(); 
        	}
        	members.add(member);
		}
    	distributionList.setDistributionList(members.toArray(new DistributionListEntryObject[0]));
    	return distributionList;
	}

    private static void assertMatches(DistributionListEntryObject[] members, Contact...contacts) {
    	for (Contact contact : contacts) {
			boolean found = false;
    		for (DistributionListEntryObject member : members) {
    			if (contact.getObjectID() == member.getEntryID()) {
    				found = true;
    				String referencedMail = null;
    				if (DistributionListEntryObject.EMAILFIELD1 == member.getEmailfield()) {
    					referencedMail = contact.getEmail1();
    				} else if (DistributionListEntryObject.EMAILFIELD2 == member.getEmailfield()) {
    					referencedMail = contact.getEmail2();
    				} else if  (DistributionListEntryObject.EMAILFIELD3 == member.getEmailfield()) {
    					referencedMail = contact.getEmail3();
    				} else {
    					fail("wrong mailfiled set in member");
    				}
    				assertEquals("referenced mail wrong", member.getEmailaddress(), referencedMail);
    			}
			}
    		assertTrue("contact not found in distlist members", found);
		}
    }

}
