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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestInit;
import com.openexchange.tools.URLParameter;

public class LockTest extends InfostoreAJAXTest {

    protected File testFile;

    public LockTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        testFile = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        sessionId = getSessionId();
        // Copied-without-thinking from FolderTest
        final int userId = getClient().getValues().getUserId();
        final int secondUserId = getClient2().getValues().getUserId();
        // TODO create folder in one step with correct permissions.
        folderId = new FolderObject("NewInfostoreFolder" + System.currentTimeMillis(), getClient().getValues().getPrivateInfostoreFolder(), Module.INFOSTORE.getFolderConstant(), FolderObject.PUBLIC, userId).getObjectID();
        updateFolder(getWebConversation(), getHostName(), sessionId, userId, secondUserId, folderId, Long.MAX_VALUE);

        com.openexchange.file.storage.File data = createFile(folderId, "test upload");
        data.setFileMIMEType("text/plain");
        itm.newAction(data, testFile);

        String c = data.getId();
        clean.add(c);

        Response res = this.update(getWebConversation(), getHostName(), sessionId, c, Long.MAX_VALUE, m(), testFile, "text/plain");
        assertNoError(res);

        res = this.update(getWebConversation(), getHostName(), sessionId, c, Long.MAX_VALUE, m(), testFile, "text/plain");
        assertNoError(res);

        res = this.update(getWebConversation(), getHostName(), sessionId, c, Long.MAX_VALUE, m(), testFile, "text/plain");
        assertNoError(res);

