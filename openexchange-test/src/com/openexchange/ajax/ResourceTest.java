
package com.openexchange.ajax;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.resource.Resource;

public class ResourceTest extends AbstractAJAXSession {

    @Test
    public void testSearch() throws Exception {
        final List<Resource> resources = resTm.search("*");
        assertTrue("resource array size is not > 0", resources.size() > 0);
    }

    @Test
    public void testList() throws Exception {
        List<Resource> resources = resTm.search("*");
        assertTrue("resource array size is not > 0", resources.size() > 0);

        final int[] id = new int[resources.size()];
        for (int a = 0; a < id.length; a++) {
            id[a] = resources.get(a).getIdentifier();
        }

        resources = resTm.list(id);
        assertTrue("resource array size is not > 0", resources.size() > 0);
    }

    @Test
    public void testGet() throws Exception {
        final List<Resource> resources = resTm.search("*");
        assertTrue("resource array size is not > 0", resources.size() > 0);
        Resource res = resTm.get(resources.get(0).getIdentifier());
        assertNotNull(res);
    }
}
