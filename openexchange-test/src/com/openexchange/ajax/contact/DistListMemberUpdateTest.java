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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.contact.action.UpdateRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link DistListMemberUpdateTest} - Checks if changes in contacts referenced 
 * by distribution list members are reflected in the distribution list, too.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DistListMemberUpdateTest extends AbstractManagedContactTest {
	
	private Contact referencedContact1, referencedContact2;
	
    public DistListMemberUpdateTest(String name) {
        super(name);
    }
    
	@Override
	public void setUp() throws Exception {
	    super.setUp();
    	/*
    	 * prepare two contacts
    	 */
	    referencedContact1 = super.generateContact("Test1");
	    referencedContact1.setEmail1("email1@example.com");
	    referencedContact1.setEmail2("email2@example.com");
	    referencedContact1.setEmail3("email3@example.com");
	    referencedContact1 = manager.newAction(referencedContact1);
	    referencedContact2 = super.generateContact("Test2");
	    referencedContact2.setEmail1("email1@example.org");
	    referencedContact2.setEmail2("email2@example.org");
	    referencedContact2.setEmail3("email3@example.org");
	    referencedContact2 = manager.newAction(referencedContact2);
	}
	
	private Contact createDistributionList(int mailField, Contact...referencedContacts) throws OXException {
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
    	return manager.newAction(distributionList);
	}
    
    public void testUpdateEMail1() throws OXException {
    	Contact distributionList = createDistributionList(1, referencedContact1, referencedContact2);
    	/*
    	 * edit referenced contact's email1-address
    	 */
    	referencedContact1.setEmail1("update_" + referencedContact1.getEmail1());
    	manager.updateAction(referencedContact1);
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    	/*
    	 * edit other referenced contact's email1-address
    	 */
    	referencedContact2.setEmail1("update_" + referencedContact2.getEmail1());
    	manager.updateAction(referencedContact2);
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }
    
    public void testUpdateEMail2() throws OXException {
    	Contact distributionList = createDistributionList(2, referencedContact1, referencedContact2);
    	/*
    	 * edit referenced contact's email1-address
    	 */
    	referencedContact1.setEmail2("update_" + referencedContact1.getEmail2());
    	manager.updateAction(referencedContact1);
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    	/*
    	 * edit other referenced contact's email1-address
    	 */
    	referencedContact2.setEmail2("update_" + referencedContact2.getEmail2());
    	manager.updateAction(referencedContact2);
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }
    
    public void testUpdateEMail3() throws OXException {
    	Contact distributionList = createDistributionList(3, referencedContact1, referencedContact2);
    	/*
    	 * edit referenced contact's email1-address
    	 */
    	referencedContact1.setEmail3("update_" + referencedContact1.getEmail1());
    	manager.updateAction(referencedContact1);
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    	/*
    	 * edit other referenced contact's email1-address
    	 */
    	referencedContact2.setEmail3("update_" + referencedContact2.getEmail3());
    	manager.updateAction(referencedContact2);
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }

    public void testRemoveEMail1() throws Exception {
    	Contact distributionList = createDistributionList(1, referencedContact1, referencedContact2);
    	/*
    	 * remove referenced contact's email1-address (using the workarounds from EmptyEmailTest)
    	 */
    	referencedContact1.setEmail1(null);
    	this.updateContact(referencedContact1, referencedContact1.getParentFolderID());
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    	/*
    	 * remove other referenced contact's email1-address
    	 */
    	referencedContact2.setEmail1(null);
    	this.updateContact(referencedContact2, referencedContact2.getParentFolderID());
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }

    public void testRemoveEMail2() throws Exception {
    	Contact distributionList = createDistributionList(2, referencedContact1, referencedContact2);
    	/*
    	 * remove referenced contact's email2-address (using the workarounds from EmptyEmailTest)
    	 */
    	referencedContact1.setEmail2(null);
    	this.updateContact(referencedContact1, referencedContact1.getParentFolderID());
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    	/*
    	 * remove other referenced contact's email2-address
    	 */
    	referencedContact2.setEmail2(null);
    	this.updateContact(referencedContact2, referencedContact2.getParentFolderID());
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }

    public void testRemoveEMail3() throws Exception {
    	Contact distributionList = createDistributionList(3, referencedContact1, referencedContact2);
    	/*
    	 * remove referenced contact's email3-address (using the workarounds from EmptyEmailTest)
    	 */
    	referencedContact1.setEmail3(null);
    	this.updateContact(referencedContact1, referencedContact1.getParentFolderID());
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    	/*
    	 * remove other referenced contact's email3-address
    	 */
    	referencedContact2.setEmail3(null);
    	this.updateContact(referencedContact2, referencedContact2.getParentFolderID());
    	/*
    	 * verify the distribution list
    	 */
    	distributionList = manager.getAction(distributionList);
    	assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
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
    
    /**
     * the following methods are taken from EmptyEmailTest 
     */
    
    /**
     * {@link EmptyEmailUpdateRequest} - Private inner class that let's us set empty emails during updates. Workaround needed because
     * DataWriter will always replace empty Strings with null which results in the field not being set in the JSONObject
     * 
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     */
    private class EmptyEmailUpdateRequest extends UpdateRequest {

        private JSONObject modifiedBody;

        /**
         * Initializes a new {@link EmptyEmailUpdateRequest}.
         * 
         * @param contactObj
         */
        public EmptyEmailUpdateRequest(Contact contactObj) {
            super(contactObj);
        }

        public EmptyEmailUpdateRequest(Contact co, JSONObject jo) {
            super(co);
            modifiedBody = jo;
        }

        @Override
        public Object getBody() throws JSONException {
            return modifiedBody;
        }

    }

    /**
     * Own version of updateContact, expanding the one from {@link AbstractContactTest} with email fields and using
     * {@link EmptyEmailUpdateRequest}
     * 
     * @param contact The contact to update
     * @param inFolder the folder
     * @param email1 email1
     * @param email2 email2
     * @param email3 email3
     * @throws Exception
     */
    private void updateContact(final Contact contact, final int inFolder) throws Exception {
        final UpdateRequest request = new UpdateRequest(inFolder, contact, true);
        JSONObject jsonObject = (JSONObject) request.getBody();
        jsonObject = setEmail(jsonObject, contact.getEmail1(), contact.getEmail2(), contact.getEmail3());
        EmptyEmailUpdateRequest modifiedRequest = new EmptyEmailUpdateRequest(contact, jsonObject);
        client.execute(modifiedRequest);
    }

    /**
     * Set the email fields in a contact already converted to a JSONObject. 
     * @param jo JSONObject
     * @param email1 email1
     * @param email2 email2
     * @param email3 email3
     * @return The modified contact in JSONObject form
     * @throws JSONException
     */
    private JSONObject setEmail(final JSONObject jo, final String email1, final String email2, final String email3) throws JSONException {
        jo.put("email1", email1 == null ? JSONObject.NULL : email1);
        jo.put("email2", email2 == null ? JSONObject.NULL : email3);
        jo.put("email3", email3 == null ? JSONObject.NULL : email3);
        return jo;
    }

}
