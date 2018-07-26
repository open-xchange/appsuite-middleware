
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.CSVImportRequest;
import com.openexchange.ajax.importexport.actions.CSVImportResponse;

public class Bug20516Test extends AbstractManagedContactTest {

    @Test
    public void testEmail() throws Exception {
        String ical = "Sur name,Given name,Email 1\nBroken,E-Mail,notanaddress\n";
        CSVImportRequest request = new CSVImportRequest(folderID, new ByteArrayInputStream(ical.getBytes()), false);
        CSVImportResponse response = getClient().execute(request);
        JSONArray data = (JSONArray) response.getData();
        assertEquals("Unexpected response length", 1, data.length());
        assertTrue("No object ID for imported contact", data.getJSONObject(0).has("id"));
        assertTrue("No warning for imported contact", data.getJSONObject(0).has("code"));
        assertEquals("Wrong error code for imported contact", "I_E-1306", data.getJSONObject(0).get("code"));
    }

}
