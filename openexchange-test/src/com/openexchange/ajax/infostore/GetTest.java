
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.google.common.collect.Iterables;
import com.openexchange.ajax.InfostoreAJAXTest;

public class GetTest extends InfostoreAJAXTest {

    @Test
    public void testBasic() throws Exception {
        final String origId = Iterables.get(itm.getCreatedEntities(), 0).getId();
        com.openexchange.file.storage.File document = itm.getAction(origId);

        assertEquals("test knowledge", document.getTitle());
        assertEquals("test knowledge description", document.getDescription());

    }
}
