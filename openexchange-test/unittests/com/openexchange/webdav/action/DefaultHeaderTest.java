
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import java.util.Date;
import org.junit.Test;
import com.openexchange.webdav.protocol.util.Utils;

public class DefaultHeaderTest extends ActionTestCase {

    @Test
    public void testDefaultHeader() throws Exception {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();
        final MockAction mockAction = new MockAction();

        final AbstractAction action = new WebdavDefaultHeaderAction();
        action.setNext(mockAction);

        final Date now = new Date();
        action.perform(req, res);
        assertEquals(Utils.convert(now), res.getHeader("Date"));
        assertEquals("Openexchange WebDAV", res.getHeader("Server"));
    }
}
