package com.openexchange.groupware.container;

public class CommonObjectTest extends FolderChildObjectTest {
    public void testFindDifferingFields() {
        CommonObject dataObject = getCommonObject();
        CommonObject otherDataObject = getCommonObject();
        
        otherDataObject.setCategories("blupp");
        assertDifferences(dataObject, otherDataObject , CommonObject.CATEGORIES);

        otherDataObject.setLabel(-2);
        assertDifferences(dataObject, otherDataObject , CommonObject.CATEGORIES, CommonObject.COLOR_LABEL);

        otherDataObject.setNumberOfAttachments(-2);
        assertDifferences(dataObject, otherDataObject , CommonObject.CATEGORIES, CommonObject.COLOR_LABEL, CommonObject.NUMBER_OF_ATTACHMENTS);

        otherDataObject.setNumberOfLinks(-2);
        assertDifferences(dataObject, otherDataObject , CommonObject.CATEGORIES, CommonObject.COLOR_LABEL, CommonObject.NUMBER_OF_ATTACHMENTS, CommonObject.NUMBER_OF_LINKS);


        otherDataObject.setPrivateFlag(false);
        assertDifferences(dataObject, otherDataObject , CommonObject.CATEGORIES, CommonObject.COLOR_LABEL, CommonObject.NUMBER_OF_ATTACHMENTS, CommonObject.NUMBER_OF_LINKS, CommonObject.PRIVATE_FLAG);

    }
    
    private CommonObject getCommonObject() {
        CommonObject co = new CommonObject(){};
        fillCommonObject(co);
        return co;
    }

    public void fillCommonObject(CommonObject co) {
        super.fillFolderChildObject(co);
        
        co.setCategories("c1, c2, c3");
        co.setLabel(1);
        co.setNumberOfAttachments(2);
        co.setNumberOfLinks(3);
        co.setPersonalFolderID(12);
        co.setPrivateFlag(true);
    }
}
