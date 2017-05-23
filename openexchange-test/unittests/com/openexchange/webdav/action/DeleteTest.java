
package com.openexchange.webdav.action;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.webdav.protocol.WebdavPath;

public class DeleteTest extends ActionTestCase {

    @Test
    public void testResource() throws Exception {
        final WebdavPath INDEX_HTML_URL = testCollection.dup().append("index.html");
        final WebdavPath DEVELOPMENT_URL = testCollection.dup().append("development");
        final WebdavPath DEVELOPMENT_GUI_INDEX3_HTML_URL = testCollection.dup().append("development/gui/index3.html");

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavDeleteAction();

        action.perform(req, res);

        assertFalse(factory.resolveResource(INDEX_HTML_URL).exists());

        req = new MockWebdavRequest(factory, "http://localhost/");
        req.setUrl(DEVELOPMENT_URL);

        action.perform(req, res);

        assertFalse(factory.resolveResource(DEVELOPMENT_URL).exists());
        assertFalse(factory.resolveResource(DEVELOPMENT_GUI_INDEX3_HTML_URL).exists());

    }
}
