/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.mail;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.ajax.mail.actions.NewMailResponse;
import com.openexchange.exception.OXException;

/**
 * {@link Base64Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Base64Test extends AbstractMailTest {

    private UserValues values;

    /**
     * Initializes a new {@link Base64Test}.
     *
     * @param name
     */
    public Base64Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Test
    public void testGetBase64() throws OXException, IOException, JSONException {

        String eml = "Content-Type: text/plain; charset=\"utf-8\"\n" + "Content-Disposition: inline\n" + "Content-Transfer-Encoding: base64\n" + "MIME-Version: 1.0\n" + "X-Mailer: MIME-tools 5.503 (Entity 5.503)\n" + "Subject: subject\n" + "Date: Wed, 11 Dec 2013 09:30:55 +0100\n" + "Message-ID: <17654355.316137.731867536.18279.3@support.pro-ite.de>\n" + "To: support@open-xchange.com\n" + "From: " + values.getSendAddress() + "\n" + "\n" + "SGFsbG8gSGVyciBHYWJsZXIsCgpPcGVuLVhjaGFuZ2UgU3VwcG9ydCA8c3Vw\n" + "cG9ydEBvcGVuLXhjaGFuZ2UuY29tPiBzY2hyaWViIGFtIDEwLjEyLjIwMTMg\n" + "MjI6MDM6Cgo+IEhhbGxvIEZyYXUgS3V0c2NoZSwKCj4gCj4gSGFiZW4gU2ll\n" + "IGhpZXIgZsO8ciBtaWNoIG1laHIgSW5mb3M/Ck1laHI/IERhcyBpc3QgZGVy\n" + "IEJ1ZzoKCj4gSGVyciBaaWVzY2hlIDIzOjE1IDI2LjkuMjAxMwo+IEhhbGxv\n" + "IEhlcnIgR2FibGVyLAo+IAoKPiBBYmVyIGRpZXNlIFRlcm1pbmJlbmFjaHJp\n" + "Y2h0aWd1bmcgd2lyZCB2b24gT1ggYXV0b21hdGlzY2ggYW4gYWxsZSBUZWls\n" + "bmVobWVyCj4gdmVyc2NoaWNrdCB1bmQgbmljaHQgbnVyIGFuIGRlbiBPcmdh\n" + "bmlzYXRvciEgVW5kIGRpZSBhbmRlcmVuIEJBVy1UZWlsbmVobWVybgo+IHd1\n" + "bmRlcm4gc2ljaCBzY2hvbiDDvGJlciBzbyBlaW5lIEUtTWFpbCBtaXQgZGll\n" + "c2VtIFRleHQsIHp1bWFsIEhyLiBSYWhsZiBqYSBudXIgbnVyCj4genVnZXNh\n" + "Z3QgaGF0dGUuIERlciBUZXh0ICIgLi4gaGF0IC4uLiBnZWJldGVuLCBkYXMg\n" + "RXJlaWduaXMgenUgw6RuZGVybiIga29tbXQgc29uc3QKPiBtLiBFLiBhdWNo\n" + "IG51ciBiZWkgZWluZXIgdGF0c8OkY2hsaWNoZW4gw4RuZGVydW5nIGFuIGRl\n" + "bSBUZXJtaW4gKE9ydCwgQW5tZXJrdW5nc2ZlbGQKPiAuLi4pIHVuZCBuaWNo\n" + "dCBiZWkgZWluZXIgcmVpbmVuIFRlcm1pbmFubmFobWUuCj4gIAo+ICAKPiBW\n" + "aWVsZSBHcsO8w59lCj4gVXdlIFppZXNjaGUKCkRhcyBQcm9ibGVtIGRyZWh0\n" + "IHNpY2ggaW0gS3JlaXMuIEltIHTDpGdsaWNoZW4gR2VzY2jDpGZ0IHdlaXRl\n" + "cmhpbiBlaW4gc3TDtnJlbmRlciBGZWhsZXI6IAoKLSBUZXJtaW5iZXN0w6R0\n" + "aWd1bmcgbGllc3Qgc2ljaCB3aWUgVGVybWluw6RuZGVydW5nLgotIEFsbGUg\n" + "VE4gd2VyZGVuIGluZm9ybWllcnQsIG5pY2h0IG51ciBPcmdhbmlzYXRvci4K\n" + "CkdlZml4dCBzb2xsdGUgZGFzIFZlcmhhbHRlbiBtaW5kZXN0ZW5zIGVpbmUg\n" + "ZGllc2VyIEJlZGluZ3VnZW4gZXJmw7xsbGVuOgotIEVpbmUgVGVybWluYmVz\n" + "dMOkdGlndW5nIGRhcmYgc2ljaCBuaWNodCBsZXNlbiB3aWUgZWluZSBUZXJt\n" + "aW7DpG5kZXJ1bmcsIGVpbiB1bnZlcsOkbmRlcnRlciBUZXJtaW5pbmhhbHQs\n" + "IGRlciBudXIgdm9uIGVpbmVtIFROIGJlc3TDpHRpZ3Qgd3VyZGUsIG11c3Mg\n" + "YWxzIHJlaW5lIEJlc3TDpHRpZ3VuZyBlaW5lcyBUZXJtaW5zIGVya2VubmJh\n" + "ciBzZWluLgotIE51ciBkZXIgT3JnYW5pc2F0b3Igd2lyZCBpbmZvcm1pZXJ0\n" + "LCBuaWNodCBhbGxlIFROLgoKCi0tCk1pdCBmcmV1bmRsaWNoZW4gR3J1ZXNz\n" + "ZW4gLyBXaXRoIGtpbmQgcmVnYXJkcwppLkEuIElociBTdXBwb3J0LVRlYW0s\n" + "IFVyc3VsYSBLdXRzY2hlCgpwcm8taXRlIEdtYkgKQW0gRHVtcGYgNCwgRC04\n" + "Njk3MiBBbHRlbnN0YWR0L09iYi4KVGVsLiArNDkgODg2MS0yNTU0LTAgfCBG\n" + "YXguICs0OSA4ODYxLTI1NTQtMjQKaHR0cDovL3d3dy5wcm8taXRlLmRlIENF\n" + "TzogRGlyayBLdXRzY2hlClJlZy5HZXJpY2h0IE11ZW5jaGVuIEhSIEIgMTI1\n" + "IDA4MSB8IFVzdC4tSWROci4gREUgMjA0IDIyMSAzNjgKTmV3c0AgaHR0cDov\n" + "L3R3aXR0ZXIuY29tL3Byb19pdGUgJiBodHRwOi8vcHJvLWl0ZS5uZXQvYmxv\n" + "Zy8=";

        final NewMailRequest newMailRequest = new NewMailRequest("default0/INBOX", eml.replaceAll("#ADDR#", values.getSendAddress()), -1, true);
        final NewMailResponse newMailResponse = getClient().execute(newMailRequest);

        assertNotNull("Missing folder in response.", newMailResponse.getFolder());
        assertNotNull("Missing ID in response.", newMailResponse.getId());

        final GetRequest newGetRequest = new GetRequest(newMailResponse.getFolder(), newMailResponse.getId(), false, true);
        final GetResponse newGetResponse = getClient().execute(newGetRequest);

        JSONObject data = (JSONObject) newGetResponse.getData();

        JSONObject attachment = data.getJSONArray("attachments").getJSONObject(0);

        assertTrue("Unexpected content in JSON mail representation:\n" + data.toString(2), attachment.getString("content").startsWith("Hallo Herr Gabler"));
    }
}
