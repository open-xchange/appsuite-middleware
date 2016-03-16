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

package com.openexchange.ajax.publish.tests;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.publish.actions.AllPublicationsRequest;
import com.openexchange.ajax.publish.actions.AllPublicationsResponse;
import com.openexchange.ajax.publish.actions.NewPublicationRequest;
import com.openexchange.ajax.publish.actions.NewPublicationResponse;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.publish.Publication;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;
import com.openexchange.test.TestInit;


/**
 * {@link AllPublicationsTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AllPublicationsTest extends AbstractPublicationTest {
	public AllPublicationsTest(String name) {
        super(name);
    }

    public void testShouldNotFindNonExistingPublication() throws OXException, IOException, SAXException, JSONException{
        AJAXClient myClient = getClient();

        FolderObject testFolder = getFolderManager().generatePublicFolder("pubsub", FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        getFolderManager().insertFolderOnServer( testFolder );

        AllPublicationsRequest req = new AllPublicationsRequest(String.valueOf(testFolder.getObjectID()), Integer.MAX_VALUE, "calendar", new LinkedList<String>());

        AllPublicationsResponse res = myClient.execute(req);
        JSONArray data = (JSONArray) res.getData();
        assertEquals("Array should be empty", I(0), I(data.length()));
    }

    public void testShouldFindOneFreshlyCreatedPublication() throws OXException, IOException, SAXException, JSONException, OXException, OXException{
        Contact contact = createDefaultContactFolderWithOneContact();
        String folderID = String.valueOf(contact.getParentFolderID() );
        String module = "contacts";

        // publish
        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();

        Publication expected = generatePublication(module, folderID, discovery);
        expected.setDisplayName("This will be changed");
        NewPublicationRequest newReq = new NewPublicationRequest(expected);
        AJAXClient myClient = getClient();
        NewPublicationResponse newResp = myClient.execute(newReq);
        assertFalse("Precondition: Should be able to create a publication: "+newResp.getException(), newResp.hasError());
        expected.setId(newResp.getId());

        //retrieve publications
        AllPublicationsRequest req = new AllPublicationsRequest(folderID, expected.getId(), module, Arrays.asList(new String[]{"id","entity", "entityModule", "displayName", "target"}));
        AllPublicationsResponse resp = getClient().execute(req);
        assertFalse("Should work", resp.hasError());
        assertEquals("Should have exactly one result", 1, resp.getAll().size());

        JSONArray actual = resp.getAll().get(0);
        assertEquals("Should have same publication ID", expected.getId(), actual.getInt(0));
        assertEquals(expected.getEntityId(), actual.getJSONObject(1).get("folder"));
        assertEquals("Should have same module", expected.getModule(), actual.getString(2));
        assertFalse("Should change display name", expected.getDisplayName().equals(actual.getString(3)));
        assertEquals("Should have same target ID", expected.getTarget().getId(), actual.getString(4));
    }

    public void testShouldFindOneFreshlyCreatedPublicationForEmptyFolder() throws OXException, IOException, SAXException, JSONException, OXException, OXException{
        FolderObject folder = createDefaultContactFolder();
        String folderID = String.valueOf(folder.getObjectID() );
        String module = "contacts";

        // publish
        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();
        pubMgr.setPublicationTargetDiscoveryService(discovery);

        Publication expected = generatePublication(module, folderID, discovery);
        expected.setDisplayName("This will be changed");

        pubMgr.newAction(expected);
        assertFalse("Precondition: Should be able to create a publication", pubMgr.getLastResponse().hasError());

        //retrieve publications
        pubMgr.allAction(folderID, expected.getId(), module, Arrays.asList(new String[]{"id","entity", "entityModule", "displayName", "target"}));
        AllPublicationsResponse resp = (AllPublicationsResponse) pubMgr.getLastResponse();
        assertFalse("Should work", resp.hasError());
        assertEquals("Should have exactly one result", 1, resp.getAll().size());

        JSONArray actual = resp.getAll().get(0);
        assertEquals("Should have same publication ID", expected.getId(), actual.getInt(0));
        assertEquals(expected.getEntityId(), actual.getJSONObject(1).get("folder"));
        assertEquals("Should have same module", expected.getModule(), actual.getString(2));
        assertFalse("Should change display name", expected.getDisplayName().equals(actual.getString(3)));
        assertEquals("Should have same target ID", expected.getTarget().getId(), actual.getString(4));
    }

    public void testShouldFindAllPublicationsOfUser() throws OXException, IOException, SAXException, JSONException, OXException, OXException {
    	// create folders
    	FolderObject contactFolder = createDefaultContactFolder();
    	String contactModule = "contacts";

    	FolderObject infostoreFolder = createDefaultInfostoreFolder("Folder for Publication-"+System.currentTimeMillis());
    	String infostoreModule = "infostore";

    	// create and upload a new Infostore item.
    	InfostoreTestManager infoMgr = getInfostoreManager();
        FolderObject infostorePublicationFolder = createDefaultInfostoreFolder("Second Folder for Publication-"+System.currentTimeMillis());

        File data = new DefaultFile();
        data.setTitle("Infostore Item To Be Published");
        data.setDescription("Infostore Item To Be Published");
        data.setFileMIMEType("text/plain");
        data.setFolderId(String.valueOf(infostorePublicationFolder.getObjectID()));
        java.io.File upload = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
        data.setFileName(upload.getName());

        infoMgr.newAction(data, upload);

    	// publish
    	ArrayList<Publication> expectedPublications = new ArrayList<Publication>();
    	SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();
        pubMgr.setPublicationTargetDiscoveryService(discovery);

        Publication contactPublication = generatePublication(contactModule, String.valueOf(contactFolder.getObjectID()), discovery);
        contactPublication.setDisplayName("My Contact Publication");
        expectedPublications.add(contactPublication);

        Publication infostorePublication = generatePublication(infostoreModule,String.valueOf(infostoreFolder.getObjectID()), discovery);
        infostorePublication.setDisplayName("My InfostoreFolder Publication");
        expectedPublications.add(infostorePublication);

        Publication infostoreItemPublication = generateInfostoreItemPublication(getObjectId(data), discovery);
        expectedPublications.add(infostoreItemPublication);

        for (Publication p : expectedPublications) {
        	pubMgr.newAction(p);
        	assertFalse("Precondition: Should be able to create a publication", pubMgr.getLastResponse().hasError());
        }

        // get all publications
        pubMgr.allAction(Arrays.asList(new String[] {"id", "entity", "entityModule", "displayName", "target"}));
        AllPublicationsResponse resp = (AllPublicationsResponse) pubMgr.getLastResponse();
        List<JSONArray> all = resp.getAll();
        List<Integer> foundIds = new ArrayList<Integer>();

        for (JSONArray c : all) {
        	int id = c.getInt(0);
        	foundIds.add(id);
        }

        boolean foundAllContacts = true;
        for (Publication p : expectedPublications) {
        	if (!foundIds.contains(p.getId())) {
        		foundAllContacts = false;
        	}
        }

        assertTrue("Did not get all contact publications.", foundAllContacts);

        // get all contact publications
        pubMgr.allAction(contactModule, -1, Arrays.asList(new String[]{"id","entity", "entityModule", "displayName", "target"}));
        resp = (AllPublicationsResponse) pubMgr.getLastResponse();
        assertFalse("Should work", resp.hasError());
        List<JSONArray> allContacts = resp.getAll();

        foundIds.clear();

        for (JSONArray c : allContacts) {
        	int id = c.getInt(0);
        	foundIds.add(id);
        }

        assertTrue("Did not get published contact.", foundIds.contains(contactPublication.getId()));

        infoMgr.cleanUp();
    }
}
