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

import static org.junit.Assert.*;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.TestInit;

public class LockTest extends InfostoreAJAXTest {

    private InfostoreTestManager itm2;
    private FolderTestManager ftm2;
    private com.openexchange.file.storage.File file;
    private FolderObject folder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        itm2 = new InfostoreTestManager(getClient2());
        ftm2 = new FolderTestManager(getClient2());
        /*
         * create folder shared to user 2 and a file inside the folder
         */
        folder = FolderTestManager.createNewFolderObject(
            UUID.randomUUID().toString(), FolderObject.INFOSTORE, FolderObject.PUBLIC, getClient().getValues().getUserId(), folderId);
        OCLPermission permission1 = FolderTestManager.createPermission(
            getClient().getValues().getUserId(), false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, true);
        OCLPermission permission2 = FolderTestManager.createPermission(
            getClient2().getValues().getUserId(), false, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS, false);
        folder.setPermissionsAsArray(new OCLPermission[] { permission1, permission2 });
        folder = ftm.insertFolderOnServer(folder);
        /*
         * create a file with multiple versions in the folder
         */
        File testFile = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
        file = itm.createFileOnServer(folder.getObjectID(), "test.bin", "application/octet-stream");
        for (int i = 0; i < 5; i++) {
            itm.updateAction(file, testFile, new com.openexchange.file.storage.File.Field[] {}, itm.getLastResponse().getTimestamp());
            assertFalse(itm.getLastResponse().hasError());
        }
    }

    @Test
    public void testLock() throws Exception {
        String id = file.getId();
        com.openexchange.file.storage.File file = itm.getAction(id);
        Date lastModified = file.getLastModified();

        itm.lock(id);
        assertFalse(itm.getLastResponse().hasError());
        assertNotNull(itm.getLastResponse().getTimestamp());

        file = itm.getAction(id);
        assertFalse(itm.getLastResponse().hasError());
        assertLocked((JSONObject) itm.getLastResponse().getData());
        lastModified = file.getLastModified();

        // BUG 4232
        itm.updateAction(file, new Field[] { Field.ID }, new Date(lastModified.getTime()));

        String idFromUpdate = (String) itm.getLastResponse().getData();
        assertEquals(id, idFromUpdate);

        // Object may not be modified
        file.setTitle("Hallo");
        itm2.updateAction(file, new Field[] { Field.ID, Field.TITLE }, new Date(Long.MAX_VALUE));
        assertTrue(itm2.getLastResponse().hasError());

        // Object may not be moved
        file.setFolderId(Integer.toString(getClient2().getValues().getPrivateInfostoreFolder()));
        itm2.updateAction(file, new Field[] { Field.ID, Field.FOLDER_ID }, new Date(Long.MAX_VALUE));
        assertTrue(itm2.getLastResponse().hasError());

        // Object may not be removed
        itm2.deleteAction(Collections.singletonList(id), Collections.singletonList(String.valueOf(folder.getObjectID())), new Date(Long.MAX_VALUE));
        assertTrue(itm2.getLastResponse().hasError());

        // Versions may not be removed
        int[] notDetached = ftm2.detach(id, new Date(Long.MAX_VALUE), new int[] { 4 });
        assertEquals(1, notDetached.length);
        assertEquals(4, notDetached[0]);

        // Object may not be locked
        itm2.lock(id);
        assertTrue(itm2.getLastResponse().hasError());

        // Object may not be unlocked
        itm2.unlock(id);
        assertTrue(itm2.getLastResponse().hasError());

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
        final String id = file.getId();
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

        // Object may be locked now
        itm2.lock(id);
        assertFalse(itm2.getLastResponse().hasError());

        // Object may not be modified
        file.setTitle("Hallo");
        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE }, new Date(Long.MAX_VALUE));
        assertTrue(itm.getLastResponse().hasError());

        // Owner may unlock
        itm2.unlock(id);
        assertFalse(itm2.getLastResponse().hasError());
        file = itm.getAction(id);
        assertUnlocked(file);
    }

    public static void assertLocked(final com.openexchange.file.storage.File file) {
        final long locked = file.getLockedUntil().getTime();
        assertFalse("This must be != 0: " + locked, 0 == locked);
    }

    public static void assertUnlocked(final com.openexchange.file.storage.File file) {
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
