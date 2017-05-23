
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.importexport.actions.CSVImportRequest;
import com.openexchange.ajax.importexport.actions.CSVImportResponse;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Charsets;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Bug36943Test}
 *
 * iOS emoticon causes database exception
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug36943Test extends AbstractManagedContactTest {

    /**
     * Initializes a new {@link Bug36943Test}.
     *
     * @param name The test name
     */
    public Bug36943Test() {
        super();
    }

    @Test
    public void testImportCSVWithAstralSymbols() throws Exception {
        /*
         * prepare csv
         */
        String lastName = "Pile of \uD83D\uDCA9 poo";
        String csv = "\"Sur name\",\"Given name\",\"Email 1\"\n" + '"' + lastName + "\",\"Otto\",\"otto@example.com\"\n";
        /*
         * import
         */
        CSVImportRequest request = new CSVImportRequest(folderID, new ByteArrayInputStream(csv.getBytes("UTF-8")), false, new AJAXRequest.Parameter("charset", "UTF-8"));
        CSVImportResponse response = getClient().execute(request);
        assertFalse("response has error", response.hasError());
        JSONArray data = (JSONArray) response.getData();
        assertNotNull("got no data", data);
        assertEquals(1, data.length());
        /*
         * verify imported data
         */
        Contact contact = cotm.getAction(folderID, data.getJSONObject(0).getInt("id"));
        assertNotNull("imported contact not found", contact);
        assertNotNull("no last name imported", contact.getSurName());
        String expectedLastName = lastName.replaceAll("\uD83D\uDCA9", "");
        assertEquals("wrong last name imported", expectedLastName, contact.getSurName());
    }

    @Test
    public void testImportVCardWithAstralSymbols() throws Exception {
        /*
         * prepare vCard
         */
        String uid = UUIDs.getUnformattedStringFromRandom();
        String firstName = "Pile of \uD83D\uDCA9 poo";
        String lastName = "test";
        String vCard = "BEGIN:VCARD" + "\r\n" + "VERSION:3.0" + "\r\n" + "N:" + lastName + ";" + firstName + ";;;" + "\r\n" + "FN:" + firstName + " " + lastName + "\r\n" + "UID:" + uid + "\r\n" + "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" + "END:VCARD" + "\r\n";
        /*
         * import
         */
        VCardImportRequest importRequest = new VCardImportRequest(folderID, new ByteArrayInputStream(vCard.getBytes(Charsets.UTF_8)));
        VCardImportResponse importResponse = getClient().execute(importRequest);
        JSONArray data = (JSONArray) importResponse.getData();
        assertTrue("got no data from import request", null != data && 0 < data.length());
        JSONObject jsonObject = data.getJSONObject(0);
        assertNotNull("got no data from import request", jsonObject);
        int objectID = jsonObject.optInt("id");
        assertTrue("got no object id from import request", 0 < objectID);
        /*
         * verify imported data
         */
        Contact contact = cotm.getAction(folderID, objectID);
        assertNotNull("imported contact not found", contact);
        assertNotNull("no last name imported", contact.getSurName());
        String expectedLastName = lastName.replaceAll("\uD83D\uDCA9", "");
        assertEquals("wrong last name imported", expectedLastName, contact.getSurName());
    }

}
