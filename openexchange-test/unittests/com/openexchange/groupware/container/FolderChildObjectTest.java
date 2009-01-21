package com.openexchange.groupware.container;


public class FolderChildObjectTest extends DataObjectTest {
    public void testFindDifferingFields() {
        FolderChildObject dataObject = getFolderChildObject();
        FolderChildObject otherDataObject = getFolderChildObject();
        
        otherDataObject.setParentFolderID(42);
        assertDifferences(dataObject, otherDataObject , FolderChildObject.FOLDER_ID);

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
