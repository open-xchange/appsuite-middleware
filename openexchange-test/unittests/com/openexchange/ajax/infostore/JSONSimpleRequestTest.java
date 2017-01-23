
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.request.JSONSimpleRequest;
import com.openexchange.ajax.request.SimpleRequest;

public class JSONSimpleRequestTest {

    @Test
    public void testGetParameter() throws Exception {
        final String json = "{\"param1\" : \"value1\", \"param2\" : \"value2\"}";
        final SimpleRequest req = new JSONSimpleRequest(new JSONObject(json));

        assertEquals("value1", req.getParameter("param1"));
        assertEquals("value2", req.getParameter("param2"));
        assertNull((req.getParameter("param3")));
    }

    @Test
    public void testGetParameterValues() throws Exception {
        final String json = "{\"param\" : \"value1,value2,value3,value4\"}";
        final SimpleRequest req = new JSONSimpleRequest(new JSONObject(json));

        final String[] values = req.getParameterValues("param");

        assertEquals(4, values.length);
        assertEquals("value1", values[0]);
        assertEquals("value2", values[1]);
        assertEquals("value3", values[2]);
        assertEquals("value4", values[3]);

    }

    @Test
    public void testGetBody() throws Exception {
        final String json = "{\"data\" : [1,2,3,4] }";
        final SimpleRequest req = new JSONSimpleRequest(new JSONObject(json));

        final JSONArray array = (JSONArray) req.getBody();

        assertEquals(4, array.length());
        for (int i = 1; i < 5; i++) {
            assertEquals(i, array.getInt(i - 1));
        }
    }
}
