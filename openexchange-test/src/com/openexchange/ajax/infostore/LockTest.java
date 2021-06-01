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

package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.test.TestInit;

public class LockTest extends InfostoreAJAXTest {

    private InfostoreTestManager itm2;
    private FolderTestManager ftm2;
    private com.openexchange.file.storage.File file;
    private FolderObject folder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        itm2 = new InfostoreTestManager(testUser2.getAjaxClient());
        ftm2 = new FolderTestManager(testUser2.getAjaxClient());
        /*
         * create folder shared to user 2 and a file inside the folder
         */
        folder = FolderTestManager.createNewFolderObject(
            UUID.randomUUID().toString(), FolderObject.INFOSTORE, FolderObject.PUBLIC, getClient().getValues().getUserId(), folderId);
        OCLPermission permission1 = FolderTestManager.createPermission(
            getClient().getValues().getUserId(), false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, true);
        OCLPermission permission2 = FolderTestManager.createPermission(
            testUser2.getAjaxClient().getValues().getUserId(), false, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS, false);
        folder.setPermissionsAsArray(new OCLPermission[] { permission1, permission2 });
        folder = ftm.insertFolderOnServer(folder);
        /*
         * create a file with multiple versions in the folder
         */
        File testFile = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
        file = itm.createFileOnServer(folder.getObjectID(), "test.bin", "application/octet-stream");
        assertFalse(itm.getLastResponse().getErrorMessage(), itm.getLastResponse().hasError());
        for (int i = 0; i < 5; i++) {
            itm.updateAction(file, testFile, new com.openexchange.file.storage.File.Field[] {}, itm.getLastResponse().getTimestamp());
            checkForError(itm);
        }
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }


    @Test
    public void testLock() throws Exception {
        String id = file.getId();
        com.openexchange.file.storage.File file = itm.getAction(id);

        itm.lock(id);
        checkForError(itm);
        Date clientTimestamp = itm.getLastResponse().getTimestamp();

        file = itm.getAction(id);
        checkForError(itm);
        assertLocked((JSONObject) itm.getLastResponse().getData());

        // BUG 4232
        itm.updateAction(file, new Field[] { Field.ID }, clientTimestamp);

        String idFromUpdate = (String) itm.getLastResponse().getData();
        assertEquals(id, idFromUpdate);

        // Object may not be modified
        file.setTitle("Hallo");
        itm2.updateAction(file, new Field[] { Field.ID, Field.TITLE }, new Date(Long.MAX_VALUE));
        assertTrue(itm2.getLastResponse().hasError());

        // Object may not be moved
        file.setFolderId(Integer.toString(testUser2.getAjaxClient().getValues().getPrivateInfostoreFolder()));
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
        checkForError(itm);
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
        checkForError(itm);

        com.openexchange.file.storage.File file = itm.getAction(id);
        assertLocked(file);

        // Lock owner may relock
        itm.lock(id);
        checkForError(itm);

        // Lock owner may unlock (duh!)
        itm.unlock(id);
        checkForError(itm);
        assertNotNull(itm.getLastResponse().getTimestamp());
        file = itm.getAction(id);
        assertUnlocked(file);

        // Object may be locked now
        itm2.lock(id);
        checkForError(itm2);

        // Object may not be modified
        file.setTitle("Hallo");
        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE }, new Date(Long.MAX_VALUE));
        assertTrue(itm.getLastResponse().hasError());

        // Owner may unlock
        itm2.unlock(id);
        checkForError(itm2);
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

    /**
     * Checks that the last response of the given {@link InfostoreTestManager} doesn't have an error
     */
    private void checkForError(InfostoreTestManager itm) {
        assertFalse("Unexpected error: " + itm.getLastResponse().getErrorMessage(), itm.getLastResponse().hasError());
    }

}
