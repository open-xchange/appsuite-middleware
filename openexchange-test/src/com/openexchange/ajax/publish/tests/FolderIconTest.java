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

package com.openexchange.ajax.publish.tests;

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.publish.actions.NewPublicationRequest;
import com.openexchange.ajax.publish.actions.NewPublicationResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.publish.Publication;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;

/**
 * Test to check whether field 3010 does properly indicated a published folder
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class FolderIconTest extends AbstractPublicationTest {

    private FolderObject folder;

    public FolderIconTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // create contact folder
        folder = fMgr.generateFolder(
            "publishedContacts",
            FolderObject.CONTACT,
            getClient().getValues().getPrivateContactFolder(),
            getClient().getValues().getUserId());
        fMgr.insertFolderOnServer(folder);

        // fill contact folder
        Contact contact = generateContact("Herbert", "Meier");
        contact.setParentFolderID(folder.getObjectID());
        cMgr.newAction(contact);

        // publish
    }

    protected void publish() throws Exception {
        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();

        Publication expected = generatePublication("contacts", String.valueOf(folder.getObjectID()), discovery);
        NewPublicationRequest newReq = new NewPublicationRequest(expected);
        AJAXClient myClient = getClient();
        NewPublicationResponse newResp = myClient.execute(newReq);
        expected.setId(newResp.getId());
    }

    public void testShouldSetTheIconViaGet() throws Exception {
        // check negative
        fMgr.getFolderFromServer(folder.getObjectID(), false, new int[] { 3010 });
        GetResponse response = (GetResponse) fMgr.getLastResponse();
        JSONObject data = (JSONObject) response.getData();
        assertTrue(
            "Should contain the key 'com.openexchange.publish.publicationFlag' even before publication",
            data.has("com.openexchange.publish.publicationFlag"));
        assertFalse(
            "Key 'com.openexchange.publish.publicationFlag' should have 'false' value before publication",
            data.getBoolean("com.openexchange.publish.publicationFlag"));
        
        // publish
        publish();

        // check positive
        fMgr.getFolderFromServer(folder.getObjectID(), false, new int[] { 3010 });
        response = (GetResponse) fMgr.getLastResponse();
        data = (JSONObject) response.getData();
        assertTrue(
            "Should contain the key 'com.openexchange.publish.publicationFlag'",
            data.has("com.openexchange.publish.publicationFlag"));
        assertTrue(
            "Key 'com.openexchange.publish.publicationFlag' should have 'true' value after publication",
            data.getBoolean("com.openexchange.publish.publicationFlag"));
    }

    public void testShouldSetTheIconViaList() throws Exception {
        // check negative
        fMgr.listFoldersOnServer(folder.getParentFolderID(), new int[] { 3010 });
        AbstractAJAXResponse response = fMgr.getLastResponse();
        JSONArray arr = (JSONArray) response.getData();
        assertTrue("Should return at least one folder", arr.length() > 0);
        //PENDING
        JSONObject data = null;
        
        assertTrue(
            "Should contain the key 'com.openexchange.publish.publicationFlag' even before publication",
            data.has("com.openexchange.publish.publicationFlag"));
        assertFalse(
            "Key 'com.openexchange.publish.publicationFlag' should have 'false' value before publication",
            data.getBoolean("com.openexchange.publish.publicationFlag"));

        // publish
        publish();

        // check positive
        fMgr.getFolderFromServer(folder.getParentFolderID(), false, new int[] { 3010 });
        response = (GetResponse) fMgr.getLastResponse();
        arr = (JSONArray) response.getData();
        assertTrue("Should return at least one field", arr.length() > 0);
        data = arr.getJSONObject(0);

        assertTrue(
            "Should contain the key 'com.openexchange.publish.publicationFlag'",
            data.has("com.openexchange.publish.publicationFlag"));
        assertTrue(
            "Key 'com.openexchange.publish.publicationFlag' should have 'true' value after publication",
            data.getBoolean("com.openexchange.publish.publicationFlag"));
    }

    public void testShouldSetTheIconViaUpdates() throws Exception {
        //  check negative
        Date lastModified = fMgr.getLastResponse().getTimestamp();
        fMgr.getUpdatedFoldersOnServer(folder.getObjectID(), lastModified, new int[] { 3010 });
        AbstractAJAXResponse response = fMgr.getLastResponse();
        
        JSONArray arr = (JSONArray) response.getData();
        assertTrue("Should return at least one field", arr.length() > 0);
        JSONObject data = arr.getJSONObject(0);
        assertTrue(
            "Should contain the key 'com.openexchange.publish.publicationFlag' even before publication",
            data.has("com.openexchange.publish.publicationFlag"));
        assertFalse(
            "Key 'com.openexchange.publish.publicationFlag' should have 'false' value before publication",
            data.getBoolean("com.openexchange.publish.publicationFlag"));

        
        // publish
        publish();

        // check positive
        lastModified = fMgr.getLastResponse().getTimestamp();
        fMgr.getUpdatedFoldersOnServer(folder.getObjectID(), lastModified, new int[] { 3010 });
        response = (GetResponse) fMgr.getLastResponse();
        
        arr = (JSONArray) response.getData();
        assertTrue("Should return at least one field", arr.length() > 0);
        data = arr.getJSONObject(0);

        assertTrue(
            "Should contain the key 'com.openexchange.publish.publicationFlag'",
            data.has("com.openexchange.publish.publicationFlag"));
        assertTrue(
            "Key 'com.openexchange.publish.publicationFlag' should have 'true' value after publication",
            data.getBoolean("com.openexchange.publish.publicationFlag"));
    }
}
