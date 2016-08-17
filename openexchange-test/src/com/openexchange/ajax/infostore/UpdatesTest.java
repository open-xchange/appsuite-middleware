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

package com.openexchange.ajax.infostore;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
import com.openexchange.ajax.infostore.actions.DetachInfostoreRequest;
import com.openexchange.ajax.infostore.actions.DetachInfostoreResponse;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.infostore.actions.UpdatesInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdatesInfostoreResponse;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.util.UUIDs;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.TestInit;


/**
 * {@link UpdatesTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdatesTest extends AbstractAJAXSession {

	protected static final int[] virtualFolders = {FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID};

    private FolderTestManager ftm;
    private FolderObject testFolder;
    private File knowledgeDoc;
    private File urlDoc;
    private InfostoreTestManager itm;

    public UpdatesTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        itm = new InfostoreTestManager(client);
        ftm = new FolderTestManager(client);
        testFolder = ftm.generatePrivateFolder(
            UUIDs.getUnformattedString(UUID.randomUUID()),
            FolderObject.INFOSTORE,
            client.getValues().getPrivateInfostoreFolder(),
            client.getValues().getUserId());
        ftm.insertFolderOnServer(testFolder);

        knowledgeDoc = new DefaultFile();
        knowledgeDoc.setFolderId(String.valueOf(testFolder.getObjectID()));
        knowledgeDoc.setTitle("test knowledge");
        knowledgeDoc.setDescription("test knowledge description");
        itm.newAction(knowledgeDoc);

        urlDoc = new DefaultFile();
        urlDoc.setFolderId(String.valueOf(testFolder.getObjectID()));
        urlDoc.setTitle("test url");
        urlDoc.setDescription("test url description");
        urlDoc.setURL("http://www.open-xchange.com");
        itm.newAction(urlDoc);
    }

    @Override
    public void tearDown() throws Exception {
        itm.cleanUp();
        ftm.cleanUp();
        super.tearDown();
    }

    public void testBasic() throws Exception {
        AllInfostoreRequest allReq = new AllInfostoreRequest(
            testFolder.getObjectID(),
            new int[] { Metadata.ID, Metadata.FOLDER_ID },
            Metadata.ID,
            Order.ASCENDING);
        AbstractColumnsResponse allResp = client.execute(allReq);
        Date timestamp = new Date(allResp.getTimestamp().getTime() + 2);

        File updateDoc = new DefaultFile();
        updateDoc.setId(knowledgeDoc.getId());
        updateDoc.setTitle("test knowledge updated");
        itm.updateAction(updateDoc, new File.Field[] { File.Field.TITLE }, timestamp);

        UpdatesInfostoreRequest req = new UpdatesInfostoreRequest(
            testFolder.getObjectID(),
            new int[] { Metadata.ID, Metadata.TITLE },
            Metadata.ID,
            Order.ASCENDING,
            Ignore.NONE,
            timestamp,
            true);
        UpdatesInfostoreResponse resp = client.execute(req);
        JSONArray modifiedValues = resp.getNewAndModified().iterator().next();
        assertEquals("Wrong number of modified documents", 1, resp.getNewAndModified().size());
        assertEquals("Wrong number of deleted documents", 0, resp.getDeleted().size());
        assertEquals("Wrong document id", updateDoc.getId(), modifiedValues.getString(0));
        assertEquals("Wrong document title", updateDoc.getTitle(), modifiedValues.getString(1));

        timestamp = itm.getLastResponse().getTimestamp();
        itm.deleteAction(knowledgeDoc.getId(), String.valueOf(testFolder.getObjectID()), timestamp);
        itm.deleteAction(urlDoc.getId(), String.valueOf(testFolder.getObjectID()), timestamp);

        req = new UpdatesInfostoreRequest(
            testFolder.getObjectID(),
            new int[] { Metadata.ID },
            Metadata.ID,
            Order.ASCENDING,
            Ignore.NONE,
            timestamp,
            true);
        resp = client.execute(req);
        assertEquals("Wrong number of modified documents", 0, resp.getNewAndModified().size());
        assertEquals("Wrong number of deleted documents", 2, resp.getDeleted().size());
        int found = 0;
        for (String id : resp.getDeleted()) {
            if (id.equals(String.valueOf(knowledgeDoc.getId())) || id.equals(String.valueOf(urlDoc.getId()))) {
                found++;
            }
        }
        assertEquals("Wrong documents have been deleted", 2, found);
    }

    public void testRemovedVersionForcesUpdate() throws Exception {
    	AllInfostoreRequest allReq = new AllInfostoreRequest(
                testFolder.getObjectID(),
                new int[] { Metadata.ID, Metadata.FOLDER_ID },
                Metadata.ID,
                Order.ASCENDING);
        AbstractColumnsResponse allResp = client.execute(allReq);
        Date timestamp = new Date(allResp.getTimestamp().getTime() + 2);

        java.io.File upload = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
        DefaultFile updateDoc = new DefaultFile();
        updateDoc.setId(knowledgeDoc.getId());
        updateDoc.setVersionComment("Comment 1");
        itm.updateAction(updateDoc, upload, new File.Field[] { File.Field.VERSION_COMMENT }, timestamp);
        timestamp = itm.getLastResponse().getTimestamp();

        updateDoc.setVersionComment("Comment 2");
        itm.updateAction(updateDoc, upload, new File.Field[] { File.Field.VERSION_COMMENT }, timestamp);
        timestamp = itm.getLastResponse().getTimestamp();

        updateDoc.setVersionComment("Comment 3");
        itm.updateAction(updateDoc, upload, new File.Field[] { File.Field.VERSION_COMMENT }, timestamp);
        timestamp = itm.getLastResponse().getTimestamp();

        DetachInfostoreRequest detachReq = new DetachInfostoreRequest(
        		updateDoc.getId(),
        		testFolder.getObjectID(),
        		Collections.singleton("3"),
        		timestamp);
        DetachInfostoreResponse detachResp = client.execute(detachReq);
        assertEquals("Version was not deleted", 0, detachResp.getNotDeleted().length);

        UpdatesInfostoreRequest req = new UpdatesInfostoreRequest(
                testFolder.getObjectID(),
                new int[] { Metadata.TITLE, Metadata.DESCRIPTION },
                Metadata.ID,
                Order.ASCENDING,
                Ignore.NONE,
                timestamp,
                true);
        UpdatesInfostoreResponse resp = client.execute(req);
        assertEquals("Wrong number of modified documents", 1, resp.getNewAndModified().size());
    }

    //Bug 4269
  	public void testVirtualFolder() throws Exception {
          for(int folderId : virtualFolders) {
              virtualFolderTest(folderId);
          }
  	}

    //Bug 4269
    @Test
  	public void virtualFolderTest(int folderId) throws Exception {
  		UpdatesInfostoreRequest req = new UpdatesInfostoreRequest(
  				folderId,
                new int[] { Metadata.ID },
                Metadata.ID,
                Order.ASCENDING,
                Ignore.NONE,
                new Date(0L),
                true);
        UpdatesInfostoreResponse resp = client.execute(req);
        assertEquals("Wrong number of modified documents", 0, resp.getNewAndModified().size() + resp.getDeleted().size());
  	}

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
  		UpdatesInfostoreRequest req = new UpdatesInfostoreRequest(
  				testFolder.getObjectID(),
                new int[] { Metadata.LAST_MODIFIED_UTC },
                Metadata.LAST_MODIFIED_UTC,
                Order.ASCENDING,
                Ignore.NONE,
                new Date(0L),
                true);
        UpdatesInfostoreResponse resp = client.execute(req);
        assertTrue("Wrong number of modified documents", resp.getNewAndModified().size() + resp.getDeleted().size() > 0);
    }

    // Bug 12427
	public void testNumberOfVersions() throws Exception {
	    java.io.File upload = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
		DefaultFile updateDoc = new DefaultFile();
        updateDoc.setId(knowledgeDoc.getId());
        updateDoc.setDescription("New description");
		itm.updateAction(updateDoc, upload, new File.Field[] { File.Field.DESCRIPTION }, new Date(Long.MAX_VALUE));

		UpdatesInfostoreRequest req = new UpdatesInfostoreRequest(
  				testFolder.getObjectID(),
                new int[] { Metadata.ID, Metadata.NUMBER_OF_VERSIONS },
                Metadata.ID,
                Order.ASCENDING,
                Ignore.NONE,
                new Date(0L),
                true);
        UpdatesInfostoreResponse resp = client.execute(req);

        boolean found = false;
        for (JSONArray modified : resp.getNewAndModified()) {
        	String id = modified.getString(0);
        	int numberOfVersions = modified.getInt(1);
        	if (id.equals(updateDoc.getId())) {
        		assertEquals(1, numberOfVersions);
        		found = true;
        	}
        }
        assertTrue(found);
	}

}
