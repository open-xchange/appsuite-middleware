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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AttachmentTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.tasks.TestTask;
import com.openexchange.test.OXTestToolkit;

public abstract class AbstractAttachmentTest extends AttachmentTest {

    protected int attachedId = -1;
    protected int folderId = -1;

    protected int moduleId = -1;

    protected String sessionId;
    private Response res;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        sessionId = getSessionId();

        folderId = getExclusiveWritableFolder(sessionId);
        attachedId = createExclusiveWritableAttachable(sessionId, folderId);

        moduleId = getModule();
    }

    @After
    public void tearDown() throws Exception {
        try {
            removeAttachments();
            removeAttachable(folderId, attachedId, sessionId);
        } finally {
            super.tearDown();
        }
    }

    public abstract int createExclusiveWritableAttachable(String sessionId, int folderId) throws Exception;

    public abstract int getExclusiveWritableFolder(String sessionId) throws Exception;

    public abstract void removeAttachable(int folder, int id, String sessionId) throws Exception;

    public abstract int getModule() throws Exception;

    protected void doDetach() throws Exception {
        doGet();
        final int id = clean.get(0).getId();
        removeAttachments();

        AttachmentMetadata get = atm.get(folderId, attachedId, moduleId, id);
        assertTrue(atm.getLastResponse().hasError());
    }

    protected void doUpdates() throws Exception {
        upload();
        Thread.sleep(2000); // Hang around a bit
        AttachmentMetadata get = atm.get(folderId, attachedId, moduleId, clean.get(0).getId());
        assertFalse(atm.getLastResponse().hasError());
        final long timestamp = atm.getLastResponse().getTimestamp().getTime();
        upload();
        upload();
        upload();
        upload();

        final List<AttachmentMetadata> createdLater = new ArrayList<AttachmentMetadata>(clean.subList(1, clean.size()));

        res = updates(getWebConversation(), sessionId, folderId, attachedId, moduleId, timestamp, new int[] { AttachmentField.ID, AttachmentField.FILENAME }, AttachmentField.CREATION_DATE, "ASC");

        assertNoError(res);
        final JSONArray arrayOfArrays = (JSONArray) res.getData();
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

        final List<AttachmentMetadata> copy = new ArrayList<AttachmentMetadata>(clean);
        removeAttachments();

        res = updates(getWebConversation(), sessionId, folderId, attachedId, moduleId, timestamp, new int[] { AttachmentField.ID, AttachmentField.FILENAME }, AttachmentField.CREATION_DATE, "ASC");

        final JSONArray arrayOfIds = (JSONArray) res.getData();
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

        final Response res = all(getWebConversation(), sessionId, folderId, attachedId, moduleId, new int[] { AttachmentField.ID, AttachmentField.FILENAME }, AttachmentField.CREATION_DATE, "ASC");
        assertNoError(res);
        final JSONArray arrayOfArrays = (JSONArray) res.getData();
        // Ugly extract of updates in response.
        int updates = 0;
        for (int i = 0; i < arrayOfArrays.length(); i++) {
            if (arrayOfArrays.get(i) instanceof JSONArray) {
                updates++;
            }
        }
        assertTrue(arrayOfArrays.toString(), clean.size() <= updates);
        for (int i = 0; i < clean.size(); i++) {
            final AttachmentMetadata attachment = clean.get(i);

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

        final int[] ids = new int[] { clean.get(0).getId(), clean.get(2).getId(), clean.get(4).getId()
        };

        final Response res = list(getWebConversation(), sessionId, folderId, attachedId, moduleId, ids, new int[] { AttachmentField.ID, AttachmentField.FILENAME });
        assertNoError(res);
        final JSONArray arrayOfArrays = (JSONArray) res.getData();

        assertEquals(ids.length, arrayOfArrays.length());

        for (int i = 0; i < arrayOfArrays.length(); i++) {
            final JSONArray values = arrayOfArrays.getJSONArray(i);

            assertEquals(ids[i], values.getInt(0));
            assertEquals(testFile.getName(), values.getString(1));
        }
    }

    protected void doGet() throws Exception {
        upload();
        
        AttachmentMetadata get = atm.get(folderId, attachedId, moduleId, clean.get(0).getId());
        assertFalse(atm.getLastResponse().hasError());

        assertEquals(folderId, get.getFolderId());
        assertEquals(attachedId, get.getAttachedId());
        assertEquals(moduleId, get.getModuleId());
        assertEquals(testFile.getName(), get.getFilename());
        assertEquals("text/plain", get.getFileMIMEType());
        assertEquals(testFile.length(), get.getFilesize());
        assertEquals(clean.get(0).getId(), get.getId());
    }

    protected void doMultiple() throws Exception {
        TestTask attachment = new TestTask();
        int id = atm.attach(attachment, testFile.getName(), new FileInputStream(testFile), null);
        AbstractAJAXResponse resp = atm.getLastResponse();
        assertFalse(resp.hasError());

        AttachmentMetadata reloaded = atm.get(folderId, attachedId, AttachmentTools.determineModule(attachment), id);
        assertEquals(reloaded.getFilename(), testFile.getName());
    }

    protected final void assertFilename(final AttachmentMetadata att, final String filename) throws MalformedURLException, JSONException, IOException, OXException {
        AttachmentMetadata get = atm.get(folderId, attachedId, moduleId, att.getId());
        assertFalse(atm.getLastResponse().hasError());
        assertNotNull(get);

        assertEquals(filename, get.getFilename());
    }

    protected void doDocument() throws Exception {
        upload();

        InputStream data = null;
        InputStream local = null;

        try {
            data = document(getWebConversation(), sessionId, folderId, attachedId, moduleId, clean.get(0).getId());
            OXTestToolkit.assertSameContent(local = new FileInputStream(testFile), data);
        } finally {
            if (data != null) {
                data.close();
            }
            if (local != null) {
                local.close();
            }
        }

        GetMethodWebRequest req = documentRequest(sessionId, folderId, attachedId, moduleId, clean.get(0).getId(), null);
        WebResponse resp = getWebConversation().getResource(req);
        assertEquals("application/octet-stream", resp.getContentType());

        req = documentRequest(sessionId, folderId, attachedId, moduleId, clean.get(0).getId(), "application/octet-stream");
        resp = getWebConversation().getResource(req);
        assertEquals("application/octet-stream", resp.getContentType());

    }

    protected void doNotExists() throws Exception {
        final int imaginaryFolderFriend = Integer.MAX_VALUE;
        final int imaginaryObjectFriend = Integer.MAX_VALUE;

        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFolderId(imaginaryFolderFriend);
        attachment.setAttachedId(attachedId);
        attachment.setModuleId(moduleId);

        Response res = attach(getWebConversation(), sessionId, imaginaryFolderFriend, attachedId, moduleId, testFile);
        assertTrue(res.hasError());

        attachment.setFolderId(folderId);
        attachment.setAttachedId(imaginaryObjectFriend);

        res = attach(getWebConversation(), sessionId, imaginaryFolderFriend, attachedId, moduleId, testFile);
        assertTrue(res.hasError());
    }

    protected void doForbidden() throws Exception {
        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFolderId(folderId);
        attachment.setAttachedId(attachedId);
        attachment.setModuleId(moduleId);

        final Response res = attach(getSecondWebConversation(), getSecondSessionId(), folderId, attachedId, moduleId, testFile);
        assertTrue(res.hasError());

    }

    protected void doQuota() throws Exception {
        Response res = quota(getWebConversation(), sessionId);
        assertNoError(res);
        JSONObject quota = (JSONObject) res.getData();
        final int use = quota.getInt("use");

        upload();

        res = quota(getWebConversation(), sessionId);
        assertNoError(res);
        quota = (JSONObject) res.getData();
        final int useAfter = quota.getInt("use");

        AttachmentMetadata get = atm.get(clean.get(0).getFolderId(), clean.get(0).getAttachedId(), clean.get(0).getAttachedId(), clean.get(0).getId());
        assertFalse(atm.getLastResponse().hasError());

        assertEquals(useAfter - use, ((JSONObject) res.getData()).get("file_size"));
    }

    protected void doDatasource() throws MalformedURLException, JSONException, IOException, SAXException, OXException {
        // Action attach in a regular PUT may contain a datasource field
        // Note that POST must always contain a file upload field and may never contain a datasource field. Maybe, anyway. Whatever.

        final Map<String, Object> datasourceDefinition = new HashMap<String, Object>();

        datasourceDefinition.put("identifier", "com.openexchange.url.mail.attachment");
        datasourceDefinition.put("url", "http://one-finger-salute.org/img/middle_finger.png");

        Response res = attach(getWebConversation(), sessionId, folderId, attachedId, moduleId, datasourceDefinition);

        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFolderId(folderId);
        attachment.setAttachedId(attachedId);
        attachment.setModuleId(moduleId);
        {
            final OXException exception = res.getException();
            if (null != exception) {
                final StringWriter writer = new StringWriter(512);
                exception.printStackTrace(new PrintWriter(writer));
                // Let it fail...
                assertNull("An exception occurred: " + writer.toString(), exception);
            }
        }
        final Integer data = (Integer) res.getData();
        assertNotNull("Response's data is null.", data);
        attachment.setId(data.intValue());
        clean.add(attachment);

        AttachmentMetadata get = atm.get(folderId, attachedId, moduleId, clean.get(0).getId());
        assertFalse(atm.getLastResponse().hasError()); // Good enough
    }

    public void upload() throws Exception {
        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFolderId(folderId);
        attachment.setAttachedId(attachedId);
        attachment.setModuleId(moduleId);

        final Response res = attach(getWebConversation(), sessionId, folderId, attachedId, moduleId, testFile);
        assertNoError(res);

        attachment.setId(((JSONArray) res.getData()).getInt(0));
        clean.add(attachment);
    }

    public AttachmentMetadata getAttachment(final int index) {
        return clean.get(index);
    }

    public File getTestFile() {
        return testFile;
    }

}
