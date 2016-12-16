
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.test.TestInit;

public class GetTest extends InfostoreAJAXTest {

    public GetTest() {
        super();
    }

    @Test
    public void testBasic() throws Exception {
        com.openexchange.file.storage.File document = itm.getAction(clean.get(0));

        assertEquals("test knowledge", document.getTitle());
        assertEquals("test knowledge description", document.getDescription());

    }

    public void getVersion() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        
        
        com.openexchange.file.storage.File toUpdate = itm.getAction(clean.get(0));
        toUpdate.setDescription("New description");
        itm.updateAction(toUpdate, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.VERSION_COMMENT }, new Date(Long.MAX_VALUE));

        com.openexchange.file.storage.File document = itm.getAction(clean.get(0));

        assertEquals("test knowledge", document.getTitle());
        assertEquals("test knowledge description", document.getDescription());

        assertEquals(1, document.getNumberOfVersions());

    }
}
