
package com.openexchange.groupware.container;

import java.util.Date;
import java.util.Set;
import junit.framework.TestCase;

import static com.openexchange.groupware.container.DataObject.*;

public class DataObjectTest extends TestCase {

    public void testFindDifferingFields() {
        DataObject dataObject = getDataObject();
        DataObject otherDataObject = getDataObject();

        otherDataObject.setCreatedBy(-1);
        assertDifferences(dataObject, otherDataObject, DataObject.CREATED_BY);

        otherDataObject.setCreationDate(new Date());
        assertDifferences(dataObject, otherDataObject, DataObject.CREATED_BY, DataObject.CREATION_DATE);

        otherDataObject.setLastModified(new Date());
        assertDifferences(dataObject, otherDataObject, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED);

        otherDataObject.setModifiedBy(-23);
        assertDifferences(
            dataObject,
            otherDataObject,
            DataObject.CREATED_BY,
            DataObject.CREATION_DATE,
            DataObject.LAST_MODIFIED,
            DataObject.MODIFIED_BY);

        otherDataObject.setObjectID(12);
        assertDifferences(
            dataObject,
            otherDataObject,
            DataObject.CREATED_BY,
            DataObject.CREATION_DATE,
            DataObject.LAST_MODIFIED,
            DataObject.MODIFIED_BY,
            DataObject.OBJECT_ID);
    }

    public void testAttrAccessors() {
        DataObject object = new DataObject(){};

        // LAST_MODIFIED
        assertFalse(object.contains(LAST_MODIFIED));
        assertFalse(object.containsLastModified());

        object.setLastModified(new Date(42));
        assertTrue(object.contains(LAST_MODIFIED));
        assertTrue(object.containsLastModified());
        assertEquals(new Date(42), object.get(LAST_MODIFIED));

        object.set(LAST_MODIFIED,new Date(23));
        assertEquals(new Date(23), object.getLastModified());

        object.remove(LAST_MODIFIED);
        assertFalse(object.contains(LAST_MODIFIED));
        assertFalse(object.containsLastModified());



        // OBJECT_ID
        assertFalse(object.contains(OBJECT_ID));
        assertFalse(object.containsObjectID());

        object.setObjectID(-12);
        assertTrue(object.contains(OBJECT_ID));
        assertTrue(object.containsObjectID());
        assertEquals(-12, object.get(OBJECT_ID));

        object.set(OBJECT_ID,12);
        assertEquals(12, object.getObjectID());

        object.remove(OBJECT_ID);
        assertFalse(object.contains(OBJECT_ID));
        assertFalse(object.containsObjectID());



        // MODIFIED_BY
        assertFalse(object.contains(MODIFIED_BY));
        assertFalse(object.containsModifiedBy());

        object.setModifiedBy(-12);
        assertTrue(object.contains(MODIFIED_BY));
        assertTrue(object.containsModifiedBy());
        assertEquals(-12, object.get(MODIFIED_BY));

        object.set(MODIFIED_BY,12);
        assertEquals(12, object.getModifiedBy());

        object.remove(MODIFIED_BY);
        assertFalse(object.contains(MODIFIED_BY));
        assertFalse(object.containsModifiedBy());



        // CREATION_DATE
        assertFalse(object.contains(CREATION_DATE));
        assertFalse(object.containsCreationDate());

        object.setCreationDate(new Date(42));
        assertTrue(object.contains(CREATION_DATE));
        assertTrue(object.containsCreationDate());
        assertEquals(new Date(42), object.get(CREATION_DATE));

        object.set(CREATION_DATE,new Date(23));
        assertEquals(new Date(23), object.getCreationDate());

        object.remove(CREATION_DATE);
        assertFalse(object.contains(CREATION_DATE));
        assertFalse(object.containsCreationDate());



        // CREATED_BY
        assertFalse(object.contains(CREATED_BY));
        assertFalse(object.containsCreatedBy());

        object.setCreatedBy(-12);
        assertTrue(object.contains(CREATED_BY));
        assertTrue(object.containsCreatedBy());
        assertEquals(-12, object.get(CREATED_BY));

        object.set(CREATED_BY,12);
        assertEquals(12, object.getCreatedBy());

        object.remove(CREATED_BY);
        assertFalse(object.contains(CREATED_BY));
        assertFalse(object.containsCreatedBy());

    }

    public void assertDifferences(DataObject dataObject, DataObject otherDataObject, int... fields) {

        Set<Integer> differingFields = dataObject.findDifferingFields(otherDataObject);
        String diffString = differingFields.toString();

        for (int field : fields) {
            assertTrue(diffString+" Didn't find: "+field, differingFields.remove(field));
        }
        assertTrue(diffString+" Have unexpected field: "+differingFields, differingFields.isEmpty());
    }

    private DataObject getDataObject() {
        DataObject dataObject = new DataObject() {
        };

        fillDataObject(dataObject);

        return dataObject;
    }

    public void fillDataObject(DataObject dataObject) {
        dataObject.setCreatedBy(1);
        dataObject.setCreationDate(new Date(2L));
        dataObject.setLastModified(new Date(3L));
        dataObject.setModifiedBy(4);
        dataObject.setObjectID(5);

    }

}
