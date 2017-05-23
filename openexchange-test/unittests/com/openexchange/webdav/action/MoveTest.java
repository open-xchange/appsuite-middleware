
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;

public class MoveTest extends StructureTest {

    //TODO noroot

    @Test
    public void testResource() throws Exception {
        final WebdavPath INDEX_HTML_URL = testCollection.dup().append("index.html");
        final WebdavPath MOVED_INDEX_HTML_URL = testCollection.dup().append("moved_index.html");

        doMove(INDEX_HTML_URL, MOVED_INDEX_HTML_URL);
        doMove(MOVED_INDEX_HTML_URL, INDEX_HTML_URL);

    }

    // Bug 12279
    @Test
    public void testRenameToLowerCase() throws IOException, OXException {
        final WebdavPath INDEX_HTML_URL = testCollection.dup().append("index.html");
        final WebdavPath MOVED_INDEX_HTML_URL = testCollection.dup().append("InDeX.html");

        doMove(INDEX_HTML_URL, MOVED_INDEX_HTML_URL);
        doMove(MOVED_INDEX_HTML_URL, INDEX_HTML_URL);
    }

    private void doMove(WebdavPath INDEX_HTML_URL, WebdavPath MOVED_INDEX_HTML_URL) throws IOException, OXException {
        final String content = getContent(INDEX_HTML_URL);

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("Destination", MOVED_INDEX_HTML_URL.toString());

        final WebdavAction action = new WebdavMoveAction(factory);
        action.perform(req, res);

        assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());

        WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
        assertFalse(resource.exists());

        resource = factory.resolveResource(MOVED_INDEX_HTML_URL);
        assertTrue(resource.exists());

        assertEquals(content, getContent(MOVED_INDEX_HTML_URL));
    }

    @Override
    public WebdavAction getAction(final WebdavFactory factory) {
        return new WebdavMoveAction(factory);
    }
}
