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

package com.openexchange.messaging.json;

import static com.openexchange.json.JSONAssertion.assertValidates;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.json.JSONAssertion;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.messaging.generic.internet.MimeAddressMessagingHeader;

/**
 * {@link AddressHeaderWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AddressHeaderWriterTest {
         @Test
     public void testFeelsResponsible() {
        final AddressHeaderWriter writer = new AddressHeaderWriter();

        final List<String> headerNames = Arrays.asList(
            "From",
            "To",
            "Cc",
            "Bcc",
            "Reply-To",
            "Resent-Reply-To",
            "Disposition-Notification-To",
            "Resent-To",
            "Sender",
            "Resent-Sender",
            "Resent-To",
            "Resent-Cc",
            "Resent-Bcc");

        for (final String headerName : headerNames) {
            assertTrue(writer.handles(entry(new StringMessageHeader(headerName, ""))));
        }

    }

         @Test
     public void testWrite() throws OXException, JSONException {
        final MimeAddressMessagingHeader header = MimeAddressMessagingHeader.valueOfPlain("To", "Clark Kent", "clark.kent@dailyplanet.com");

        final AddressHeaderWriter writer = new AddressHeaderWriter();

        final JSONArray headerJSON = (JSONArray) writer.writeValue(entry(header), null);

        assertNotNull(headerJSON);

        assertEquals(1, headerJSON.length());

        final JSONAssertion assertion = new JSONAssertion().isObject().hasKey("personal").withValue("Clark Kent").hasKey("address").withValue(
            "clark.kent@dailyplanet.com").objectEnds();

        assertValidates(assertion, headerJSON.getJSONObject(0));
    }

         @Test
     public void testWriteFromAsSingleObject() throws OXException, JSONException {
        final MimeAddressMessagingHeader header = MimeAddressMessagingHeader.valueOfPlain("From", "Clark Kent", "clark.kent@dailyplanet.com");

        final AddressHeaderWriter writer = new AddressHeaderWriter();

        final JSONObject headerJSON = (JSONObject) writer.writeValue(entry(header), null);


        final JSONAssertion assertion = new JSONAssertion().isObject().hasKey("personal").withValue("Clark Kent").hasKey("address").withValue(
            "clark.kent@dailyplanet.com").objectEnds();

        assertValidates(assertion, headerJSON);
    }

         @Test
     public void testWriteBasic() throws OXException, JSONException {
        final MessagingHeader header = new StringMessageHeader("To", "Clark Kent <clark.kent@dailyplanet.com>");

        final AddressHeaderWriter writer = new AddressHeaderWriter();

        final JSONArray headerJSON = (JSONArray) writer.writeValue(entry(header), null);

        assertNotNull(headerJSON);

        assertEquals(1, headerJSON.length());

        final JSONAssertion assertion = new JSONAssertion().isObject().hasKey("personal").withValue("Clark Kent").hasKey("address").withValue(
            "clark.kent@dailyplanet.com").objectEnds();

        assertValidates(assertion, headerJSON.getJSONObject(0));
    }

    private SimEntry<String, Collection<MessagingHeader>> entry(final MessagingHeader header) {
        return new SimEntry<String, Collection<MessagingHeader>>(header.getName(), Arrays.asList(header));
    }

}
