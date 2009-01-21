
package com.openexchange.groupware.container;

import java.util.Date;
import java.util.Set;
import junit.framework.TestCase;

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
        dataObject.setCreationDate(new Date(2l));
        dataObject.setLastModified(new Date(3l));
        dataObject.setModifiedBy(4);
        dataObject.setObjectID(5);

    }

}
