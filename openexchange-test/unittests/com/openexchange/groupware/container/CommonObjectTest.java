
package com.openexchange.groupware.container;

import static com.openexchange.groupware.container.CommonObject.CATEGORIES;
import static com.openexchange.groupware.container.CommonObject.COLOR_LABEL;
import static com.openexchange.groupware.container.CommonObject.NUMBER_OF_ATTACHMENTS;
import static com.openexchange.groupware.container.CommonObject.PRIVATE_FLAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CommonObjectTest extends FolderChildObjectTest {

    @Test
    public void testAttrAccessors() {

        CommonObject object = new CommonObject() {};

        // COLOR_LABEL
        assertFalse(object.contains(COLOR_LABEL));
        assertFalse(object.containsLabel());

        object.setLabel(-12);
        assertTrue(object.contains(COLOR_LABEL));
        assertTrue(object.containsLabel());
        assertEquals(-12, object.get(COLOR_LABEL));

        object.set(COLOR_LABEL, 12);
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

        object.set(CATEGORIES, "Blupp");
        assertEquals("Blupp", object.getCategories());

        object.remove(CATEGORIES);
        assertFalse(object.contains(CATEGORIES));
        assertFalse(object.containsCategories());

        // NUMBER_OF_ATTACHMENTS
        assertFalse(object.contains(NUMBER_OF_ATTACHMENTS));
        assertFalse(object.containsNumberOfAttachments());

        object.setNumberOfAttachments(-12);
        assertTrue(object.contains(NUMBER_OF_ATTACHMENTS));
        assertTrue(object.containsNumberOfAttachments());
        assertEquals(-12, object.get(NUMBER_OF_ATTACHMENTS));

        object.set(NUMBER_OF_ATTACHMENTS, 12);
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

        object.set(PRIVATE_FLAG, true);
        assertEquals(true, object.getPrivateFlag());

        object.remove(PRIVATE_FLAG);
        assertFalse(object.contains(PRIVATE_FLAG));
        assertFalse(object.containsPrivateFlag());
    }

    public void fillCommonObject(CommonObject co) {
        super.fillFolderChildObject(co);

        co.setCategories("c1, c2, c3");
        co.setLabel(1);
        co.setNumberOfAttachments(2);
        co.setPersonalFolderID(12);
        co.setPrivateFlag(true);
    }
}
