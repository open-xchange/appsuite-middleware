
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavLock.Scope;
import com.openexchange.webdav.protocol.WebdavLock.Type;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;

public class UnlockTest extends ActionTestCase {

    @Test
    public void testUnlock() throws Exception {
        final WebdavPath INDEX_HTML = testCollection.dup().append("index.html");

        final WebdavResource resource = factory.resolveResource(INDEX_HTML);

        final WebdavLock lock = new WebdavLock();
        lock.setTimeout(WebdavLock.NEVER);
        lock.setDepth(0);
        lock.setOwner("me");
        lock.setScope(Scope.EXCLUSIVE_LITERAL);
        lock.setType(Type.WRITE_LITERAL);

        resource.lock(lock);

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML);
        req.setHeader("Lock-Token", "<" + lock.getToken() + ">");

        final WebdavAction action = new WebdavUnlockAction();

        action.perform(req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
        assertTrue(resource.getLocks().isEmpty());
    }
}
