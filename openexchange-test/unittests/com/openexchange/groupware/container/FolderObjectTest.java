
package com.openexchange.groupware.container;

import com.openexchange.server.impl.OCLPermission;

import static com.openexchange.groupware.container.FolderObject.*;

public class FolderObjectTest extends FolderChildObjectTest {

    public void testFindDifferingFields() {
        FolderObject dataObject = getFolderObject();
        FolderObject otherDataObject = getFolderObject();

        otherDataObject.setFolderName("Blupp");
        assertDifferences(dataObject, otherDataObject, FolderObject.FOLDER_NAME);

        otherDataObject.setModule(12);
        assertDifferences(dataObject, otherDataObject, FolderObject.FOLDER_NAME, FolderObject.MODULE);

        otherDataObject.setType(12);
        assertDifferences(dataObject, otherDataObject, FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.TYPE);

    }

    public void testAttrAccessors() {

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
