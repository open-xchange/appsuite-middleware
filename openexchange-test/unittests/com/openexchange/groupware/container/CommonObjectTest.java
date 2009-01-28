package com.openexchange.groupware.container;

import static com.openexchange.groupware.container.CommonObject.*;

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
    
    public void testAttrAccessors() {
        
        CommonObject object = new CommonObject() {};
        
        // COLOR_LABEL
        assertFalse(object.contains(COLOR_LABEL));
        assertFalse(object.containsLabel());

        object.setLabel(-12);
        assertTrue(object.contains(COLOR_LABEL));
        assertTrue(object.containsLabel());
        assertEquals(-12, object.get(COLOR_LABEL));

        object.set(COLOR_LABEL,12);
        assertEquals(12, object.getLabel());

        object.remove(COLOR_LABEL);
        assertFalse(object.contains(COLOR_LABEL));
        assertFalse(object.containsLabel());



        // CATEGORIES
        assertFalse(object.contains(CATEGORIES));
        assertFalse(object.containsCategories());

        object.setCategories("Bla");
        assertTrue(object.contains(CATEGORIES));
        assertTrue(object.containsCategories());
        assertEquals("Bla", object.get(CATEGORIES));

        object.set(CATEGORIES,"Blupp");
        assertEquals("Blupp", object.getCategories());

        object.remove(CATEGORIES);
        assertFalse(object.contains(CATEGORIES));
        assertFalse(object.containsCategories());



        // NUMBER_OF_LINKS
        assertFalse(object.contains(NUMBER_OF_LINKS));
        assertFalse(object.containsNumberOfLinks());

        object.setNumberOfLinks(-12);
        assertTrue(object.contains(NUMBER_OF_LINKS));
        assertTrue(object.containsNumberOfLinks());
        assertEquals(-12, object.get(NUMBER_OF_LINKS));

        object.set(NUMBER_OF_LINKS,12);
        assertEquals(12, object.getNumberOfLinks());

        object.remove(NUMBER_OF_LINKS);
        assertFalse(object.contains(NUMBER_OF_LINKS));
        assertFalse(object.containsNumberOfLinks());



        // NUMBER_OF_ATTACHMENTS
        assertFalse(object.contains(NUMBER_OF_ATTACHMENTS));
        assertFalse(object.containsNumberOfAttachments());

        object.setNumberOfAttachments(-12);
        assertTrue(object.contains(NUMBER_OF_ATTACHMENTS));
        assertTrue(object.containsNumberOfAttachments());
        assertEquals(-12, object.get(NUMBER_OF_ATTACHMENTS));

        object.set(NUMBER_OF_ATTACHMENTS,12);
        assertEquals(12, object.getNumberOfAttachments());

        object.remove(NUMBER_OF_ATTACHMENTS);
        assertFalse(object.contains(NUMBER_OF_ATTACHMENTS));
        assertFalse(object.containsNumberOfAttachments());

        // PRIVATE_FLAG
        assertFalse(object.contains(PRIVATE_FLAG));
        assertFalse(object.containsPrivateFlag());

        object.setPrivateFlag(false);
        assertTrue(object.contains(PRIVATE_FLAG));
        assertTrue(object.containsPrivateFlag());
        assertEquals(false, object.get(PRIVATE_FLAG));

        object.set(PRIVATE_FLAG,true);
        assertEquals(true, object.getPrivateFlag());

        object.remove(PRIVATE_FLAG);
        assertFalse(object.contains(PRIVATE_FLAG));
        assertFalse(object.containsPrivateFlag());
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
