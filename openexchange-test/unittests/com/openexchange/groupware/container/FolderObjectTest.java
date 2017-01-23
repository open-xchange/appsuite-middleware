
package com.openexchange.groupware.container;

import static com.openexchange.groupware.container.FolderObject.FOLDER_NAME;
import static com.openexchange.groupware.container.FolderObject.MODULE;
import static com.openexchange.groupware.container.FolderObject.PERMISSIONS_BITS;
import static com.openexchange.groupware.container.FolderObject.SUBFOLDERS;
import static com.openexchange.groupware.container.FolderObject.TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.openexchange.server.impl.OCLPermission;

public class FolderObjectTest extends FolderChildObjectTest {

    @Test
    public void testPermissionMethods() {
        FolderObject object = new FolderObject();

        // PERMISSIONS_BITS
        assertFalse(object.contains(PERMISSIONS_BITS));
        assertFalse(object.containsPermissions());

        List<OCLPermission> permissions = Arrays.asList(new OCLPermission[] {});

        object.setPermissions(permissions);
        assertTrue(object.contains(PERMISSIONS_BITS));
        assertTrue(object.containsPermissions());
        assertEquals(permissions, object.get(PERMISSIONS_BITS));

        List<OCLPermission> permissions2 = Arrays.asList(new OCLPermission[] { new OCLPermission() });

        object.set(PERMISSIONS_BITS, permissions2);
        assertEquals(permissions2, object.getPermissions());

        object.remove(PERMISSIONS_BITS);
        assertFalse(object.contains(PERMISSIONS_BITS));
        assertFalse(object.containsPermissions());
    }

    @Test
    public void testSubfolderMethods() {
        FolderObject object = new FolderObject();

        // SUBFOLDERS
        assertFalse(object.contains(SUBFOLDERS));
        assertFalse(object.containsSubfolderFlag());

        object.setSubfolderFlag(true);
        assertTrue(object.contains(SUBFOLDERS));
        assertTrue(object.containsSubfolderFlag());
        assertEquals(true, object.get(SUBFOLDERS));

        object.remove(SUBFOLDERS);
        assertFalse(object.contains(SUBFOLDERS));
        assertFalse(object.containsSubfolderFlag());
    }

    @Test
    public void testTypeMethods() {
        FolderObject object = new FolderObject();

        // TYPE
        assertFalse(object.contains(TYPE));
        assertFalse(object.containsType());

        object.setType(-12);
        assertTrue(object.contains(TYPE));
        assertTrue(object.containsType());
        assertEquals(-12, object.get(TYPE));

        object.set(TYPE, 12);
        assertEquals(12, object.getType());

        object.remove(TYPE);
        assertFalse(object.contains(TYPE));
        assertFalse(object.containsType());
    }

    @Test
    public void testFolderMethods() {
        FolderObject object = new FolderObject();

        // FOLDER_NAME
        assertFalse(object.contains(FOLDER_NAME));
        assertFalse(object.containsFolderName());

        object.setFolderName("Bla");
        assertTrue(object.contains(FOLDER_NAME));
        assertTrue(object.containsFolderName());
        assertEquals("Bla", object.get(FOLDER_NAME));

        object.set(FOLDER_NAME, "Blupp");
        assertEquals("Blupp", object.getFolderName());

        object.remove(FOLDER_NAME);
        assertFalse(object.contains(FOLDER_NAME));
        assertFalse(object.containsFolderName());
    }

    @Test
    public void testModuleMethods() {
        FolderObject object = new FolderObject();

        // MODULE
        assertFalse(object.contains(MODULE));
        assertFalse(object.containsModule());

        object.setModule(-12);
        assertTrue(object.contains(MODULE));
        assertTrue(object.containsModule());
        assertEquals(-12, object.get(MODULE));

        object.set(MODULE, 12);
        assertEquals(12, object.getModule());

        object.remove(MODULE);
        assertFalse(object.contains(MODULE));
        assertFalse(object.containsModule());
    }

    public FolderObject getFolderObject() {
        FolderObject folderObject = new FolderObject();

        fillFolderObject(folderObject);

        return folderObject;
    }

    public void fillFolderObject(FolderObject object) {

        super.fillFolderChildObject(object);

        object.setCreator(-12);

        object.setDefaultFolder(false);

        object.setFolderName("Bla");

        object.setFullName("Bla");

        object.setModule(-12);

        object.setPermissionFlag(-12);

        object.setPermissionsAsArray(new OCLPermission[0]);

        object.setSubfolderFlag(false);

        object.setType(-12);
    }

}
