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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.google.common.collect.Iterables;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.TestInit;
import edu.emory.mathcs.backport.java.util.Collections;

public class LockTest extends InfostoreAJAXTest {

    protected File testFile;

    public LockTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        testFile = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        sessionId = getSessionId();
        // Copied-without-thinking from FolderTest
        final int userId = getClient().getValues().getUserId();
        final int secondUserId = getClient2().getValues().getUserId();
        // TODO create folder in one step with correct permissions.
        FolderObject folder = FolderTestManager.createNewFolderObject("NewInfostoreFolder" + System.currentTimeMillis(), Module.INFOSTORE.getFolderConstant(), FolderObject.PUBLIC, userId, getClient().getValues().getPrivateInfostoreFolder());
        folderId = ftm.insertFolderOnServer(folder).getObjectID();
        
        folder.addPermission(new OCLPermission(secondUserId, folderId));
        ftm.updateFolderOnServer(folder);

        com.openexchange.file.storage.File data = createFile(folderId, "test upload");
        data.setFileMIMEType("text/plain");
        itm.newAction(data, testFile);

        String c = data.getId();

        com.openexchange.file.storage.File org = itm.getAction(c);
        itm.updateAction(org, testFile, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        itm.updateAction(org, testFile, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        itm.updateAction(org, testFile, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        itm.updateAction(org, testFile, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());
    }

    @Test
    public void testLock() throws Exception {
        final String id = Iterables.get(itm.getCreatedEntities(), 0).getId();
        itm.getAction(id);

        itm.lock(id);
        assertFalse(itm.getLastResponse().hasError());
        assertNotNull(itm.getLastResponse().getTimestamp());

        com.openexchange.file.storage.File file = itm.getAction(id);
        Date ts = itm.getLastResponse().getTimestamp();
        assertFalse(itm.getLastResponse().hasError());
        assertLocked((JSONObject) itm.getLastResponse().getData());

        // BUG 4232
        itm.updateAction(file, new Field[] { Field.ID }, new Date(ts.getTime()));

        final JSONArray modAndDel = (JSONArray) itm.getLastResponse().getData();
        final JSONArray mod = modAndDel.getJSONArray(0);

        assertEquals(1, mod.length());
        assertEquals(id, mod.getString(0));

        itm.setClient(getClient2());
        // Object may not be modified
        file.setTitle("Hallo");
        itm.updateAction(file, new Field[] { Field.ID, Field.TITLE }, new Date(Long.MAX_VALUE));
        assertTrue(itm.getLastResponse().hasError());

        // Bug #????
        // Object may not be moved

        final int userId2 = getClient2().getValues().getUserId();
        final int folderId2 = getClient2().getValues().getPrivateInfostoreFolder();
        itm.updateAction(file, new Field[] { Field.ID, Field.FOLDER_ID }, new Date(Long.MAX_VALUE));
        assertTrue(itm.getLastResponse().hasError());

        // Object may not be removed
        itm.deleteAction(Collections.singletonList(id), Collections.singletonList(folderId), new Date(Long.MAX_VALUE));
        assertTrue(itm.getLastResponse().hasError());

        // Versions may not be removed
        ftm.setClient(getClient2());
        int[] notDetached = ftm.detach(id, new Date(Long.MAX_VALUE), new int[] { 4 });
        assertEquals(1, notDetached.length);
        assertEquals(4, notDetached[0]);

        itm.lock(id);
        // Object may not be locked
        assertTrue(itm.getLastResponse().hasError());

        // Object may not be unlocked
        itm.unlock(id);
        assertTrue(itm.getLastResponse().hasError());

        itm.setClient(getClient());
        // Lock owner may update
        file.setTitle("Hallo");
        itm.updateAction(file, new Field[] { Field.ID, Field.TITLE }, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        com.openexchange.file.storage.File reload = itm.getAction(id);

        assertEquals("Hallo", reload.getTitle());

        //Lock owner may detach
        ftm.setClient(getClient());
        notDetached = ftm.detach(id, new Date(Long.MAX_VALUE), new int[] { 4 });
        assertEquals(0, notDetached.length);
    }

    @Test
    public void testUnlock() throws Exception {
        final String id = Iterables.get(itm.getCreatedEntities(), 0).getId();
        itm.lock(id);
        assertFalse(itm.getLastResponse().hasError());

        com.openexchange.file.storage.File file = itm.getAction(id);
        assertLocked(file);

        // Lock owner may relock
        itm.lock(id);
        assertFalse(itm.getLastResponse().hasError());

        // Lock owner may unlock (duh!)
        itm.unlock(id);
        assertFalse(itm.getLastResponse().hasError());
        assertNotNull(itm.getLastResponse().getTimestamp());

        file = itm.getAction(id);
        assertUnlocked(file);

        itm.setClient(getClient2());
        itm.lock(id);
        assertFalse(itm.getLastResponse().hasError());

        file.setTitle("Hallo");
        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE }, new Date(Long.MAX_VALUE));
        assertTrue(itm.getLastResponse().hasError());

        // Owner may unlock
        itm.setClient(getClient());
        itm.unlock(id);
        assertFalse(itm.getLastResponse().hasError());

        file = itm.getAction(id);
        assertUnlocked(file);
    }

    public static void assertLocked(final com.openexchange.file.storage.File file) throws JSONException {
        final long locked = file.getLockedUntil().getTime();
        assertFalse("This must be != 0: " + locked, 0 == locked);
    }

    public static void assertUnlocked(final com.openexchange.file.storage.File file) throws JSONException {
        assertNull(file.getLockedUntil());
    }

    public static void assertLocked(final JSONObject o) throws JSONException {
        final long locked = o.getInt(Metadata.LOCKED_UNTIL_LITERAL.getName());
        assertFalse("This must be != 0: " + locked, 0 == locked);
    }

    public static void assertUnlocked(final JSONObject o) throws JSONException {
        assertEquals(0, o.getInt(Metadata.LOCKED_UNTIL_LITERAL.getName()));
    }

}
