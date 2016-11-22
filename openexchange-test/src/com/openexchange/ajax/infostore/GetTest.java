
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.test.TestInit;

public class GetTest extends InfostoreAJAXTest {

    public GetTest() {
        super();
    }

    @Test
    public void testBasic() throws Exception {

        final Response res = this.get(getWebConversation(), getHostName(), sessionId, clean.get(0));

        assertNoError(res);

        final JSONObject obj = (JSONObject) res.getData();

        assertEquals("test knowledge", obj.getString("title"));
        assertEquals("test knowledge description", obj.getString("description"));

    }

    public void getVersion() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        Response res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("description", "New description"), upload, "text/plain");
        assertNoError(res);

        res = this.get(getWebConversation(), getHostName(), sessionId, clean.get(0), 0);

        assertNoError(res);

        JSONObject obj = (JSONObject) res.getData();

        assertEquals("test knowledge", obj.getString("title"));
        assertEquals("test knowledge description", obj.getString("description"));

        res = this.get(getWebConversation(), getHostName(), sessionId, clean.get(0), 1);

        assertNoError(res);

        obj = (JSONObject) res.getData();

        assertEquals("test knowledge description", obj.getString("New description"));

    }

    // Node 2652    @Test
    @Test
    public void testLastModifiedUTC() throws Exception {
        final Response res = this.get(getWebConversation(), getHostName(), sessionId, clean.get(0));

        assertNoError(res);

        final JSONObject obj = (JSONObject) res.getData();

        assertTrue(obj.has("last_modified_utc"));
    }

    // Bug 12427
    @Test
    public void testNumberOfVersions() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        Response res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("description", "New description"), upload, "text/plain");
        assertNoError(res);

        res = this.get(getWebConversation(), getHostName(), sessionId, clean.get(0), 0);
        assertNoError(res);

        JSONObject obj = (JSONObject) res.getData();
        assertTrue(obj.has("number_of_versions"));
        assertEquals(1, obj.getInt("number_of_versions"));
    }

}
