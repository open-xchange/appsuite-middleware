
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.google.common.collect.Iterables;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.file.storage.File;

public class GetTest extends InfostoreAJAXTest {

    @Test
    public void testBasic() throws Exception {
        File file = Iterables.get(itm.getCreatedEntities(), 0);
        final String origId = file.getId();
        com.openexchange.file.storage.File document = itm.getAction(origId);

        assertEquals(file.getTitle(), document.getTitle());
        assertEquals(file.getDescription(), document.getDescription());

    }
}
