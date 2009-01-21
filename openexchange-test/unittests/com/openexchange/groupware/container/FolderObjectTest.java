package com.openexchange.groupware.container;

import com.openexchange.server.impl.OCLPermission;


public class FolderObjectTest extends FolderChildObjectTest {

    public void testFindDifferingFields() {
        FolderObject dataObject = getFolderObject();
        FolderObject otherDataObject = getFolderObject();
        
        otherDataObject.setFolderName("Blupp");
        assertDifferences(dataObject, otherDataObject , FolderObject.FOLDER_NAME);

        otherDataObject.setModule(12);
        assertDifferences(dataObject, otherDataObject , FolderObject.FOLDER_NAME, FolderObject.MODULE);

        otherDataObject.setType(12);
        assertDifferences(dataObject, otherDataObject ,  FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.TYPE);

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
