
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.util.Date;
import org.junit.Test;
import com.google.common.collect.Iterables;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.test.TestInit;

public class GetTest extends InfostoreAJAXTest {

    @Test
    public void testBasic() throws Exception {
        final String origId = Iterables.get(itm.getCreatedEntities(), 0).getId();
        com.openexchange.file.storage.File document = itm.getAction(origId);

        assertEquals("test knowledge", document.getTitle());
        assertEquals("test knowledge description", document.getDescription());

    }

    public void getVersion() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        
        final String origId = Iterables.get(itm.getCreatedEntities(), 0).getId();

        com.openexchange.file.storage.File toUpdate = itm.getAction(origId);
        toUpdate.setDescription("New description");
        itm.updateAction(toUpdate, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.VERSION_COMMENT }, new Date(Long.MAX_VALUE));

        com.openexchange.file.storage.File document = itm.getAction(origId);

        assertEquals("test knowledge", document.getTitle());
        assertEquals("test knowledge description", document.getDescription());

        assertEquals(1, document.getNumberOfVersions());

    }
}
