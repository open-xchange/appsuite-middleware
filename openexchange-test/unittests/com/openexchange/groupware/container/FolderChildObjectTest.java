package com.openexchange.groupware.container;

import static com.openexchange.groupware.container.FolderChildObject.*;

public class FolderChildObjectTest extends DataObjectTest {
    @Override
    public void testFindDifferingFields() {
        FolderChildObject dataObject = getFolderChildObject();
        FolderChildObject otherDataObject = getFolderChildObject();

        otherDataObject.setParentFolderID(42);
        assertDifferences(dataObject, otherDataObject , FolderChildObject.FOLDER_ID);

    }

    @Override
    public void testAttrAccessors() {
        FolderChildObject object = new FolderChildObject(){};
        // FOLDER_ID
        assertFalse(object.contains(FOLDER_ID));
        assertFalse(object.containsParentFolderID());

        object.setParentFolderID(-12);
        assertTrue(object.contains(FOLDER_ID));
        assertTrue(object.containsParentFolderID());
        assertEquals(-12, object.get(FOLDER_ID));

        object.set(FOLDER_ID,12);
        assertEquals(12, object.getParentFolderID());

        object.remove(FOLDER_ID);
        assertFalse(object.contains(FOLDER_ID));
        assertFalse(object.containsParentFolderID());

    }

    private FolderChildObject getFolderChildObject() {
        FolderChildObject fco = new FolderChildObject(){};
        fillFolderChildObject(fco);
        return fco;
    }

    public void fillFolderChildObject(FolderChildObject fco) {
        super.fillDataObject(fco);
        fco.setParentFolderID(23);
    }

}
