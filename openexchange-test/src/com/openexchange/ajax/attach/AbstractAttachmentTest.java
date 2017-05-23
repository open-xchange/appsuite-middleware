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

package com.openexchange.ajax.attach;

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import com.openexchange.ajax.AttachmentTest;
import com.openexchange.ajax.attach.actions.GetDocumentResponse;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.quota.FilestoreQuotaRequest;
import com.openexchange.ajax.quota.FilestoreQuotaResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.search.Order;
import com.openexchange.test.OXTestToolkit;

public abstract class AbstractAttachmentTest extends AttachmentTest {

    protected int attachedId = -1;
    protected int folderId = -1;

    protected int moduleId = -1;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        folderId = getExclusiveWritableFolder();
        attachedId = createExclusiveWritableAttachable(folderId);
        moduleId = getModule();
    }

    public abstract int createExclusiveWritableAttachable(int folderId) throws Exception;

    public abstract int getExclusiveWritableFolder() throws Exception;

    public abstract int getModule() throws Exception;

    protected void doDetach() throws Exception {
        doGet();
        final int id = atm.getCreatedEntities().get(0).getId();
        atm.cleanUp();

        AttachmentMetadata get = atm.get(folderId, attachedId, moduleId, id);
        assertTrue(atm.getLastResponse().hasError());
    }

    protected void doUpdates() throws Exception {
        int objectId = upload();
        Thread.sleep(2000); // Hang around a bit
        AttachmentMetadata get = atm.get(folderId, attachedId, moduleId, objectId);
        assertFalse(atm.getLastResponse().hasError());
        final long timestamp = atm.getLastResponse().getTimestamp().getTime();
        upload();
        upload();
        upload();
        upload();

        final List<AttachmentMetadata> createdLater = new ArrayList<AttachmentMetadata>(atm.getCreatedEntities().subList(1, atm.getCreatedEntities().size()));

        atm.updates(folderId, attachedId, moduleId, new int[] { AttachmentField.ID, AttachmentField.FILENAME }, timestamp);
        assertFalse(atm.getLastResponse().hasError());

        final JSONArray arrayOfArrays = (JSONArray) atm.getLastResponse().getData();
        // Ugly extract of updates in response.
        int updates = 0;
        for (int i = 0; i < arrayOfArrays.length(); i++) {
            if (arrayOfArrays.get(i) instanceof JSONArray) {
                updates++;
            }
        }

        assertEquals(arrayOfArrays.toString() + " were modified later than " + timestamp, createdLater.size(), updates);

        for (int i = 0; i < arrayOfArrays.length(); i++) {
            final Object tmp = arrayOfArrays.get(i);
            if (tmp instanceof JSONArray) {
                final JSONArray values = (JSONArray) tmp;
                final AttachmentMetadata attachment = createdLater.get(i);
                assertEquals(values.getInt(0), attachment.getId());
                assertEquals(testFile.getName(), values.getString(1));
            }
        }

        final List<AttachmentMetadata> copy = new ArrayList<AttachmentMetadata>(atm.getCreatedEntities());
        atm.cleanUp();

        atm.updates(folderId, attachedId, moduleId, new int[] { AttachmentField.ID, AttachmentField.FILENAME }, timestamp);

        final JSONArray arrayOfIds = (JSONArray) atm.getLastResponse().getData();
        // Ugly extract of deletes in response.
        updates = 0;
        for (int i = 0; i < arrayOfIds.length(); i++) {
            if (arrayOfIds.get(i) instanceof Integer) {
                updates++;
            }
        }

        assertEquals(arrayOfIds.toString(), copy.size(), updates);

        for (int i = 0; i < arrayOfIds.length(); i++) {
            final Object tmp = arrayOfIds.get(i);
            if (tmp instanceof Integer) {
                final int id = ((Integer) tmp).intValue();
                final AttachmentMetadata attachment = copy.get(i);
                assertEquals(id, attachment.getId());
            }
        }
    }

    protected void doAll() throws Exception {
        upload();
        upload();
        upload();
        upload();
        upload();

        atm.all(folderId, attachedId, moduleId, new int[] { AttachmentField.ID, AttachmentField.FILENAME }, AttachmentField.CREATION_DATE, Order.ASCENDING);
        assertFalse(atm.getLastResponse().hasError());

        final JSONArray arrayOfArrays = (JSONArray) atm.getLastResponse().getData();
        // Ugly extract of updates in response.
        int updates = 0;
        for (int i = 0; i < arrayOfArrays.length(); i++) {
            if (arrayOfArrays.get(i) instanceof JSONArray) {
                updates++;
            }
        }
        assertTrue(arrayOfArrays.toString(), atm.getCreatedEntities().size() <= updates);
        for (int i = 0; i < atm.getCreatedEntities().size(); i++) {
            final AttachmentMetadata attachment = atm.getCreatedEntities().get(i);

            JSONArray found = null;
            for (int j = 0; null == found && j < arrayOfArrays.length(); j++) {
                final Object tmp = arrayOfArrays.get(j);
                if (tmp instanceof JSONArray) {
                    final JSONArray values = (JSONArray) tmp;
                    if (values.getInt(0) == attachment.getId()) {
                        found = values;
                    }
                }
            }

            assertTrue(null != found);
            assertEquals(testFile.getName(), found.getString(1));
        }
    }

    protected void doList() throws Exception {
        upload();
        upload();
        upload();
        upload();
        upload();

        final int[] ids = new int[] { atm.getCreatedEntities().get(0).getId(), atm.getCreatedEntities().get(2).getId(), atm.getCreatedEntities().get(4).getId() };
        atm.list(folderId, attachedId, moduleId, ids, new int[] { AttachmentField.ID, AttachmentField.FILENAME, AttachmentField.ATTACHED_ID, AttachmentField.MODULE_ID });
        final AbstractAJAXResponse res = atm.getLastResponse();
        assertFalse(res.hasError());
        final JSONArray arrayOfArrays = (JSONArray) res.getData();

        assertEquals(ids.length, arrayOfArrays.length());

        for (int i = 0; i < arrayOfArrays.length(); i++) {
            final JSONArray values = arrayOfArrays.getJSONArray(i);

            assertEquals(ids[i], values.getInt(0));
            assertEquals(testFile.getName(), values.getString(1));
        }
    }

    protected void doGet() throws Exception {
        int objectId = upload();

        AttachmentMetadata get = atm.get(folderId, attachedId, moduleId, objectId);
        assertFalse(atm.getLastResponse().hasError());

        assertEquals(folderId, get.getFolderId());
        assertEquals(attachedId, get.getAttachedId());
        assertEquals(moduleId, get.getModuleId());
        assertEquals(testFile.getName(), get.getFilename());
        assertEquals("text/plain", get.getFileMIMEType());
        assertEquals(testFile.length(), get.getFilesize());
        assertEquals(objectId, get.getId());
    }

    protected void doMultiple() throws Exception {
        int id = upload();
        AttachmentMetadata reloaded = atm.get(folderId, attachedId, moduleId, id);
        assertEquals(reloaded.getFilename(), testFile.getName());
    }

    protected final void assertFilename(final AttachmentMetadata att, final String filename) throws MalformedURLException, JSONException, IOException, OXException {
        AttachmentMetadata get = atm.get(folderId, attachedId, moduleId, att.getId());
        assertFalse(atm.getLastResponse().hasError());
        assertNotNull(get);

        assertEquals(filename, get.getFilename());
    }

    protected void doDocument() throws Exception {
        int objectId = upload();

        String data = null;
        InputStream local = null;

        try {
            data = atm.document(folderId, attachedId, moduleId, objectId);
            OXTestToolkit.assertSameContent(local = new FileInputStream(testFile), new ByteArrayInputStream(data.getBytes()));
        } finally {
            if (local != null) {
                local.close();
            }
        }

        com.openexchange.ajax.attach.actions.GetDocumentRequest request = new com.openexchange.ajax.attach.actions.GetDocumentRequest(folderId, objectId, moduleId, attachedId);
        GetDocumentResponse response = getClient().execute(request);
        assertEquals("application/octet-stream;charset=UTF-8", response.getContentType());

        request = new com.openexchange.ajax.attach.actions.GetDocumentRequest(folderId, objectId, moduleId, attachedId, "application/octet-stream");
        response = getClient().execute(request);
        assertEquals("application/octet-stream;charset=UTF-8", response.getContentType());
    }

    protected void doNotExists() throws Exception {
        final int imaginaryFolderFriend = Integer.MAX_VALUE;
        final int imaginaryObjectFriend = Integer.MAX_VALUE;

        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFolderId(imaginaryFolderFriend);
        attachment.setAttachedId(attachedId);
        attachment.setModuleId(moduleId);

        int objectId = atm.attach(attachment, testFile.getName(), FileUtils.openInputStream(testFile), null);
        assertTrue(atm.getLastResponse().hasError());

        attachment.setFolderId(folderId);
        attachment.setAttachedId(imaginaryObjectFriend);

        atm.attach(attachment, testFile.getName(), FileUtils.openInputStream(testFile), null);
        assertTrue(atm.getLastResponse().hasError());
    }

    protected void doForbidden() throws Exception {
        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFolderId(folderId);
        attachment.setAttachedId(attachedId);
        attachment.setModuleId(moduleId);

        atm.setClient(getClient2());
        int objectId = atm.attach(attachment, testFile.getName(), FileUtils.openInputStream(testFile), null);
        assertTrue(atm.getLastResponse().hasError());

        atm.setClient(getClient());
    }

    protected void doQuota() throws Exception {
        FilestoreQuotaRequest request = new FilestoreQuotaRequest();
        FilestoreQuotaResponse response = getClient().execute(request);
        assertFalse(response.hasError());

        JSONObject quota = (JSONObject) response.getData();
        final int use = quota.getInt("use");

        upload();

        response = getClient().execute(request);
        assertFalse(response.hasError());

        quota = (JSONObject) response.getData();
        final int useAfter = quota.getInt("use");

        AttachmentMetadata get = atm.get(atm.getCreatedEntities().get(0).getFolderId(), atm.getCreatedEntities().get(0).getAttachedId(), atm.getCreatedEntities().get(0).getAttachedId(), atm.getCreatedEntities().get(0).getId());
        assertFalse(atm.getLastResponse().hasError());

        assertEquals(useAfter - use, ((JSONObject) atm.getLastResponse().getData()).get("file_size"));
    }

    public int upload() throws Exception {
        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFolderId(folderId);
        attachment.setAttachedId(attachedId);
        attachment.setModuleId(moduleId);

        int objectId = atm.attach(attachment, testFile.getName(), FileUtils.openInputStream(testFile), "text/plain");

        assertFalse(atm.getLastResponse().hasError());

        return objectId;
    }

    public AttachmentMetadata getAttachment(final int index) {
        return atm.getCreatedEntities().get(index);
    }

    public File getTestFile() {
        return testFile;
    }

}
