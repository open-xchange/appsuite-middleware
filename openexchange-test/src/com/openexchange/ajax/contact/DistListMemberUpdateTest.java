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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
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

    public DistListMemberUpdateTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        /*
         * prepare two contacts
         */
        referencedContact1 = super.generateContact("Test1");
        referencedContact1.setEmail1("email1@example.com");
        referencedContact1.setEmail2("email2@example.com");
        referencedContact1.setEmail3("email3@example.com");
        referencedContact1 = cotm.newAction(referencedContact1);
        referencedContact2 = super.generateContact("Test2");
        referencedContact2.setEmail1("email1@example.org");
        referencedContact2.setEmail2("email2@example.org");
        referencedContact2.setEmail3("email3@example.org");
        referencedContact2 = cotm.newAction(referencedContact2);
    }

    private Contact createDistributionList(int mailField, Contact... referencedContacts) throws OXException {
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
        return cotm.newAction(distributionList);
    }

    @Test
    public void testUpdateEMail1() throws OXException {
        Contact distributionList = createDistributionList(1, referencedContact1, referencedContact2);
        /*
         * edit referenced contact's email1-address
         */
        referencedContact1.setEmail1("update_" + referencedContact1.getEmail1());
        cotm.updateAction(referencedContact1);
        /*
         * verify the distribution list
         */
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
        /*
         * edit other referenced contact's email1-address
         */
        referencedContact2.setEmail1("update_" + referencedContact2.getEmail1());
        cotm.updateAction(referencedContact2);
        /*
         * verify the distribution list
         */
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }

    @Test
    public void testUpdateEMail2() throws OXException {
        Contact distributionList = createDistributionList(2, referencedContact1, referencedContact2);
        /*
         * edit referenced contact's email1-address
         */
        referencedContact1.setEmail2("update_" + referencedContact1.getEmail2());
        cotm.updateAction(referencedContact1);
        /*
         * verify the distribution list
         */
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
        /*
         * edit other referenced contact's email1-address
         */
        referencedContact2.setEmail2("update_" + referencedContact2.getEmail2());
        cotm.updateAction(referencedContact2);
        /*
         * verify the distribution list
         */
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }

    @Test
    public void testUpdateEMail3() throws OXException {
        Contact distributionList = createDistributionList(3, referencedContact1, referencedContact2);
        /*
         * edit referenced contact's email1-address
         */
        referencedContact1.setEmail3("update_" + referencedContact1.getEmail1());
        cotm.updateAction(referencedContact1);
        /*
         * verify the distribution list
         */
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
        /*
         * edit other referenced contact's email1-address
         */
        referencedContact2.setEmail3("update_" + referencedContact2.getEmail3());
        cotm.updateAction(referencedContact2);
        /*
         * verify the distribution list
         */
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }

    @Test
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
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
        /*
         * remove other referenced contact's email1-address
         */
        referencedContact2.setEmail1(null);
        this.updateContact(referencedContact2, referencedContact2.getParentFolderID());
        /*
         * verify the distribution list
         */
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }

    @Test
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
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
        /*
         * remove other referenced contact's email2-address
         */
        referencedContact2.setEmail2(null);
        this.updateContact(referencedContact2, referencedContact2.getParentFolderID());
        /*
         * verify the distribution list
         */
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }

    @Test
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
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
        /*
         * remove other referenced contact's email3-address
         */
        referencedContact2.setEmail3(null);
        this.updateContact(referencedContact2, referencedContact2.getParentFolderID());
        /*
         * verify the distribution list
         */
        distributionList = cotm.getAction(distributionList);
        assertMatches(distributionList.getDistributionList(), referencedContact1, referencedContact2);
    }

    private static void assertMatches(DistributionListEntryObject[] members, Contact... contacts) {
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
                    } else if (DistributionListEntryObject.EMAILFIELD3 == member.getEmailfield()) {
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
        getClient().execute(modifiedRequest);
    }

    /**
     * Set the email fields in a contact already converted to a JSONObject.
     * 
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
