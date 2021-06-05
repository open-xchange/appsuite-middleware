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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingHeader;


/**
 * {@link AddressHeaderParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AddressHeaderParserTest {         @Test
     public void testResponsible() {
        final AddressHeaderParser parser = new AddressHeaderParser();

        final List<String> headerNames = Arrays.asList(
            "From",
            "To",
            "Cc",
            "Bcc",
            "Reply-To",
            "Resent-Reply-To",
            "Disposition-Notification-To",
            "Resent-From",
            "Sender",
            "Resent-Sender",
            "Resent-To",
            "Resent-Cc",
            "Resent-Bcc");

        for (final String headerName : headerNames) {
            assertTrue(parser.handles(headerName, null));
        }
    }

         @Test
     public void testParseComplex() throws JSONException, OXException {
        final JSONObject object = new JSONObject("{address : 'clark.kent@dailyplanet.com', personal : 'Clark Kent'}");

        final AddressHeaderParser parser = new AddressHeaderParser();

        final HashMap<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        parser.parseAndAdd(headers, "From", object);

        assertTrue(headers.containsKey("From"));

        final Collection<MessagingHeader> parsed = headers.get("From");
        assertNotNull(parsed);

        assertEquals(1, parsed.size());

        final MessagingAddressHeader header = (MessagingAddressHeader) parsed.iterator().next();

        assertEquals("Clark Kent", header.getPersonal());
        assertEquals("clark.kent@dailyplanet.com", header.getAddress());
    }

         @Test
     public void testParseSimple() throws OXException, JSONException {

        final AddressHeaderParser parser = new AddressHeaderParser();

        final HashMap<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        parser.parseAndAdd(headers, "From", "Clark Kent <clark.kent@dailyplanet.com>");

        assertTrue(headers.containsKey("From"));

        final Collection<MessagingHeader> parsed = headers.get("From");
        assertNotNull(parsed);

        assertEquals(1, parsed.size());

        final MessagingAddressHeader header = (MessagingAddressHeader) parsed.iterator().next();

        assertEquals("Clark Kent", header.getPersonal());
        assertEquals("clark.kent@dailyplanet.com", header.getAddress());
    }

         @Test
     public void testParseList() throws OXException, JSONException {
        final JSONObject object = new JSONObject("{address : 'clark.kent@dailyplanet.com', personal : 'Clark Kent'}");

        final JSONArray array = new JSONArray("['Lois Lane <lois.lane@dailyplanet.com>']");
        array.put(object);

        final AddressHeaderParser parser = new AddressHeaderParser();

        final HashMap<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        parser.parseAndAdd(headers, "From", array);

        assertTrue(headers.containsKey("From"));

        final Collection<MessagingHeader> parsed = headers.get("From");
        assertNotNull(parsed);

        assertEquals(2, parsed.size());

        final Iterator<MessagingHeader> iterator = parsed.iterator();
        MessagingAddressHeader header = (MessagingAddressHeader) iterator.next();

        assertEquals("Lois Lane", header.getPersonal());
        assertEquals("lois.lane@dailyplanet.com", header.getAddress());

        header = (MessagingAddressHeader) iterator.next();

        assertEquals("Clark Kent", header.getPersonal());
        assertEquals("clark.kent@dailyplanet.com", header.getAddress());

    }
}
