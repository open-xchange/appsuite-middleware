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

package com.openexchange.ajax.share.tests;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.AnonymousRecipient;


/**
 * Anonymous guests are created for creating links to folders/items. At most one link must exist
 * per folder or item. This implies that once created anonymous guests must not be re-used and added
 * as permission entities to files or items others than their initial one. I.e. there must be one-to-one
 * relationships between folder/item, anonymous guests and folder/object permissions.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AnonymousGuestTest extends ShareTest {

    private FolderObject folder;

    private File file;

    /**
     * Initializes a new {@link AnonymousGuestTest}.
     * @param name
     */
    public AnonymousGuestTest(String name) {
        super(name);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder());
        remember(folder);
        file = insertFile(folder.getObjectID());
        remember(file);
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testAddAnonymousGuestToFolder() throws Exception {
        OCLGuestPermission guestPermission = createAnonymousGuestPermission();
        FolderObject updated = addPermissions(folder, guestPermission);
        OCLPermission matchingPermission = findAndCheckPermission(updated);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OX_NEW, FolderObject.INFOSTORE, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
    }

    @Test
    public void testAddAnonymousGuestWithInvalidPermissionToFolder() throws Exception {
        /*
         * Permission bits are ignored for anonymous guests, therefore the update should succeed
         * and the created permission will be read-only
         */
        OCLGuestPermission guestPermission = createInvalidAnonymousGuestPermission();

        FolderObject updated = addPermissions(folder, guestPermission);
        OCLPermission matchingPermission = findAndCheckPermission(updated);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OX_NEW, FolderObject.INFOSTORE, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
    }

    @Test
    public void testAddTwoAnonymousGuestsToFolder() throws Exception {
        /*
         * Only one anonymous permission is allowed, so the update must fail.
         */
        OCLGuestPermission guestPermission1 = createAnonymousGuestPermission();
        OCLGuestPermission guestPermission2 = createAnonymousGuestPermission();

        boolean thrown = false;
        try {
            addPermissions(folder, guestPermission1, guestPermission2);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("PERMISSION_DENIED"));
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testAddTwoAnonymousGuestsSubsequentlyToFolder() throws Exception {
        /*
         * Only one anonymous permission is allowed, so the second update must fail.
         */
        OCLGuestPermission guestPermission1 = createAnonymousGuestPermission();
        FolderObject updated = addPermissions(folder, guestPermission1);
        OCLGuestPermission guestPermission2 = createAnonymousGuestPermission();
        boolean thrown = false;
        try {
            addPermissions(updated, guestPermission2);
        } catch (AssertionError e) {
            Assert.assertTrue(e.getMessage().contains("PERMISSION_DENIED"));
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testAddExistingAnonymousGuestToNewFolder() throws Exception {
        OCLGuestPermission guestPermission = createAnonymousGuestPermission();
        FolderObject updated = addPermissions(folder, guestPermission);
        OCLPermission entityPermission = findAndCheckPermission(updated);

        FolderObject newFolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder());
        remember(newFolder);

        boolean thrown = false;
        try {
            addPermissions(newFolder, entityPermission);
        } catch (AssertionError e) {
            Assert.assertTrue(e.getMessage().contains("PERMISSION_DENIED"));
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testUpdateAnonymousGuestPermissionToWritableOnFolder() throws Exception {
        OCLGuestPermission guestPermission = createAnonymousGuestPermission();
        FolderObject updated = addPermissions(folder, guestPermission);
        OCLPermission entityPermission = findAndCheckPermission(updated);
         /*
          * Change to writable
          */
        entityPermission.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        boolean thrown = false;
        try {
            updateFolder(EnumAPI.OX_NEW, updated);
        } catch (AssertionError e) {
            Assert.assertTrue(e.getMessage().contains("PERMISSION_DENIED"));
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testAddAnonymousGuestToFile() throws Exception {
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(createAnonymousGuestPermission());

        File updated = addPermissions(file, guestPermission);
        FileStorageObjectPermission matchingPermission = findAndCheckPermission(updated);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getFolderId(), file.getId(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
    }

    @Test
    public void testAddAnonymousGuestWithInvalidPermissionToFile() throws Exception {
        /*
         * Permission bits are ignored for anonymous guests, therefore the update should succeed
         * and the created permission will be read-only
         */
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(createInvalidAnonymousGuestPermission());

        File updated = addPermissions(file, guestPermission);
        FileStorageObjectPermission matchingPermission = findAndCheckPermission(updated);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getFolderId(), file.getId(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
    }

    @Test
    public void testAddTwoAnonymousGuestsToFile() throws Exception {
        /*
         * Only one anonymous permission is allowed, so the update must fail.
         */
        FileStorageGuestObjectPermission guestPermission1 = asObjectPermission(createAnonymousGuestPermission());
        FileStorageGuestObjectPermission guestPermission2 = asObjectPermission(createAnonymousGuestPermission());

        boolean thrown = false;
        try {
            addPermissions(file, guestPermission1, guestPermission2);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("PERMISSION_DENIED"));
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testAddTwoAnonymousGuestsSubsequentlyToFile() throws Exception {
        /*
         * Only one anonymous permission is allowed, so the second update must fail.
         */
        FileStorageGuestObjectPermission guestPermission1 = asObjectPermission(createAnonymousGuestPermission());
        File updated = addPermissions(file, guestPermission1);
        FileStorageGuestObjectPermission guestPermission2 = asObjectPermission(createAnonymousGuestPermission());
        boolean thrown = false;
        try {
            addPermissions(updated, guestPermission2);
        } catch (AssertionError e) {
            Assert.assertTrue(e.getMessage().contains("PERMISSION_DENIED"));
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testAddExistingAnonymousGuestToNewFile() throws Exception {
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(createAnonymousGuestPermission());
        File updated = addPermissions(file, guestPermission);
        FileStorageObjectPermission entityPermission = findAndCheckPermission(updated);

        File newFile = insertFile(client.getValues().getPrivateInfostoreFolder());
        remember(newFile);

        boolean thrown = false;
        try {
            addPermissions(newFile, entityPermission);
        } catch (AssertionError e) {
            Assert.assertTrue(e.getMessage().contains("PERMISSION_DENIED"));
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testUpdateAnonymousGuestPermissionToWritableOnFile() throws Exception {
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(createAnonymousGuestPermission());
        File updated = addPermissions(file, guestPermission);
        FileStorageObjectPermission entityPermission = findAndCheckPermission(updated);
         /*
          * Change to writable
          */
        List<FileStorageObjectPermission> newPermissions = new ArrayList<>(2);
        newPermissions.addAll(updated.getObjectPermissions());
        newPermissions.remove(entityPermission);
        newPermissions.add(new DefaultFileStorageObjectPermission(entityPermission.getEntity(), false, FileStorageObjectPermission.DELETE));

        DefaultFile toUpdate = new DefaultFile();
        toUpdate.setId(file.getId());
        toUpdate.setFolderId(file.getFolderId());
        toUpdate.setLastModified(file.getLastModified());
        toUpdate.setObjectPermissions(newPermissions);

        boolean thrown = false;
        try {
            updateFile(toUpdate, new Field[] { Field.OBJECT_PERMISSIONS });
        } catch (AssertionError e) {
            Assert.assertTrue(e.getMessage().contains("PERMISSION_DENIED"));
            thrown = true;
        }
        assertTrue(thrown);
    }

    protected static OCLGuestPermission createInvalidAnonymousGuestPermission(String password) {
        OCLGuestPermission guestPermission = createAnonymousPermission(password);
        guestPermission.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        guestPermission.getRecipient().setBits(guestPermission.getPermissionBits());
        return guestPermission;
    }

    protected static OCLGuestPermission createInvalidAnonymousGuestPermission() {
        return createAnonymousGuestPermission(null);
    }

    protected static OCLGuestPermission createInvalidAnonymousPermission(String password) {
        AnonymousRecipient recipient = new AnonymousRecipient();
        recipient.setPassword(password);
        OCLGuestPermission guestPermission = new OCLGuestPermission(recipient);
        AnonymousRecipient anonymousRecipient = new AnonymousRecipient();
        anonymousRecipient.setPassword(password);
        guestPermission.setRecipient(anonymousRecipient);
        guestPermission.setGroupPermission(false);
        guestPermission.setFolderAdmin(false);
        guestPermission.getRecipient().setBits(guestPermission.getPermissionBits());
        return guestPermission;
    }

    private FolderObject addPermissions(FolderObject folder, OCLPermission... permissions) throws Exception {
        FolderObject toUpdate = new FolderObject(folder.getObjectID());
        toUpdate.setLastModified(folder.getLastModified());
        toUpdate.setFolderName(folder.getFolderName());
        toUpdate.setPermissions(folder.getPermissions());
        for (OCLPermission p : permissions) {
            toUpdate.addPermission(p);
        }
        return updateFolder(EnumAPI.OX_NEW, toUpdate);
    }

    private File addPermissions(File file, FileStorageObjectPermission... permissions) throws Exception {
        DefaultFile metadata = new DefaultFile();
        metadata.setId(file.getId());
        metadata.setFolderId(file.getFolderId());
        metadata.setLastModified(file.getLastModified());
        List<FileStorageObjectPermission> newPermissions = new ArrayList<>(2);
        List<FileStorageObjectPermission> oldPermissions = file.getObjectPermissions();
        if (oldPermissions != null) {
            newPermissions.addAll(oldPermissions);
        }

        for (FileStorageObjectPermission p : permissions) {
            newPermissions.add(p);
        }
        metadata.setObjectPermissions(newPermissions);
        return updateFile(metadata, new Field[] { Field.OBJECT_PERMISSIONS });
    }

    private OCLPermission findAndCheckPermission(FolderObject folder) throws Exception {
        List<OCLPermission> permissions = folder.getPermissions();
        Assert.assertEquals("Wrong number of permissions", 2, permissions.size());

        OCLPermission matchingPermission = null;
        for (OCLPermission p : permissions) {
            if (p.getEntity() != client.getValues().getUserId()) {
                Assert.assertTrue(p.isFolderVisible());
                Assert.assertTrue(p.canReadOwnObjects());
                Assert.assertTrue(p.canReadAllObjects());
                Assert.assertFalse(p.isFolderAdmin());
                Assert.assertFalse(p.isSystem());
                Assert.assertFalse(p.isGroupPermission());
                Assert.assertFalse(p.canWriteOwnObjects());
                Assert.assertFalse(p.canWriteAllObjects());
                Assert.assertFalse(p.canDeleteOwnObjects());
                Assert.assertFalse(p.canDeleteAllObjects());
                matchingPermission = p;
            }
        }
        Assert.assertNotNull("Missing expected guest permission", matchingPermission);
        return matchingPermission;
    }

    private FileStorageObjectPermission findAndCheckPermission(File file) throws Exception {
        List<FileStorageObjectPermission> permissions = file.getObjectPermissions();
        Assert.assertNotNull("Missing object permissions", permissions);
        Assert.assertEquals("Wrong number of permissions", 1, permissions.size());

        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission p : permissions) {
            if (p.getEntity() != client.getValues().getUserId()) {
                Assert.assertTrue(p.canRead());
                Assert.assertFalse(p.canWrite());
                Assert.assertFalse(p.canDelete());
                matchingPermission = p;
            }
        }
        Assert.assertNotNull("Missing expected guest permission", matchingPermission);
        return matchingPermission;
    }

}
