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

import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link DistListTest} - Checks the distribution list handling.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DistListTest extends AbstractManagedContactTest {
	
    public DistListTest(String name) {
        super(name);
    }
    
	@Override
	public void setUp() throws Exception {
	    super.setUp();
	}
	
    public void testCreateWithoutMembers() throws OXException {
    	/*
    	 * create empty distribution list
    	 */
        Contact distributionList = super.generateContact("List");
    	distributionList.setDistributionList(new DistributionListEntryObject[]{new DistributionListEntryObject("dn", "mail@example.invalid", DistributionListEntryObject.INDEPENDENT)});
    	manager.setFailOnError(true);
    	distributionList = manager.newAction(distributionList);
    	/*
    	 * verify distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	
    }

    public void testCreateWithContactWithoutEMail() throws OXException {
    	/*
    	 * create contact
    	 */
	    Contact referencedContact = super.generateContact("Test");
	    referencedContact = manager.newAction(referencedContact);
    	/*
    	 * create distribution list
    	 */
        Contact distributionList = super.generateContact("List");
        DistributionListEntryObject[] members = new DistributionListEntryObject[1];
        members[0] = new DistributionListEntryObject();
        members[0].setEntryID(referencedContact.getObjectID());
        members[0].setFolderID(referencedContact.getParentFolderID());
        members[0].setDisplayname(referencedContact.getDisplayName());
        members[0].setEmailfield(DistributionListEntryObject.EMAILFIELD1);
    	distributionList.setDistributionList(members);
    	distributionList = manager.newAction(distributionList);
    	/*
    	 * verify distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertMatches(distributionList.getDistributionList(), referencedContact);
    }

    public void testCreateWithOneOffWithoutEMail() throws OXException {
    	/*
    	 * prepare distribution list
    	 */
        Contact distributionList = super.generateContact("List");
        DistributionListEntryObject[] members = new DistributionListEntryObject[1];
        members[0] = new DistributionListEntryObject();
        members[0].setDisplayname("Test no mail");
        members[0].setEmailfield(DistributionListEntryObject.INDEPENDENT);
    	distributionList.setDistributionList(members);
    	/*
    	 * try to create distribution list
    	 */
   		distributionList = manager.newAction(distributionList);
   		AbstractAJAXResponse lastResponse = manager.getLastResponse();
   		assertNotNull("no error message", lastResponse.getErrorMessage());
   		assertTrue("wrong exception", ContactExceptionCodes.EMAIL_MANDATORY_FOR_EXTERNAL_MEMBERS.equals(lastResponse.getException()));
    	/*
    	 * try to create with empty address instead
    	 */
        members[0].setEmailaddress("");
   		distributionList = manager.newAction(distributionList);
   		lastResponse = manager.getLastResponse();
   		assertNotNull("no error message", lastResponse.getErrorMessage());
   		assertTrue("wrong exception", ContactExceptionCodes.EMAIL_MANDATORY_FOR_EXTERNAL_MEMBERS.equals(lastResponse.getException()));
    }

    public void testRemoveEMailFromOneOff() throws OXException {
    	/*
    	 * create distribution list
    	 */
        Contact distributionList = super.generateContact("List");
        DistributionListEntryObject[] members = new DistributionListEntryObject[1];
        members[0] = new DistributionListEntryObject();
        members[0].setDisplayname("Test OneOff");
        members[0].setEmailaddress("hallo@example.com");
        members[0].setEmailfield(DistributionListEntryObject.INDEPENDENT);
    	distributionList.setDistributionList(members);
   		distributionList = manager.newAction(distributionList);
    	/*
    	 * verify distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	/*
    	 * try to update distribution list
    	 */
    	distributionList.getDistributionList()[0].setEmailaddress(null);
   		distributionList = manager.newAction(distributionList);
   		AbstractAJAXResponse lastResponse = manager.getLastResponse();
   		assertNotNull("no error message", lastResponse.getErrorMessage());
   		assertTrue("wrong exception", ContactExceptionCodes.EMAIL_MANDATORY_FOR_EXTERNAL_MEMBERS.equals(lastResponse.getException()));
    	/*
    	 * try to update with empty address instead
    	 */
    	distributionList.getDistributionList()[0].setEmailaddress("");
   		distributionList = manager.newAction(distributionList);
   		lastResponse = manager.getLastResponse();
   		assertNotNull("no error message", lastResponse.getErrorMessage());
   		assertTrue("wrong exception", ContactExceptionCodes.EMAIL_MANDATORY_FOR_EXTERNAL_MEMBERS.equals(lastResponse.getException()));
    }

    public void testDeleteReferencedContact() throws OXException {
    	/*
    	 * create contact
    	 */
	    Contact referencedContact = super.generateContact("Test");
	    referencedContact.setEmail1("mail@example.com");
	    referencedContact = manager.newAction(referencedContact);
    	/*
    	 * create distribution list
    	 */
        Contact distributionList = super.generateContact("List");
        DistributionListEntryObject[] members = new DistributionListEntryObject[1];
        members[0] = new DistributionListEntryObject();
        members[0].setEntryID(referencedContact.getObjectID());
        members[0].setFolderID(referencedContact.getParentFolderID());
        members[0].setDisplayname(referencedContact.getDisplayName());
        members[0].setEmailaddress(referencedContact.getEmail1());
        members[0].setEmailfield(DistributionListEntryObject.EMAILFIELD1);
    	distributionList.setDistributionList(members);
    	distributionList = manager.newAction(distributionList);
    	/*
    	 * verify distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertMatches(distributionList.getDistributionList(), referencedContact);
    	/*
    	 * delete referenced contact
    	 */
    	manager.deleteAction(referencedContact);
    	/*
    	 * verify distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertEquals("member mail field wrong", DistributionListEntryObject.INDEPENDENT, 
    			distributionList.getDistributionList()[0].getEmailfield());
    	assertEquals("member email address", referencedContact.getEmail1(), distributionList.getDistributionList()[0].getEmailaddress());
    }
    
    public void testDeleteReferencedContactAfterUpdate() throws OXException {
    	/*
    	 * create contact
    	 */
	    Contact referencedContact = super.generateContact("Test");
	    referencedContact.setEmail1("mail@example.com");
	    referencedContact = manager.newAction(referencedContact);
    	/*
    	 * create distribution list
    	 */
        Contact distributionList = super.generateContact("List");
        DistributionListEntryObject[] members = new DistributionListEntryObject[1];
        members[0] = new DistributionListEntryObject();
        members[0].setEntryID(referencedContact.getObjectID());
        members[0].setFolderID(referencedContact.getParentFolderID());
        members[0].setDisplayname(referencedContact.getDisplayName());
        members[0].setEmailaddress(referencedContact.getEmail1());
        members[0].setEmailfield(DistributionListEntryObject.EMAILFIELD1);
    	distributionList.setDistributionList(members);
    	distributionList = manager.newAction(distributionList);
    	/*
    	 * verify distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertMatches(distributionList.getDistributionList(), referencedContact);
    	/*
    	 * update referenced contact
    	 */
	    referencedContact.setEmail1("mail_edit@example.com");
	    referencedContact = manager.updateAction(referencedContact);
    	/*
    	 * verify distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertMatches(distributionList.getDistributionList(), referencedContact);
    	/*
    	 * delete referenced contact
    	 */
    	manager.deleteAction(referencedContact);
    	/*
    	 * verify distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertEquals("member mail field wrong", DistributionListEntryObject.INDEPENDENT, 
    			distributionList.getDistributionList()[0].getEmailfield());
    	assertEquals("member email address", referencedContact.getEmail1(), distributionList.getDistributionList()[0].getEmailaddress());
    }
    
    public void testAddReferencedContactWithoutFolderID() throws OXException {
    	/*
    	 * create contact
    	 */
	    Contact referencedContact = super.generateContact("Test");
	    referencedContact.setEmail1("mail@example.com");
	    referencedContact = manager.newAction(referencedContact);
    	/*
    	 * create distribution list
    	 */
        Contact distributionList = super.generateContact("List");
        DistributionListEntryObject[] members = new DistributionListEntryObject[1];
        members[0] = new DistributionListEntryObject();
        members[0].setEntryID(referencedContact.getObjectID());
        members[0].setDisplayname(referencedContact.getDisplayName());
        members[0].setEmailaddress(referencedContact.getEmail1());
        members[0].setEmailfield(DistributionListEntryObject.EMAILFIELD1);
    	distributionList.setDistributionList(members);
    	distributionList = manager.newAction(distributionList);
    	/*
    	 * verify distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertNotNull("distibution list not found", distributionList);
    	assertTrue("not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
    	assertEquals("member count wrong", 1, distributionList.getNumberOfDistributionLists());
    	assertEquals("member count wrong", 1, distributionList.getDistributionList().length);
    	assertEquals("referenced object id wrong", referencedContact.getObjectID(), distributionList.getDistributionList()[0].getEntryID());
    	assertEquals("referenced folder id wrong", referencedContact.getParentFolderID(), distributionList.getDistributionList()[0].getFolderID());
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
