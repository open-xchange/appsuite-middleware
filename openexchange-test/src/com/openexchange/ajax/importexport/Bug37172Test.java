
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Charsets;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Bug37172Test}
 *
 *
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug37172Test extends AbstractManagedContactTest {

    /**
     * Initializes a new {@link Bug37172Test}.
     *
     * @param name The test name
     */
    public Bug37172Test() {
        super();
    }

    @Test
    public void testNotLosingPhoneNumbers() throws Exception {
        /*
         * prepare vCard
         */
        String uid = UUIDs.getUnformattedStringFromRandom();
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\r\n" + "N:;Test;;;\r\n" + "UID:" + uid + "\r\n" + "REV:2015-03-09T23:04:44+00:00\r\n" + "FN:Test\r\n" + "PRODID:-//ownCloud//NONSGML Contacts 0.3.0.18//EN\r\n" + "EMAIL;TYPE=WORK:test@abc123.de\r\n" + "TEL;TYPE=CELL:0151 123456789\r\n" + "TEL;TYPE=HOME:0911 9876543\r\n" + "TEL;TYPE=HOME:0160 123456\r\n" + "IMPP;X-SERVICE-TYPE=jabber:xmpp:87654321\r\n" + "TEL;TYPE=WORK:0912 12345678\r\n" + "END:VCARD\r\n";
        ;
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
        assertEquals("firstname wrong", "Test", contact.getGivenName());
        assertEquals("lastname wrong", null, contact.getSurName());
        assertEquals("cellular phone wrong", "0151 123456789", contact.getCellularTelephone1());
        assertEquals("home phone wrong", "0911 9876543", contact.getTelephoneHome1());
        assertEquals("home phone alternative wrong", "0160 123456", contact.getTelephoneHome2());
        assertEquals("company phone wrong", "0912 12345678", contact.getTelephoneBusiness1());
        assertEquals("xmpp jabber wrong", "xmpp:87654321", contact.getInstantMessenger2());
        assertEquals("email wrong", "test@abc123.de", contact.getEmail1());
    }

    @Test
    public void testNotLosingPhoneNumbersAlt() throws Exception {
        /*
         * create contact
         */
        String uid = UUIDs.getUnformattedStringFromRandom();
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\r\n" + "N:;Test;;;\r\n" + "UID:" + uid + "\r\n" + "REV:2015-03-09T23:04:44+00:00\r\n" + "FN:Test\r\n" + "PRODID:-//ownCloud//NONSGML Contacts 0.3.0.18//EN\r\n" + "EMAIL;TYPE=WORK:test@abc123.de\r\n" + "TEL;TYPE=CELL:0151 123456789\r\n" + "TEL;TYPE=home,voice:0911 9876543\r\n" + "TEL;TYPE=home,voice:0160 123456\r\n" + "IMPP;X-SERVICE-TYPE=jabber:xmpp:87654321\r\n" + "TEL;TYPE=WORK,voice:0912 12345678\r\n" + "END:VCARD\r\n";
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

        Contact contact = cotm.getAction(folderID, objectID);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", "Test", contact.getGivenName());
        assertEquals("lastname wrong", null, contact.getSurName());
        assertEquals("cellular phone wrong", "0151 123456789", contact.getCellularTelephone1());
        assertEquals("home phone wrong", "0911 9876543", contact.getTelephoneHome1());
        assertEquals("home phone alternative wrong", "0160 123456", contact.getTelephoneHome2());
        assertEquals("company phone wrong", "0912 12345678", contact.getTelephoneBusiness1());
        assertEquals("xmpp jabber wrong", "xmpp:87654321", contact.getInstantMessenger2());
        assertEquals("email wrong", "test@abc123.de", contact.getEmail1());
    }
}