        res = this.update(getWebConversation(), getHostName(), sessionId, c, Long.MAX_VALUE, m(), testFile, "text/plain");
        assertNoError(res);

    }

    @After
    public void tearDown() throws Exception {
        try {
            ftm.deleteFolderOnServer(folderId, new Date(Long.MAX_VALUE));
        } finally {
            super.tearDown();
        }
    }

    //FIXME MS re-add later
    //    @Test
    //    public void testLock() throws Exception {
    //
    //        Response res = get(getWebConversation(), getHostName(), sessionId, clean.get(0), -1);
    //        final Date ts = res.getTimestamp();
    //
    //        res = lock(getWebConversation(), getHostName(), sessionId, clean.get(0));
    //        assertNoError(res);
    //        assertNotNull(res.getTimestamp());
    //
    //        res = get(getWebConversation(), getHostName(), sessionId, clean.get(0), -1);
    //        assertNoError(res);
    //        assertLocked((JSONObject) res.getData());
    //
    //        // BUG 4232
    //
    //        res = updates(getWebConversation(), getHostName(), sessionId, folderId, new int[] { Metadata.ID }, ts.getTime());
    //
    //        final JSONArray modAndDel = (JSONArray) res.getData();
    //        final JSONArray mod = modAndDel.getJSONArray(0);
    //
    //        assertEquals(1, mod.length());
    //        assertEquals(clean.get(0), mod.getString(0));
    //
    //        final String sessionId2 = this.getSecondSessionId();
    //
    //        // Object may not be modified
    //        res = update(getSecondWebConversation(), getHostName(), sessionId2, clean.get(0), Long.MAX_VALUE, m("title", "Hallo"));
    //        assertTrue(res.hasError());
    //
    //        // Bug #????
    //        // Object may not be moved
    //
    //        final int userId2 = getClient2().getValues().getUserId();
    //        final int folderId2 = getClient2().getValues().getPrivateInfostoreFolder();
    //
    //        res = update(getSecondWebConversation(), getHostName(), sessionId2, clean.get(0), Long.MAX_VALUE, m("folder_id", "" + folderId2));
    //        assertTrue(res.hasError());
    //
    //        // Object may not be removed
    //        JSONObject response = deleteGetResponse(getSecondWebConversation(), null, getHostName(), sessionId2, Long.MAX_VALUE, new String[][] { { String.valueOf(folderId), clean.get(0) } });
    //        assertTrue(response.has("error"));
    //
    //        // Versions may not be removed
    //        int[] notDetached = detach(getSecondWebConversation(), getHostName(), sessionId2, Long.MAX_VALUE, clean.get(0), new int[] { 4 });
    //        assertEquals(1, notDetached.length);
    //        assertEquals(4, notDetached[0]);
    //
    //        // Object may not be locked
    //        res = lock(getSecondWebConversation(), getHostName(), sessionId2, clean.get(0));
    //        assertTrue(res.hasError());
    //
    //        // Object may not be unlocked
    //
    //        res = unlock(getSecondWebConversation(), getHostName(), sessionId2, clean.get(0));
    //        assertTrue(res.hasError());
    //
    //        // Lock owner may update
    //        res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("title", "Hallo"));
    //        assertNoError(res);
    //
    //        res = get(getWebConversation(), getHostName(), sessionId, clean.get(0), -1);
    //        final JSONObject o = (JSONObject) res.getData();
    //
    //        assertEquals("Hallo", o.get("title"));
    //
    //        //Lock owner may detach
    //        notDetached = detach(getWebConversation(), getHostName(), sessionId, Long.MAX_VALUE, clean.get(0), new int[] { 4 });
    //        assertEquals(0, notDetached.length);
    //
    //        //Lock owner may remove
    //        String[] notDeleted = delete(getWebConversation(), getHostName(), sessionId, Long.MAX_VALUE, new String[][] { { String.valueOf(folderId), clean.get(0) } });
    //        assertEquals(0, notDeleted.length);
    //        clean.remove(0);
    //
    //    }

    @Test
    public void testUnlock() throws Exception {
        Response res = lock(getWebConversation(), getHostName(), sessionId, clean.get(0));
        assertNoError(res);

        com.openexchange.file.storage.File file = itm.getAction(clean.get(0));
        assertLocked(file);

        // Lock owner may relock
        res = lock(getWebConversation(), getHostName(), sessionId, clean.get(0));
        assertNoError(res);

        // Lock owner may unlock (duh!)
        res = unlock(getWebConversation(), getHostName(), sessionId, clean.get(0));
        assertNoError(res);
        assertNotNull(res.getTimestamp());

        file = itm.getAction(clean.get(0));
        assertUnlocked(file);

        final String sessionId2 = getSecondSessionId();

        res = lock(getSecondWebConversation(), getHostName(), sessionId2, clean.get(0));
        assertNoError(res);

        file.setTitle("Hallo");
        UpdateInfostoreResponse result = update(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE }, Long.MAX_VALUE);
        assertTrue(result.hasError());

        // Owner may unlock
        res = unlock(getWebConversation(), getHostName(), sessionId, clean.get(0));
        assertNoError(res);

        file = itm.getAction(clean.get(0));
        assertUnlocked(file);

    }

    public static void assertLocked(final com.openexchange.file.storage.File file) throws JSONException {
        final long locked = file.getLockedUntil().getTime();
        assertFalse("This must be != 0: " + locked, 0 == locked);
    }

    public static void assertUnlocked(final com.openexchange.file.storage.File file) throws JSONException {
        assertNull(file.getLockedUntil());
    }

    // Copied from FolderTest

    public static boolean updateFolder(final WebConversation conversation, final String hostname, final String sessionId, final int entity, final int secondEntity, final int folderId, final long timestamp) throws JSONException, MalformedURLException, IOException, SAXException {
        final JSONObject jsonFolder = new JSONObject();
        jsonFolder.put("id", folderId);
        final JSONArray perms = new JSONArray();
        JSONObject jsonPermission = new JSONObject();
        jsonPermission.put("entity", entity);
        jsonPermission.put("group", false);
        jsonPermission.put("bits", createPermissionBits(8, 8, 8, 8, true));
        perms.put(jsonPermission);
        jsonPermission = new JSONObject();
        jsonPermission.put("entity", secondEntity);
        jsonPermission.put("group", false);
        jsonPermission.put("bits", createPermissionBits(8, 8, 8, 8, true));
        perms.put(jsonPermission);
        jsonFolder.put("permissions", perms);
        final URLParameter urlParam = new URLParameter();
        urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
        urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        urlParam.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(folderId));
        urlParam.setParameter("timestamp", String.valueOf(timestamp));
        final byte[] bytes = jsonFolder.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(), bais, "text/javascript; charset=UTF-8");
        final WebResponse resp = conversation.getResponse(req);
        final JSONObject respObj = new JSONObject(resp.getText());
        if (respObj.has("error")) {
            return false;
        }
        return true;
    }

    private static int createPermissionBits(final int fp, final int orp, final int owp, final int odp, final boolean adminFlag) {
        final int[] perms = new int[5];
        perms[0] = fp;
        perms[1] = orp;
        perms[2] = owp;
        perms[3] = odp;
        perms[4] = adminFlag ? 1 : 0;
        return createPermissionBits(perms);
    }

    private static int createPermissionBits(final int[] permission) {
        int retval = 0;
        boolean first = true;
        for (int i = permission.length - 1; i >= 0; i--) {
            final int exponent = (i * 7); // Number of bits to be shifted
            if (first) {
                retval += permission[i] << exponent;
                first = false;
            } else {
                if (permission[i] == OCLPermission.ADMIN_PERMISSION) {
                    retval += Folder.MAX_PERMISSION << exponent;
                } else {
                    retval += mapping[permission[i]] << exponent;
                }
            }
        }
        return retval;
    }

    private static final int[] mapping = { 0, -1, 1, -1, 2, -1, -1, -1, 4 };

    private static final String FOLDER_URL = "/ajax/folders";

}
